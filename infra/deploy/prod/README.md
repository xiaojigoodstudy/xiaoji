# Production Deploy

## Current Scope

- Included: `backend + nginx`
- Included frontend static hosting via nginx:
  - `admin-web` on `:80`
  - `admin-h5` on `:8081`
  - `official-h5` on `:8082`
  - `mobile-app` on `:8083`
- External dependencies: MySQL, Redis
- Deferred: Elasticsearch, XXL-JOB, MQ

## Files

- `docker-compose.yml`: runtime orchestration
- `.env.example`: environment template
- `nginx/default.conf`: reverse proxy config

## Quick Start (Linux)

1. `cp .env.example .env`
2. Edit `.env` and verify MySQL/Redis credentials.
3. From repo root, run `bash scripts/deploy/prod-frontend-build.sh`
4. Run `bash scripts/deploy/prod-build.sh`
5. Run `bash scripts/deploy/prod-up.sh`
6. Check `curl http://<server-ip>/api/health`

## With Existing MySQL/Redis Compose

If MySQL/Redis are already running on the same host in another compose project:

- keep `MYSQL_URL` host as `host.docker.internal`
- keep `REDIS_HOST=host.docker.internal`
- ensure MySQL `3306` and Redis `6379` are published to host
- backend compose already injects `extra_hosts: host.docker.internal:host-gateway`

## Two-Server Suggestion

- Main server (2C8G10M): deploy this compose stack.
- Secondary server (2C2G3M): reserve for future XXL-JOB/MQ/monitoring.
