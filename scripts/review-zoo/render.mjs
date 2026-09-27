import {createHash} from 'node:crypto';
import {level} from './model.mjs';
import {colors as C,rect,text,panel,pixelText,creature,heart,star,coin,cloud,flower,meadow} from './pixel-art.mjs';

function petSprite(sprites,seed,lv,x,y,scale=5,animate=false) {
  const families=Object.values(sprites.families);
  const index=createHash('sha256').update(String(seed)).digest().readUInt32BE()%families.length;
  return creature(families[index][lv-1],x,y,scale,animate);
}
function scene(sprites,stage) {
  const p=[panel(44,198,872,310,C.sky),rect(48,202,864,35,C.mint),text(65,225,`WORLD 01 / ${stage===0?'OT':'WEEK '+stage}`,13),text(688,225,'HP',12)];
  for(let i=0;i<3;i++)p.push(heart(724+i*35,213,3));
  p.push(cloud(84,274,8),cloud(762,269,8),cloud(223,344,5));
  p.push(rect(48,389,864,65,'#bbe2c0'),rect(74,365,101,59,'#bbe2c0'),rect(697,372,108,54,'#bbe2c0'));
  p.push(panel(263,255,436,86,C.white),pixelText(stage===0?'HELLO, WORLD!':`STAGE 0${stage} START!`,310,272,3,C.ink),text(481,319,'서버 개발자의 작은 모험',14,C.muted,'text-anchor="middle"'));
  for(const x of [308,353,564,609])p.push(coin(x,370));
  p.push(meadow(48,449,864,55));
  p.push(creature(sprites.scenery.rabbit,93,358,6,true),creature(sprites.scenery.frog,431,353,6,true),creature(sprites.scenery.cat,733,358,6,true));
  for(const [x,y,c] of [[207,428,C.pink],[263,432,C.white],[631,429,C.pink],[842,428,C.gold]])p.push(flower(x,y,c));
  p.push(text(481,483,'CODE  ·  REVIEW  ·  LEVEL UP  ·  REPEAT',12,C.ink,'text-anchor="middle"'));
  return p.join('');
}
function stageMap(stage) {
  const p=[panel(44,532,872,140,C.cream),pixelText('STAGE SELECT',64,550,2,C.muted),text(890,565,'달력 말고, 퀘스트 순서대로!',12,C.muted,'text-anchor="end"')];
  p.push(rect(91,613,776,4,'#d8caaf'));
  ['OT','01','02','03','04','05','06','07','08'].forEach((label,i)=>{
    const x=90+i*97,active=i===stage,done=i<stage;
    p.push(panel(x-21,594,42,40,active?C.pink:done?C.mint:'#eadfce'));
    p.push(pixelText(label,x-14,606,2,active||done?C.ink:'#aa9b96'));
    if(active)p.push(text(x,655,'YOU!',11,C.pinkDark,'text-anchor="middle"'));
    else if(done)p.push(text(x,655,'CLEAR',10,C.muted,'text-anchor="middle"'));
  });
  return p.join('');
}
function dialogue() {
  return panel(44,696,872,134,C.white)+rect(48,700,864,28,C.lilac)+text(64,719,'NPC CODEX  >  README 제작 완료!',12)+
    text(72,761,'신기하죠? Codex가 다 했어요. 이 문장도요.',19)+
    text(72,799,'하지만 과제는 제가 할거에요. 파이팅!',18,C.pinkDark)+star(853,763,4,C.gold);
}
function emptyParty(sprites,y) {
  const p=[panel(44,y,872,236,C.cream),pixelText('CHOOSE YOUR NEXT FRIEND',74,y+23,3,C.ink),text(74,y+65,'코드 리뷰를 남기고 귀여운 펫을 키워보세요!',14,C.muted)];
  const families=Object.values(sprites.families);
  families.forEach((frames,i)=>{
    const x=76+i*180;
    p.push(rect(x,y+92,148,104,['#e4d9f3','#daf0dd','#ffe3ba'][i]));
    p.push(creature(frames[0],x+26,y+99,6,true),text(x+74,y+214,'???',13,C.muted,'text-anchor="middle"'));
  });
  p.push(pixelText('PLAYER 2',655,y+117,2,C.muted),pixelText('JOIN!',655,y+143,4,C.pinkDark));
  p.push(text(655,y+192,'첫 리뷰어를 기다리는 중',12,C.muted),text(655,y+214,'펫 미리보기 · 실제 리뷰어 0명',11,C.muted));
  return p.join('');
}
function petCard(sprites,pet,x,y) {
  const lv=level(pet.comments),accent=[C.mint,C.lilac,C.gold][lv-1];
  const p=[panel(x,y,424,194,C.white),rect(x+4,y+4,416,28,accent),text(x+15,y+23,`LV.${lv}  ${['NEW FRIEND','PARTY MEMBER','GUILD LEGEND'][lv-1]}`,12)];
  p.push(rect(x+14,y+48,108,118,'#e6efdf'),meadow(x+14,y+138,108,28),petSprite(sprites,pet.id,lv,x+20,y+52,6,true));
  p.push(text(x+138,y+65,`@${pet.username}`,pet.username.length>28?10:12,C.ink));
  p.push(text(x+138,y+93,`${pet.comments} comments`,14,C.ink),text(x+138,y+115,`${pet.reviews} reviews`,12,C.muted));
  for(let n=0;n<10;n++)p.push(rect(x+138+n*26,y+133,20,10,n<Math.min(pet.comments,10)?C.pinkDark:'#eadfe4'));
  p.push(text(x+138,y+174,lv===3?'MAX! 이제 여기가 집입니다.':`진화까지 댓글 ${lv*5-pet.comments}개!`,12,C.muted));
  if(lv===3)p.push(star(x+95,y+37,3,C.gold));
  return p.join('');
}
export function render(config,state,sprites) {
  const rounds=[];
  let y=947;
  for(const round of state.rounds) {
    const active=round.number===config.currentRound;
    rounds.push(pixelText(`ROUND 0${round.number}`,48,y,3,C.cream),text(249,y+19,active?'NOW PLAYING':'SAVED PARTY',12,active?C.mint:C.lilac));
    rounds.push(text(913,y+19,`${round.pets.length} FRIEND${round.pets.length===1?'':'S'}`,12,C.lilac,'text-anchor="end"'));
    y+=40;
    if(!round.pets.length){rounds.push(emptyParty(sprites,y));y+=264;}
    else {
      round.pets.forEach((pet,i)=>rounds.push(petCard(sprites,pet,44+(i%2)*448,y+Math.floor(i/2)*218)));
      y+=Math.ceil(round.pets.length/2)*218+16;
    }
  }
  const height=y+160;
  const p=[`<svg xmlns="http://www.w3.org/2000/svg" width="960" height="${height}" viewBox="0 0 960 ${height}" role="img" aria-labelledby="title desc"><title id="title">Sungbeen's Server Arcade — Code Review Zoo</title><desc id="desc">귀여운 픽셀 동물과 OT부터 8주차까지의 스테이지. 리뷰어마다 펫이 생기고 inline 댓글 5개마다 진화합니다.</desc><style>@keyframes hop{0%,65%,100%{transform:translateY(0)}75%,85%{transform:translateY(-5px)}}.hop{animation:hop 4s steps(1,end) infinite}.hop:nth-child(2n){animation-delay:1.3s}@media(prefers-reduced-motion:reduce){.hop{animation:none}}</style><g font-family="ui-monospace,SFMono-Regular,Menlo,Consolas,monospace">`,rect(0,0,960,height,C.bg),rect(16,16,928,height-32,C.edge),rect(24,24,912,height-48,C.lilac),rect(32,184,896,height-210,C.bg)];
  p.push(panel(40,32,880,140,C.bg,C.ink),text(62,57,'39 PLAY SOPT / SUNGBEEN',12,C.lilac),text(895,57,'1P  •  INSERT REVIEW',12,C.mint,'text-anchor="end"'));
  p.push(pixelText('SERVER ARCADE',210,82,7,C.pinkDark),pixelText('SERVER ARCADE',207,77,7,C.cream),star(86,91,5,C.pink),star(837,91,5,C.mint));
  p.push(text(480,151,'작은 리뷰가 모여, 귀여운 동료가 됩니다.',13,C.lilac,'text-anchor="middle"'));
  p.push(scene(sprites,config.stage),stageMap(config.stage),dialogue());
  p.push(pixelText('REVIEW ZOO',48,869,4,C.mint),text(910,894,'5 COMMENTS = LEVEL UP!',13,C.gold,'text-anchor="end"'),...rounds);
  p.push(panel(44,y,872,56,C.pink),heart(67,y+18,3,C.ink),text(105,y+35,'펫은 6시간마다 자동 갱신돼요! 반영이 조금 늦을 수 있어요.',16));
  p.push(rect(72,y+85,25,9,C.lilac),rect(80,y+77,9,25,C.lilac),rect(795,y+81,18,18,C.pink),rect(828,y+72,18,18,C.gold));
  p.push(text(480,y+96,'A : CODE    B : REVIEW    START : 파이팅!',12,C.lilac,'text-anchor="middle"'),text(480,y+120,'PIXELS · TINY CREATURES / CLINT BELLANGER / CC0',10,C.lilac,'text-anchor="middle"'),'</g></svg>');
  return p.join('\n');
}

export function summary(config,state) {
  let s=`![Server Quest HUD — OT~8주차 진행도와 리뷰 펫](assets/review-zoo/world.svg)\n\n`;
  s+=`**현재 퀘스트:** ${config.stage===0?'OT':`${config.stage}주차`} · **활동 조:** Round ${config.currentRound}  \n`;
  s+='진화 규칙: inline review comment **0–4개 LV1 · 5–9개 LV2 · 10개 이상 LV3**. 리뷰만 남겨도 LV1 동료로 합류합니다.\n\n';
  s+='<details>\n<summary>세이브 데이터 · 라운드별 리뷰 기록</summary>\n\n';
  for(const r of state.rounds) {
    s+=`### Round ${r.number}${r.number===config.currentRound?' · 진행 중':' · 기록'}\n\n`;
    if(!r.pets.length) s+='아직 합류한 리뷰어가 없습니다. 첫 리뷰를 기다리는 중!\n\n';
    else { s+='| 동료 | 레벨 | Inline comments | Reviews |\n| --- | --- | --- | --- |\n'; for(const p of r.pets) s+=`| [@${p.username}](https://github.com/${encodeURIComponent(p.username)}) | LV${level(p.comments)} | ${p.comments} | ${p.reviews} |\n`; s+='\n'; }
  }
  s+=`집계 데이터 확인 시각(변경 시 저장): ${state.lastSyncedAt ?? '아직 실행하지 않음'}\n\n</details>`;
  return s;
}
export function updateReadme(readme,generated) {
  const start='<!-- REVIEW-ZOO:START -->', end='<!-- REVIEW-ZOO:END -->';
  if(!readme.startsWith('![banner_server.png](banner_server.png)')) throw Error('Required first banner must remain intact');
  const a=readme.indexOf(start), b=readme.indexOf(end);
  const block=`${start}\n${generated}\n${end}`;
  if(a<0 && b<0) return `${readme.trimEnd()}\n\n${block}\n`;
  if(a<0 || b<a || readme.indexOf(start,a+start.length)>=0 || readme.indexOf(end,b+end.length)>=0) throw Error('Invalid README generation markers');
  return readme.slice(0,a)+block+readme.slice(b+end.length);
}
