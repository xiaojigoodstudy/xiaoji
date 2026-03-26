#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DEPLOY_FRONT_DIR="$ROOT_DIR/infra/deploy/prod/frontend-dist"

build_and_copy() {
  local project_dir="$1"
  local target_dir="$2"
  local build_script="${3:-build}"
  local output_rel_dir="${4:-dist}"

  npm --prefix "$project_dir" run "$build_script"
  mkdir -p "$target_dir"
  rm -rf "${target_dir:?}/"*
  cp -a "$project_dir/$output_rel_dir/." "$target_dir/"
}

mkdir -p "$DEPLOY_FRONT_DIR"

build_and_copy "$ROOT_DIR/frontend/admin-web-vue3" "$DEPLOY_FRONT_DIR/admin-web"
build_and_copy "$ROOT_DIR/frontend/admin-h5-vue3" "$DEPLOY_FRONT_DIR/admin-h5"
build_and_copy "$ROOT_DIR/frontend/wechat-official-h5" "$DEPLOY_FRONT_DIR/official-h5"
build_and_copy "$ROOT_DIR/frontend/mobile-app" "$DEPLOY_FRONT_DIR/mobile-app" "build:h5" "dist/build/h5"

echo "Frontend dist artifacts synced to $DEPLOY_FRONT_DIR"
