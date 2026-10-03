# Review Zoo

- Entry point: `node scripts/review-zoo/index.mjs` (offline render).
- Read-only GitHub refresh: `node scripts/review-zoo/index.mjs --sync` (`GH_TOKEN` or `gh` authentication).
- Checks: `node --test scripts/review-zoo/*.test.mjs`.
- No npm dependencies. Node 22+ standard library only.
- Preserve README's first banner and content outside generated markers.
- Only inline review comments grant XP. Never persist review bodies, credentials or email addresses.
- Round boundaries and per-round aggregates live in assets/review-zoo/state.json; retain it with generated SVG and README when publishing.
