#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DEPLOY_DIR="$ROOT_DIR/infra/deploy/prod"
ENV_FILE="$DEPLOY_DIR/.env"
ENV_EXAMPLE="$DEPLOY_DIR/.env.example"
MAVEN_IMAGE="${MAVEN_IMAGE:-maven:3.8.8-eclipse-temurin-8}"
MAVEN_CACHE_VOLUME="${MAVEN_CACHE_VOLUME:-xiaoji-maven-repo-cache}"
MAVEN_CONFIG_FILE="$ROOT_DIR/.mvn/maven.config"
MAVEN_CONFIG_BACKUP=""

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo "Created $ENV_FILE from template. Please verify credentials."
fi

restore_maven_config() {
  if [[ -n "$MAVEN_CONFIG_BACKUP" && -f "$MAVEN_CONFIG_BACKUP" ]]; then
    mv "$MAVEN_CONFIG_BACKUP" "$MAVEN_CONFIG_FILE"
  fi
}

if [[ -f "$MAVEN_CONFIG_FILE" ]] && grep -q '^--settings ' "$MAVEN_CONFIG_FILE"; then
  MAVEN_CONFIG_BACKUP="$MAVEN_CONFIG_FILE.prod-build.bak"
  cp "$MAVEN_CONFIG_FILE" "$MAVEN_CONFIG_BACKUP"
  trap restore_maven_config EXIT
  sed -i '/^--settings /d' "$MAVEN_CONFIG_FILE"
fi

docker volume create "$MAVEN_CACHE_VOLUME" >/dev/null
docker run --rm \
  -v "$ROOT_DIR:/workspace" \
  -v "$MAVEN_CACHE_VOLUME:/root/.m2" \
  -w /workspace \
  "$MAVEN_IMAGE" \
  mvn -q -DskipTests -pl backend/app-bootstrap -am package

docker compose --env-file "$ENV_FILE" -f "$DEPLOY_DIR/docker-compose.yml" build backend
echo "Backend image build finished. Maven cache volume: $MAVEN_CACHE_VOLUME"
