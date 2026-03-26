# Local Infra One-Click

本地基础设施一键编排，支持：

- 基础：MySQL + Redis
- 可选：Elasticsearch（`es` profile）
- 可选：RabbitMQ（`mq` profile）

## 准备

1. `cp .env.example .env`
2. 按需修改端口和密码。

## 启动

- 仅基础：`bash scripts/infra/local-up.sh`
- 基础 + ES：`bash scripts/infra/local-up.sh with-es`
- 基础 + MQ：`bash scripts/infra/local-up.sh with-mq`
- 全部：`bash scripts/infra/local-up.sh full`

## 查看和停止

- 日志：`bash scripts/infra/local-logs.sh`
- 停止：`bash scripts/infra/local-down.sh`
