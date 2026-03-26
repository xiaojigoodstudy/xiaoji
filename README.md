# Daily Toolkit

个人向三端兼容工具集（微信小程序/公众号/App）项目骨架。

## 当前状态
- 已完成架构文档：`docs/architecture`
- 已初始化后端 Maven 多模块骨架：`backend`
- 已创建前端与基础设施目录：`frontend`、`infra`、`scripts`

## 快速开始
- 编译后端：`mvn -q -DskipTests compile`
- 启动主程序（后续补充配置后）：`mvn -pl backend/app-bootstrap spring-boot:run`

## 生产部署（当前可用）

1. 复制配置模板：`cp infra/deploy/prod/.env.example infra/deploy/prod/.env`
2. 构建前端静态资源：`bash scripts/deploy/prod-frontend-build.sh`
3. 构建后端镜像：`bash scripts/deploy/prod-build.sh`
4. 启动服务：`bash scripts/deploy/prod-up.sh`
5. 验证健康：`curl http://<server-ip>/api/health`

说明：
- 当前编排仅包含 `backend + nginx`，MySQL/Redis 使用外置实例。
- 如果 MySQL/Redis 已在同机其他 compose 中运行，`.env` 使用 `host.docker.internal` 即可联通。
- 前端静态入口端口：
  - `80`：admin-web
  - `8081`：admin-h5
  - `8082`：wechat-official-h5
  - `8083`：mobile-app
- ES/XXL-JOB/MQ 预留后续接入。

## 多环境模板

- 应用配置模板：`backend/app-bootstrap/src/main/resources/application-dev.yml`、`application-test.yml`、`application-prod.yml`
- 环境变量模板：`infra/deploy/env/.env.dev.example`、`.env.test.example`、`.env.prod.example`

## 本地基础设施一键启动

- 配置：`cp infra/local/.env.example infra/local/.env`
- 启动基础（MySQL+Redis）：`bash scripts/infra/local-up.sh`
- 启动基础 + ES：`bash scripts/infra/local-up.sh with-es`
- 启动基础 + MQ：`bash scripts/infra/local-up.sh with-mq`
- 启动全部：`bash scripts/infra/local-up.sh full`
- 查看日志：`bash scripts/infra/local-logs.sh`
- 停止：`bash scripts/infra/local-down.sh`
