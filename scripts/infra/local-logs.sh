#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INFRA_DIR="$ROOT_DIR/infra/local"
ENV_FILE="$INFRA_DIR/.env"
COMPOSE_FILE="$INFRA_DIR/docker-compose.yml"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "$ENV_FILE not found."
  exit 1
fi

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f --tail 200
