#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

echo "[app] Waiting for Postgres (TCP 127.0.0.1:5432 + trust_db)..."
until docker compose exec -T postgres pg_isready -h 127.0.0.1 -p 5432 -U trust -d trust_db >/dev/null 2>&1 \
  && docker compose exec -T postgres psql -U trust -d trust_db -tAc "SELECT 1" >/dev/null 2>&1; do
  sleep 1
done

echo "[app] Postgres is ready. Starting Spring Boot on :8080..."
exec ./gradlew --no-daemon bootRun --console=plain
