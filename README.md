# Trust Kill-Switch — Interview Project

Spring Boot 3.4 / Java 21 / PostgreSQL 16 + Flyway service bootstrapped for tasks 2 and 3.

## Quick Start

```sh
./scripts/start.sh
```

Installs `mprocs` with Homebrew when it is not already on `PATH`, then starts `mprocs` from the repo root.

> For the sake of time, please start Docker on the day of the interview and ensure no other containers are running, especially Postgres.

`mprocs.yaml` starts:
- `postgres` — PostgreSQL 16 via `docker compose up` (port `5432`)
- `app` — waits for Postgres TCP `5432` + `trust_db`, builds `bootJar`, then runs `java -jar` (port `8080`)
- `tests` — runs `./gradlew test` with Testcontainers on start and stays up (press `Enter` in the pane or `r` in `mprocs` to re-run)
- `psql` — waits for Postgres, then opens an interactive `psql` shell into `trust_db`
- `shell` — interactive terminal

Verify it's running:

```sh
curl -s http://localhost:8080/v1/projects | jq
```

## Pre-Seeded Projects (`V2__seed_projects.sql`)

| Project ID | Account Type | Trust Level | Blocked | Use For |
|---|---|---|---|---|
| `proj-cloud-limited` | `CLOUD` | `LIMITED` | `false` | Happy path `POST /block` |
| `proj-cloud-unverified` | `CLOUD` | `UNVERIFIED` | `false` | Block from `UNVERIFIED` |
| `proj-cloud-trusted` | `CLOUD` | `TRUSTED` | `false` | Block from `TRUSTED` |
| `proj-enterprise-exempt` | `ENTERPRISE` | `EXEMPT` | `false` | Reject block (`409 Conflict`) |
| `proj-already-blocked` | `CLOUD` | `NON_COMPLIANT` | `true` | Already-blocked contract check |
| `proj-high-volume` | `CLOUD` | `LIMITED` | `false` | Part 3 precedence / outbox fan-out |

## Caller Identity Convention (Part 2)
No auth/JWT setup is required — read the operator identity from an `X-Caller-Id` request header (and require it to be non-blank).
