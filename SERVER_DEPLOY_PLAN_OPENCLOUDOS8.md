# Xiaoji 生产部署方案（OpenCloudOS 8 + Docker 26）

适用时间：2026-03  
适用环境：OpenCloudOS8-Docker26 镜像（已安装 Docker 26）  
代码仓库：GitHub（私有或公开均可）

---

## 1. 先给结论：服务器直接拉代码，还是 CI 自动发版？

### 1.1 当前阶段建议（你现在就能用）

优先用 **“服务器拉代码 + 服务器本机构建 + docker compose 发布”**。

原因：
- 你当前已经有可用脚本：`scripts/deploy/prod-frontend-build.sh`、`prod-build.sh`、`prod-up.sh`。
- 后端是 Docker 多阶段构建，服务器不必装本地 JDK/Maven 才能跑 Java。
- 改动上线路径短，适合当前产品快速迭代阶段。

### 1.2 稳定后建议（下一阶段）

升级为 **“GitHub Actions 自动部署（SSH 到服务器执行部署脚本）”**。  
再下一步可以演进到 **“CI 构建镜像并推送镜像仓库，服务器只 pull 镜像”**。

---

## 2. 当前生产架构（按你现状）

- 应用容器：
  - `xiaoji-backend`（Spring Boot，端口 8080，容器内）
  - `xiaoji-nginx`（对外入口）
- 数据服务：
  - MySQL（外置，已部署）
  - Redis（外置，已部署）
- 编排文件：
  - `infra/deploy/prod/docker-compose.yml`
- Nginx 前端托管端口：
  - `80`：admin-web
  - `8081`：admin-h5
  - `8082`：official-h5
  - `8083`：mobile-app（uni-app H5）

---

## 3. 服务器环境基线（OpenCloudOS8）

## 3.1 必备软件

- Docker 26（已具备）
- Docker Compose v2（命令为 `docker compose`）
- Git
- Bash
- Node.js 20 LTS + npm（用于前端构建）

> 说明：  
> 后端运行在容器里，默认不要求宿主机安装 JDK。  
> 仅当你要“宿主机直接运行 jar”时才需要 JDK（建议 Temurin/OpenJDK 8，与当前后端镜像一致）。

## 3.2 快速检查命令

```bash
docker -v
docker compose version
git --version
bash --version
node -v
npm -v
```

---

## 4. 首次部署（手工版，推荐先跑通）

## 4.1 目录规划（示例）

```bash
mkdir -p /data/apps
cd /data/apps
git clone <你的GitHub仓库地址> xiaoji
cd /data/apps/xiaoji
```

## 4.2 生产配置

```bash
cp infra/deploy/prod/.env.example infra/deploy/prod/.env
```

重点确认 `infra/deploy/prod/.env`：
- `MYSQL_URL` 指向 `xiaoji` 库
- `MYSQL_USERNAME` / `MYSQL_PASSWORD`
- `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD`
- `JAVA_OPTS`（按机器规格调整）

建议（2C8G）：
- `JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai`

## 4.3 构建并启动

```bash
# 1) 构建四端前端并同步到 nginx 挂载目录
bash scripts/deploy/prod-frontend-build.sh

# 2) 构建后端镜像
bash scripts/deploy/prod-build.sh

# 3) 启动服务
bash scripts/deploy/prod-up.sh
```

## 4.4 验证

```bash
curl http://127.0.0.1/api/health
docker compose --env-file infra/deploy/prod/.env -f infra/deploy/prod/docker-compose.yml ps
docker compose --env-file infra/deploy/prod/.env -f infra/deploy/prod/docker-compose.yml logs -f --tail=200 backend
```

---

## 5. Nginx 部署说明（当前项目）

当前采用 **容器化 Nginx**，配置文件：
- `infra/deploy/prod/nginx/default.conf`

特点：
- `/api/` 反代到 `backend:8080`
- 四个前端站点分端口托管
- `try_files ... /index.html` 支持前端路由刷新

## 5.1 域名与 HTTPS 建议

推荐生产接入方式：
- 方案 A：云负载均衡/网关做 TLS 终止，再回源到本机 80/8081/8082/8083
- 方案 B：Nginx 容器内挂证书，监听 443（需改 compose 与 nginx 配置）

如果暂时没有域名，先用 IP + 端口可运行。

---

## 6. 自动部署方案（推荐落地版）

目标：`main` 分支有新提交后，自动在服务器执行更新。

## 6.1 服务器准备

在服务器新增部署脚本 `/data/apps/xiaoji/deploy/deploy-prod.sh`：

```bash
#!/usr/bin/env bash
set -euo pipefail

cd /data/apps/xiaoji
git fetch origin
git checkout main
git pull --ff-only origin main

bash scripts/deploy/prod-frontend-build.sh
bash scripts/deploy/prod-build.sh
bash scripts/deploy/prod-up.sh

curl -fsS http://127.0.0.1/api/health
echo "deploy success"
```

赋权：

```bash
chmod +x /data/apps/xiaoji/deploy/deploy-prod.sh
```

## 6.2 GitHub Actions 工作流（示例）

文件：`.github/workflows/deploy-prod.yml`

```yaml
name: deploy-prod
on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy via SSH
        uses: appleboy/ssh-action@v1.2.0
        with:
          host: ${{ secrets.PROD_HOST }}
          username: ${{ secrets.PROD_USER }}
          key: ${{ secrets.PROD_SSH_KEY }}
          port: ${{ secrets.PROD_SSH_PORT }}
          script: |
            /data/apps/xiaoji/deploy/deploy-prod.sh
```

GitHub Secrets 需要配置：
- `PROD_HOST`
- `PROD_USER`
- `PROD_SSH_KEY`
- `PROD_SSH_PORT`

## 6.3 这个方案的优缺点

优点：
- 上手最快，和你当前脚本完全兼容
- 不需要先搭建镜像仓库流程

缺点：
- 构建压力在服务器（CPU/内存占用）
- 发布速度受服务器网络与编译速度影响

---

## 7. 自动部署升级版（后续）

当版本稳定后建议升级到：
- GitHub Actions 构建后端镜像并推送到 GHCR/阿里云镜像仓库
- 服务器执行 `docker compose pull && docker compose up -d`
- 前端改为 CI 产物上传到对象存储/CDN（或 rsync 到服务器）

好处：
- 服务器负载更低
- 可追踪镜像版本，回滚更稳

---

## 8. 回滚方案（必须准备）

建议每次发布前打 Git tag（例如 `release-2026-03-26-01`）。

回滚步骤：

```bash
cd /data/apps/xiaoji
git fetch --tags
git checkout <历史稳定tag>
bash scripts/deploy/prod-frontend-build.sh
bash scripts/deploy/prod-build.sh
bash scripts/deploy/prod-up.sh
```

---

## 9. 运维与安全基线

- `.env` 严禁提交到 GitHub。
- 只开放必要端口（80/8081/8082/8083；若上 HTTPS 则 443）。
- SSH 使用密钥登录，禁用密码登录（建议）。
- 为 Docker 日志设置轮转，避免磁盘被打满。
- MySQL/Redis 建议开启定时备份。
- 每次发布执行健康检查：`/api/health`。

---

## 10. 资源建议（结合你现有机器）

- 主机（2C8G10M）：
  - 运行 `backend + nginx + mysql + redis` 没问题（当前阶段足够）
- 副机（2C2G3M）：
  - 暂不放主业务，优先预留给后续 `XXL-JOB/MQ/监控` 或 CI Runner

当前阶段先保持单主机方案最稳，等 XXL-JOB/MQ 接入后再做拆分。

