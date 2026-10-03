import test from 'node:test';
import assert from 'node:assert/strict';
import {level,refresh,validateConfig} from './model.mjs';
import {collect} from './github.mjs';
import {render,updateReadme} from './render.mjs';
import {readFileSync} from 'node:fs';
const config={repository:'owner/repo',author:'author',stage:0,currentRound:1};
const initial=()=>({repository:'owner/repo',author:'author',lastSyncedAt:null,rounds:[{number:1,since:'1970-01-01T00:00:00.000Z',pets:[]}]});
const event=(id, overrides={})=>({id,userId:20,login:'friend',kind:'comment',at:'2026-01-01T00:00:00Z',...overrides});
const now='2026-02-01T00:00:00.000Z';
test('exact evolution boundaries',()=>{
  assert.deepEqual([0,4,5,9,10,99].map(level),[1,1,2,2,3,3]);
});
test('unique inline comments grant XP; submitted reviews grant membership; self and bots excluded',()=>{
  const data=[event(1),event(1),event(1,{kind:'review'}),event(2,{login:'AUTHOR'}),event(3,{bot:true}),event(4,{kind:'review',userId:21,login:'review-only'})];
  const state=refresh(config,initial(),data,now);
  assert.deepEqual(state.rounds[0].pets,[{id:20,username:'friend',comments:1,reviews:1},{id:21,username:'review-only',comments:0,reviews:1}]);
  assert.deepEqual(refresh(config,state,data,now),state);
});
test('round cutover uses timestamp, preserves old attribution and assigns boundary to next round',()=>{
  const data=[event(1),event(2,{at:now})];
  const next=refresh({...config,currentRound:2},initial(),data,now);
  assert.equal(next.rounds[0].pets[0].comments,1);
  assert.equal(next.rounds[1].pets[0].comments,1);
  const again=refresh({...config,currentRound:2},next,[...data,event(3,{at:'2026-01-20T00:00:00Z'})],'2026-03-01T00:00:00Z');
  assert.equal(again.rounds[0].pets[0].comments,2);
  assert.equal(again.rounds[1].since,now);
  assert.throws(()=>refresh(config,next,[],now),/Advance/);
  assert.throws(()=>refresh({...config,currentRound:3},initial(),[],now),/Advance/);
});
test('refresh reflects deleted comments and tracks identity through username changes',()=>{
  const before=refresh(config,initial(),[event(1),event(2)],now);
  const after=refresh(config,before,[event(2,{login:'renamed'})],now);
  assert.equal(after.rounds[0].pets[0].comments,1);
  assert.equal(after.rounds[0].pets[0].username,'renamed');
  assert.equal(before.rounds[0].pets[0].comments,2);
});
test('reject invalid configuration and mismatched state',()=>{
  for(const c of [{stage:9},{stage:-1},{stage:1.5},{currentRound:0},{currentRound:4},{author:'../oops'},{repository:'bad'}]) assert.throws(()=>validateConfig({...config,...c}));
  assert.throws(()=>refresh({...config,repository:'other/repo'},initial(),[],now),/another/);
});
test('collector only reads author PRs (including closed), skips pending/deleted users, excludes issue comments by endpoint',async()=>{
  const paths=[];
  const events=await collect(config,async path=>{
    paths.push(path);
    if(path.includes('?')) return [{number:1,user:{login:'AUTHOR'}},{number:2,user:{login:'someone'}}];
    if(path.endsWith('/reviews')) return [{id:9,user:{id:20,login:'friend',type:'User'},state:'COMMENTED',submitted_at:now},{id:10,user:{login:'friend'},state:'PENDING'}];
    return [{id:11,user:{id:20,login:'friend',type:'User'},created_at:now},{id:12,user:null,created_at:now}];
  });
  assert.deepEqual(paths,['repos/owner/repo/pulls?state=all','repos/owner/repo/pulls/1/reviews','repos/owner/repo/pulls/1/comments']);
  assert.deepEqual(events.map(e=>e.kind),['review','comment']);
  await assert.rejects(()=>collect(config,async()=>{throw Error('network');}),/network/);
});
test('README preserves banner and all handwritten content across repeated renders',()=>{
  const source='![banner_server.png](banner_server.png)\n\nCustom text\n';
  const once=updateReadme(source,'generated')+'\nFooter';
  const twice=updateReadme(once,'updated');
  assert.ok(twice.startsWith(source));
  assert.ok(twice.endsWith('\nFooter'));
  assert.equal(updateReadme(twice,'updated'),twice);
  assert.throws(()=>updateReadme('other banner','x'),/banner/);
  assert.throws(()=>updateReadme(source+'<!-- REVIEW-ZOO:START -->','x'),/markers/);
});
test('SVG deterministic, escapes text and fits many pets across archived rounds',()=>{
  const sprites=JSON.parse(readFileSync(new URL('../../assets/review-zoo/sprites.json',import.meta.url)));
  const state=refresh(config,initial(),Array.from({length:15},(_,i)=>event(i,{userId:i+100,login:'a'.repeat(39)})),now);
  const svg=render(config,state,sprites);
  assert.equal(render(config,state,sprites),svg);
  assert.equal((svg.match(/PARTY MEMBER|NEW FRIEND|GUILD LEGEND/g)||[]).length,15);
  state.rounds[0].pets[0].username='<script>&';
  assert.ok(render(config,state,sprites).includes('&lt;script&gt;&amp;'));
});
test('unchanged data does not create timestamp-only saves',()=>{
  const first=refresh(config,initial(),[],now);
  assert.deepEqual(refresh(config,first,[],'2026-03-01T00:00:00Z'),first);
});
test('API pagination reads beyond 100 and surfaces HTTP failure',async()=>{
  const {githubList}=await import('./github.mjs');
  const originalFetch=globalThis.fetch, oldToken=process.env.GH_TOKEN;
  const paths=[];
  try {
    process.env.GH_TOKEN='test-token';
    globalThis.fetch=async url=>{ paths.push(url); return {ok:true,json:async()=>paths.length===1?Array.from({length:100},(_,i)=>({id:i})):[{id:100}]}; };
    assert.equal((await githubList('repos/owner/repo/pulls?state=all')).length,101);
    assert.ok(paths[1].endsWith('&per_page=100&page=2'));
    globalThis.fetch=async()=>({ok:false,status:403});
    await assert.rejects(()=>githubList('repos/owner/repo/pulls'),/HTTP 403/);
  } finally {
    globalThis.fetch=originalFetch;
    if(oldToken===undefined) delete process.env.GH_TOKEN; else process.env.GH_TOKEN=oldToken;
  }
});
