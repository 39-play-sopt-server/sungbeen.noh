#!/usr/bin/env bash
set -euo pipefail
base=/home/ubuntu/assignment
config=/home/ubuntu/.config/assignment
mkdir -p "$base" "$config"
chmod 700 "$config"
# Preserve the previous release for recovery if the new application fails its health check.
if [[ -f "$base/assignment.jar" && -f "$config/production.env" ]]; then
  cp "$base/assignment.jar" "$base/assignment.jar.previous"
  cp "$config/production.env" "$config/production.env.previous"
fi
chmod 600 "$config/production.env.next"
mv "$base/assignment.jar.next" "$base/assignment.jar"
mv "$config/production.env.next" "$config/production.env"
if sudo systemctl restart assignment; then
for attempt in {1..30}; do
  if curl -fsS --max-time 3 http://127.0.0.1:8080/api/v1/posts >/dev/null; then
    echo 'Deployment health check passed.'
    exit 0
  fi
  sleep 2
done
fi
echo 'Health check failed; restoring previous release if available.' >&2
if [[ -f "$base/assignment.jar.previous" && -f "$config/production.env.previous" ]]; then
  mv "$base/assignment.jar.previous" "$base/assignment.jar"
  mv "$config/production.env.previous" "$config/production.env"
  sudo systemctl restart assignment
fi
exit 1
