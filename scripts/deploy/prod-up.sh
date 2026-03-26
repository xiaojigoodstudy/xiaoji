#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DEPLOY_DIR="$ROOT_DIR/infra/deploy/prod"
ENV_FILE="$DEPLOY_DIR/.env"
ENV_EXAMPLE="$DEPLOY_DIR/.env.example"
FRONTEND_DIR="$DEPLOY_DIR/frontend-dist"

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo "Created $ENV_FILE from template. Please verify credentials."
fi

for app in admin-web admin-h5 official-h5 mobile-app; do
  if [[ ! -f "$FRONTEND_DIR/$app/index.html" ]]; then
    echo "Warning: missing $FRONTEND_DIR/$app/index.html"
    echo "Run: bash scripts/deploy/prod-frontend-build.sh"
  fi
done

docker compose --env-file "$ENV_FILE" -f "$DEPLOY_DIR/docker-compose.yml" up -d
docker compose --env-file "$ENV_FILE" -f "$DEPLOY_DIR/docker-compose.yml" ps
