# Scripts

自动化脚本目录（构建、发布、初始化、检查）。
## Deploy Scripts

- `deploy/prod-build.sh`：构建后端生产镜像。
  默认使用 `xiaoji-maven-repo-cache` 持久化 Maven 依赖缓存，避免重复下载。
- `deploy/prod-frontend-build.sh`：构建前端并同步 `frontend-dist`。
- `deploy/prod-up.sh`：启动生产编排（后台）。
- `deploy/prod-down.sh`：停止生产编排。

## Infra Scripts

- `infra/local-up.sh`：一键启动本地基础设施（支持 `base/with-es/with-mq/full`）。
- `infra/local-down.sh`：停止本地基础设施。
- `infra/local-logs.sh`：查看本地基础设施日志。
