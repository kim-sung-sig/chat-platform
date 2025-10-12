#!/bin/bash
set -e

PGDATA="/var/lib/postgresql/data"
ARCHIVE_DIR="$PGDATA/archive"

# 아카이브 디렉토리 생성
mkdir -p "$ARCHIVE_DIR"
chown -R postgres:postgres "$ARCHIVE_DIR"

# postgresql.conf에 설정 추가/수정
echo "wal_level = replica" >> "$PGDATA/postgresql.conf"
echo "archive_mode = on" >> "$PGDATA/postgresql.conf"
echo "archive_command = 'cp %p $ARCHIVE_DIR/%f'" >> "$PGDATA/postgresql.conf"
echo "max_wal_senders = 10" >> "$PGDATA/postgresql.conf"
echo "max_replication_slots = 10" >> "$PGDATA/postgresql.conf"

echo "host replication replica_user 0.0.0.0/0 md5" >> "$PGDATA/pg_hba.conf"

# 권한 설정
chown postgres:postgres "$PGDATA/postgresql.conf"