export const level = count => Math.min(3, 1 + Math.floor(count / 5));

export function validateConfig(c) {
  if (!/^[\w.-]+\/[\w.-]+$/.test(c.repository) || !/^[\w-]+$/.test(c.author)) throw Error('Invalid repository/author');
  if (!Number.isInteger(c.stage) || c.stage < 0 || c.stage > 8) throw Error('stage must be 0..8');
  if (!Number.isInteger(c.currentRound) || c.currentRound < 1 || c.currentRound > 3) throw Error('currentRound must be 1..3');
}

// Pure transition: no files change until all API reads and rendering have succeeded.
export function refresh(config, previous, events, now) {
  validateConfig(config);
  const state = structuredClone(previous);
  if (state.repository !== config.repository || state.author.toLowerCase() !== config.author.toLowerCase()) throw Error('State belongs to another repository/author');
  const last = state.rounds.at(-1);
  if (config.currentRound < last.number || config.currentRound > last.number + 1) throw Error('Advance one round at a time; rollback requires restoring saved state');
  if (config.currentRound !== last.number) state.rounds.push({number: config.currentRound, since: now, pets: []});
  const seen = new Set();
  for (const round of state.rounds) round.pets = [];
  for (const event of events) {
    const key = `${event.kind}:${event.id}`;
    if (seen.has(key)) continue;
    seen.add(key);
    if (!event.login || event.bot || event.login.toLowerCase() === config.author.toLowerCase()) continue;
    const at = Date.parse(event.at);
    if (!Number.isFinite(at)) throw Error('Invalid event timestamp');
    const round = state.rounds.findLast(r => at >= Date.parse(r.since));
    if (!round) continue;
    let pet = round.pets.find(p => p.id === event.userId);
    if (!pet) { pet = {id: event.userId, username: event.login, comments: 0, reviews: 0}; round.pets.push(pet); }
    pet.username = event.login;
    if (event.kind === 'comment') pet.comments++;
    else if (event.kind === 'review') pet.reviews++;
    else throw Error('Unknown event kind');
  }
  for (const round of state.rounds) round.pets.sort((a,b) => b.comments-a.comments || a.username.localeCompare(b.username));
  // Avoid a save commit every six hours when the public data has not changed.
  state.lastSyncedAt = previous.lastSyncedAt && JSON.stringify(state.rounds) === JSON.stringify(previous.rounds) ? previous.lastSyncedAt : now;
  return state;
}
