# 生产部署与更新手册（A/B 双机）

最后更新：2026-03-26

## 1. 部署拓扑
- 服务器 A（应用）：`10.1.4.12`（公网：`42.193.180.66`）
  - 部署：`backend + nginx + 前端静态资源`
  - 目录：`/data/apps/xiaoji`
- 服务器 B（数据）：`10.1.0.9`
  - 部署：`MySQL + Redis`

## 2. 首次部署步骤
1. 在 A 拉代码并进入目录

```bash
cd /data/apps
git clone https://github.com/xiaojigoodstudy/xiaoji.git
cd /data/apps/xiaoji
```

2. 配置生产环境变量（A）

```bash
cd /data/apps/xiaoji/infra/deploy/prod
cp .env.example .env
```

关键参数确认：
- `MYSQL_URL=jdbc:mysql://10.1.0.9:3306/xiaoji?...`
- `MYSQL_USERNAME` / `MYSQL_PASSWORD`
- `REDIS_HOST=10.1.0.9`
- `REDIS_PASSWORD`
- `JAVA_OPTS=-Xms384m -Xmx1024m ...`
- `NGINX_HTTP_PORT`、`NGINX_ADMIN_H5_PORT`、`NGINX_OFFICIAL_H5_PORT`、`NGINX_MOBILE_APP_PORT`

3. 检查 A 到 B 连通性

```bash
nc -zv 10.1.0.9 3306
nc -zv 10.1.0.9 6379
```

4. 构建前端静态资源（A）

```bash
cd /data/apps/xiaoji
npm --prefix frontend/admin-web-vue3 ci --include=dev
npm --prefix frontend/admin-h5-vue3 ci --include=dev
npm --prefix frontend/wechat-official-h5 ci --include=dev
npm --prefix frontend/mobile-app ci --include=dev
bash scripts/deploy/prod-frontend-build.sh
```

5. 构建后端镜像并启动（A）

```bash
cd /data/apps/xiaoji
bash scripts/deploy/prod-build.sh
bash scripts/deploy/prod-up.sh
```

6. 验证部署

```bash
docker compose --env-file /data/apps/xiaoji/infra/deploy/prod/.env -f /data/apps/xiaoji/infra/deploy/prod/docker-compose.yml ps
curl -i http://127.0.0.1:80/api/health
curl -i http://127.0.0.1:8081/api/health
```

公网验证（按实际端口）：
- `http://42.193.180.66/`
- `http://42.193.180.66:8081/`（或你改后的 `8001`）
- `http://42.193.180.66/api/health`

## 3. 后续更新方法（标准流程）
每次代码更新后，在 A 执行：

```bash
cd /data/apps/xiaoji
git pull
bash scripts/deploy/prod-frontend-build.sh
bash scripts/deploy/prod-build.sh
bash scripts/deploy/prod-up.sh
```

说明：
- `prod-build.sh` 已接入 Maven 缓存卷 `xiaoji-maven-repo-cache`，后续构建不会每次全量下载依赖。
- `Dockerfile` 已改为运行时镜像，仅拷贝 jar，构建更稳定更快。

## 4. 日常运维命令
查看状态：

```bash
docker compose --env-file /data/apps/xiaoji/infra/deploy/prod/.env -f /data/apps/xiaoji/infra/deploy/prod/docker-compose.yml ps
```

查看日志：

```bash
docker compose --env-file /data/apps/xiaoji/infra/deploy/prod/.env -f /data/apps/xiaoji/infra/deploy/prod/docker-compose.yml logs -n 200 backend
docker compose --env-file /data/apps/xiaoji/infra/deploy/prod/.env -f /data/apps/xiaoji/infra/deploy/prod/docker-compose.yml logs -n 200 nginx
```

重启服务：

```bash
docker compose --env-file /data/apps/xiaoji/infra/deploy/prod/.env -f /data/apps/xiaoji/infra/deploy/prod/docker-compose.yml restart backend nginx
```

停止服务：

```bash
bash scripts/deploy/prod-down.sh
```

## 5. 回滚方法（按提交回滚）

```bash
cd /data/apps/xiaoji
git log --oneline -n 20
git checkout <稳定提交ID>
bash scripts/deploy/prod-frontend-build.sh
bash scripts/deploy/prod-build.sh
bash scripts/deploy/prod-up.sh
```

## 6. 安全组与防火墙建议
- B（数据机）`3306/6379` 仅允许 A 内网 IP `10.1.4.12`。
- A（应用机）只放行业务端口（如 `80/8081/8082/8083` 或 `80/8001/8002/8003`）。
- 小程序/线上访问端口与 `.env` 保持一致，避免误判 502。

## 7. 本次出现的问题（已处理）
1. `vue-tsc: not found`
   - 原因：前端依赖未安装或 dev 依赖被省略。
   - 处理：对各前端项目执行 `npm ci --include=dev`。

2. Docker 拉基础镜像超时（`maven` / `eclipse-temurin` / `nginx`）
   - 原因：A 访问 DockerHub 不稳定。
   - 处理：先通过镜像站预拉并打本地标签，再继续构建/启动。

3. Maven 在 Docker 内构建失败（Windows settings 路径）
   - 报错：`The specified user settings file does not exist: /workspace/D:/...`
   - 原因：`.mvn/maven.config` 写死 Windows `--settings`。
   - 处理：构建脚本中临时移除该参数，构建后恢复；并改为 Maven 容器先打包。

4. 访问 502
   - 原因：访问端口与 Nginx 实际映射端口不一致（`8001` 与 `8081` 混用）。
   - 处理：统一 `.env` 端口并同步安全组放行，问题消失。

5. `compose.yml: command not found`
   - 原因：命令换行/粘贴错误，Shell 把 `compose.yml` 识别成独立命令。
   - 处理：使用完整单行命令执行 `docker compose --env-file ... -f ...`。
