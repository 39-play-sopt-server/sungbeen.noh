import {readFile,writeFile,rename,unlink} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import {resolve} from 'node:path';
import {validateConfig,refresh} from './model.mjs';
import {collect} from './github.mjs';
import {render,summary,updateReadme} from './render.mjs';

const root=fileURLToPath(new URL('../../',import.meta.url));
const read=path=>readFile(resolve(root,path),'utf8');
async function run() {
  const flags=process.argv.slice(2);
  if(flags.some(f=>f!=='--sync')) throw Error('Usage: node scripts/review-zoo/index.mjs [--sync]');
  const config=JSON.parse(await read('scripts/review-zoo/config.json'));
  validateConfig(config);
  let state=JSON.parse(await read('assets/review-zoo/state.json'));
  if(flags.includes('--sync')) {
    // Capture boundary before reads: events arriving while fetching belong to the new round.
    const now=new Date().toISOString();
    state=refresh(config,state,await collect(config),now);
  }
  if(state.rounds.at(-1).number!==config.currentRound) throw Error('Run --sync to establish the new round boundary');
  if(state.repository!==config.repository || state.author.toLowerCase()!==config.author.toLowerCase()) throw Error('Config/state identity mismatch');
  const sprites=JSON.parse(await read('assets/review-zoo/sprites.json'));
  const outputs={
    'assets/review-zoo/world.svg':render(config,state,sprites)+'\n',
    'README.md':updateReadme(await read('README.md'),summary(config,state)),
    'assets/review-zoo/state.json':JSON.stringify(state,null,2)+'\n'
  };
  // Stage every file first. Each rename is atomic; a crash between renames is recovered by rerunning.
  // State goes last, so an uncommitted round transition can always be retried.
  const temps=[];
  try {
    for(const [path,content] of Object.entries(outputs)) {const tmp=resolve(root,`${path}.tmp`); await writeFile(tmp,content); temps.push(tmp);}
    for(const path of Object.keys(outputs)) await rename(resolve(root,`${path}.tmp`),resolve(root,path));
  } finally { for(const path of temps) await unlink(path).catch(()=>{}); }
  console.log(`Rendered stage ${config.stage}, round ${config.currentRound}, ${state.rounds.reduce((n,r)=>n+r.pets.length,0)} pets. No remote writes.`);
}
run().catch(error=>{console.error(error.message);process.exitCode=1;});
