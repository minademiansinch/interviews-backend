# Trust Kill-Switch — Interview Project

### Background
Sinch's messaging gateway checks a project's trust/compliance state on every message send (~100k req/s).

Two independent sources mutate a project's trust state:

1. **Automated lifecycle events** (e.g., billing/account status changes arriving asynchronously via Kafka) continuously recompute trust tiers (`UNVERIFIED`, `LIMITED`, `TRUSTED`). Enterprise projects are `EXEMPT` and cannot be blocked.
2. **Compliance operators** can manually issue a permanent `BLOCK` on a fraudulent `CLOUD` project, transitioning it to `NON_COMPLIANT` and triggering downstream enforcement:
   - Invalidating / updating the messaging gateway's enforcement state so traffic stops immediately
   - Recording an immutable audit entry
   - Reporting the fraud block to an external compliance webhook

## Quick Start

```sh
chmod +x ./scripts/start.sh
./scripts/start.sh
```

> For the sake of time, please start Docker on the day of the interview and ensure no other containers are running, especially Postgres.

`mprocs.yaml` starts:
- `postgres` — PostgreSQL 16 via `docker compose up` (port `5432`)
- `app` — waits for Postgres TCP `5432` + `trust_db`, builds `bootJar`, then runs `java -jar` (port `8080`)
- `tests` — runs `./gradlew test` with Testcontainers on start and stays up (press `Enter` in the pane or `r` in `mprocs` to re-run)
- `psql` — waits for Postgres, then opens an interactive `psql` shell into `trust_db`
- `shell` — interactive terminal

### 3. Quick Sanity Check

Verify it's running:

```sh
# List seeded projects
curl -s http://localhost:8080/v1/projects | jq

# Single project lookup
curl -s http://localhost:8080/v1/projects/proj-cloud-limited | jq

# Check tables in psql (or use the psql pane in mprocs)
SELECT id, account_type, trust_level, blocked, version FROM projects;
SELECT * FROM block_audit;
```
---

## Pre-Seeded Projects (`V2__seed_projects.sql`)

| Project ID | Account Type | Trust Level | Blocked | Use For |
|---|---|---|---|---|
| `proj-cloud-limited` | `CLOUD` | `LIMITED` | `false` | Happy path `POST /block` |
| `proj-cloud-unverified` | `CLOUD` | `UNVERIFIED` | `false` | Block from `UNVERIFIED` |
| `proj-cloud-trusted` | `CLOUD` | `TRUSTED` | `false` | Block from `TRUSTED` |
| `proj-enterprise-exempt` | `ENTERPRISE` | `EXEMPT` | `false` | Reject block (`409 Conflict`) |
| `proj-already-blocked` | `CLOUD` | `NON_COMPLIANT` | `true` | Already-blocked contract check |
| `proj-high-volume` | `CLOUD` | `LIMITED` | `false` | Part 3 precedence / outbox fan-out |
