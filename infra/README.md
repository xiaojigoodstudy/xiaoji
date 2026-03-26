# Infra

基础设施编排目录（数据库、缓存、检索、消息队列等）。
## Deploy

- `deploy/prod/docker-compose.yml`：生产部署编排（`backend + nginx`）。
- `deploy/prod/.env.example`：生产环境变量模板（MySQL/Redis/Nginx）。
- `deploy/prod/nginx/default.conf`：反向代理配置。
- `deploy/prod/frontend-dist/`：前端静态文件挂载目录（由脚本生成）。
- `deploy/env/`：`dev/test/prod` 应用环境变量模板。

## Local Infra

- `local/docker-compose.yml`：本地基础设施一键编排（MySQL/Redis，可选 ES/MQ）。
- `local/.env.example`：本地基础设施配置模板。

> 当前部署方案默认外置 MySQL/Redis，ES/XXL-JOB/MQ 后续补充。
> 推荐双机部署：应用服务（`backend + nginx`）与数据服务（`MySQL + Redis`）分离，应用侧通过内网地址连接数据侧。
