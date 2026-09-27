import {execFileSync} from 'node:child_process';

export async function githubList(path) {
  const token = process.env.GH_TOKEN || process.env.GITHUB_TOKEN;
  const result = [];
  for (let page = 1; ; page++) {
    const endpoint = `${path}${path.includes('?') ? '&' : '?'}per_page=100&page=${page}`;
    let batch;
    if (token) {
      const response = await fetch(`https://api.github.com/${endpoint}`, {
        headers: {Authorization: `Bearer ${token}`, Accept: 'application/vnd.github+json', 'X-GitHub-Api-Version': '2022-11-28'},
        signal: AbortSignal.timeout(30000)
      });
      if (!response.ok) throw Error(`GitHub API HTTP ${response.status}; saved data unchanged`);
      batch = await response.json();
    } else {
      try { batch = JSON.parse(execFileSync('gh', ['api', endpoint], {encoding:'utf8', stdio:['ignore','pipe','pipe']})); }
      catch { throw Error('GitHub read failed. Authenticate gh or set GH_TOKEN; saved data unchanged'); }
    }
    if (!Array.isArray(batch)) throw Error('Unexpected GitHub response');
    result.push(...batch);
    if (batch.length < 100) return result;
  }
}

export async function collect(config, list = githubList) {
  const base = `repos/${config.repository}`;
  const prs = (await list(`${base}/pulls?state=all`)).filter(p => p.user?.login?.toLowerCase() === config.author.toLowerCase());
  const events = [];
  for (const pr of prs) {
    for (const [kind, endpoint] of [['review','reviews'], ['comment','comments']]) {
      for (const item of await list(`${base}/pulls/${pr.number}/${endpoint}`)) {
        if (!item.user || (kind === 'review' && (!item.submitted_at || item.state === 'PENDING'))) continue;
        events.push({kind, id: item.id, userId: item.user.id, login: item.user.login, bot: item.user.type === 'Bot', at: kind === 'review' ? item.submitted_at : item.created_at});
      }
    }
  }
  return events;
}
