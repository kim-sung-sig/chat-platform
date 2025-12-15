#!/bin/bash
set -e

echo "=== Starting Replica Setup ==="

PGDATA="${PGDATA:-/var/lib/postgresql/data}"

# PGDATA가 비어있거나 초기화되지 않은 경우
if [ ! -f "$PGDATA/PG_VERSION" ]; then
    echo "Initializing replica from primary..."

    export PGPASSWORD="${REPLICA_SOURCE_PASSWORD}"

    # pg_basebackup으로 복제
    pg_basebackup -h "${REPLICA_SOURCE_HOST}" \
                  -p "${REPLICA_SOURCE_PORT}" \
                  -U "${REPLICA_SOURCE_USER}" \
                  -D "$PGDATA" \
                  -Fp -Xs -P -R

    echo "Replica initialization complete!"
else
    echo "Replica already initialized (found PG_VERSION)"
fi

echo "=== Starting PostgreSQL ==="
exec docker-entrypoint.sh postgres
