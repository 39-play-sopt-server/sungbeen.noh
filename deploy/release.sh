#!/usr/bin/env bash
set -euo pipefail

base=/home/ubuntu/assignment
config=/home/ubuntu/.config/assignment
cd "$base"
trap 'rm -f release/assignment.tar release/compose.env' EXIT

# 배포 전에 기존 이미지와 설정을 보관
sudo docker image inspect assignment:current >/dev/null
sudo docker tag assignment:current assignment:previous
cp compose.yml compose.yml.previous
cp deploy/nginx.conf deploy/nginx.conf.previous
cp "$config/compose.env" "$config/compose.env.previous"

sudo docker load -i release/assignment.tar
sudo docker image inspect assignment:candidate --format '{{.Architecture}}' | grep -qx arm64
sudo docker tag assignment:candidate assignment:current
install -m 600 release/compose.env "$config/compose.env"
cp release/compose.yml compose.yml
cp release/nginx.conf deploy/nginx.conf

# 새 컨테이너가 정상적으로 응답하지 않으면 직전 구성으로 복구
if sudo docker compose -f compose.yml up -d --force-recreate; then
    for attempt in {1..30}; do
        if curl -fsS --max-time 3 --resolve sopt-39-sungbeen.p-e.kr:443:127.0.0.1 https://sopt-39-sungbeen.p-e.kr/api/v1/posts >/dev/null; then
            echo '배포 후 API 응답 확인 완료'
            exit 0
        fi
        sleep 2
    done
fi

echo '배포 실패: 이전 이미지와 설정으로 복구' >&2
sudo docker tag assignment:previous assignment:current
mv compose.yml.previous compose.yml
mv deploy/nginx.conf.previous deploy/nginx.conf
mv "$config/compose.env.previous" "$config/compose.env"
sudo docker compose -f compose.yml up -d --force-recreate
exit 1
