#!/bin/bash
set -e

PGDATA="/var/lib/postgresql/data"
ARCHIVE_DIR="$PGDATA/archive"

# 아카이브 디렉토리 생성
mkdir -p "$ARCHIVE_DIR"
chown -R postgres:postgres "$ARCHIVE_DIR"

# postgresql.conf에 설정 추가/수정
cat >> "$PGDATA/postgresql.conf" <<EOF
# Replication Settings
wal_level = replica
archive_mode = on
archive_command = 'cp %p $ARCHIVE_DIR/%f'
max_wal_senders = 10
max_replication_slots = 10
hot_standby = on
EOF

# pg_hba.conf에 레플리케이션 접근 허용 추가
cat >> "$PGDATA/pg_hba.conf" <<EOF
# Replication connections
host    replication     replica_user    0.0.0.0/0               scram-sha-256
host    replication     replica_user    172.0.0.0/8             scram-sha-256
EOF

# PostgreSQL 재시작을 위한 시그널
pg_ctl reload -D "$PGDATA" || true
