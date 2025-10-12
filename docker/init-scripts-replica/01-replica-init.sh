#!/bin/bash
set -ex

# Windows + Docker 볼륨 문제로, 중간 디렉토리 없이 직접 초기화
REPLICA_DATA="/var/lib/postgresql/data"

# 기존 데이터가 남아있으면 삭제
echo "Cleaning replica data directory..."
rm -rf "$REPLICA_DATA"/*

echo "Initializing replica from primary..."

# 비밀번호 환경 변수 설정
export PGPASSWORD="replica_password"

# pg_basebackup로 데이터 가져오기
pg_basebackup -h ms-postgres-source -D "$REPLICA_DATA" -U replica_user -P --wal-method=stream

# standby.signal 생성
touch "$REPLICA_DATA/standby.signal"

# primary 연결 정보 작성
cat >> "$REPLICA_DATA/postgresql.conf" <<EOF
primary_conninfo = 'host=ms-postgres-source port=5432 user=replica_user password=replica_password'
EOF

echo "Replica initialization complete."
