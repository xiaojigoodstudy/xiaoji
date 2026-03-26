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

## Recommended Topology (Two Servers)

- Server A (app server): `4C4G / 3M / 40G`
  - deploy this compose stack (`backend + nginx + frontend-dist`)
- Server B (data server): `2C8G / 10M / 150G`
  - deploy `MySQL + Redis`
  - expose `3306/6379` only to Server A private IP

## Quick Start (Linux, Server A)

1. `cp .env.example .env`
2. Edit `.env`:
  - set `MYSQL_URL` to Server B private IP (for example `10.0.0.12`)
  - set `REDIS_HOST` to Server B private IP
3. From repo root, run `bash scripts/deploy/prod-frontend-build.sh`
4. Run `bash scripts/deploy/prod-build.sh`
5. Run `bash scripts/deploy/prod-up.sh`
6. Check `curl http://<server-a-ip>/api/health`

## Capacity Notes

- Server A (`4C4G`) memory target:
  - backend JVM: `-Xmx1024m`
  - nginx + docker + system remain about `2G` headroom
- Server B (`8G`) memory target:
  - MySQL: `1-2G` (`innodb_buffer_pool_size` around `2G`)
  - Redis: `512M-1G` (`maxmemory` per data size)

## Same-Host Fallback

If MySQL/Redis are on the same host in another compose project:

- use `MYSQL_URL` host as `host.docker.internal`
- use `REDIS_HOST=host.docker.internal`
- ensure MySQL `3306` and Redis `6379` are published to host
- compose already injects `extra_hosts: host.docker.internal:host-gateway`
