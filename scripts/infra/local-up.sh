#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INFRA_DIR="$ROOT_DIR/infra/local"
ENV_FILE="$INFRA_DIR/.env"
ENV_EXAMPLE="$INFRA_DIR/.env.example"
COMPOSE_FILE="$INFRA_DIR/docker-compose.yml"

MODE="${1:-base}"
PROFILE_ARGS=()

case "$MODE" in
  base)
    ;;
  with-es)
    PROFILE_ARGS+=(--profile es)
    ;;
  with-mq)
    PROFILE_ARGS+=(--profile mq)
    ;;
  full)
    PROFILE_ARGS+=(--profile es --profile mq)
    ;;
  *)
    echo "Usage: bash scripts/infra/local-up.sh [base|with-es|with-mq|full]"
    exit 1
    ;;
esac

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo "Created $ENV_FILE from template. Please verify credentials."
fi

mkdir -p "$INFRA_DIR/volumes/mysql" "$INFRA_DIR/volumes/redis" "$INFRA_DIR/volumes/es" "$INFRA_DIR/volumes/rabbitmq"

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "${PROFILE_ARGS[@]}" up -d
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "${PROFILE_ARGS[@]}" ps
