#!/bin/sh
# 인증서 갱신 후 Nginx 컨테이너에서 새 인증서 사용
set -eu
/usr/bin/docker compose -f /home/ubuntu/assignment/compose.yml exec -T nginx nginx -s reload
