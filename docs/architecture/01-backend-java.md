# 01 - Java 后端框架

## 1. 目标与边界
- 提供统一业务能力：信息抓取、规则触发、消息提醒、打卡管理。
- 保证可扩展：新增工具模块时不侵入已有模块。

## 2. 技术选型
- JDK：17
- 框架：Spring Boot 3.x
- 构建：Maven 多模块
- 存储：MySQL + Redis + Elasticsearch
- 异步：RabbitMQ
- 调度：XXL-JOB

## 3. 模块划分
- `app-gateway`：网关层
- `app-bff-c`：C 端聚合
- `app-bff-admin`：管理端聚合
- `app-core`：领域核心，内部再拆
  - `iam`
  - `radar`
  - `rule-engine`
  - `notification`
  - `tool-habit`
  - `platform`

## 4. 分层规范
- `controller`：入参校验与协议转换。
- `application`：用例编排。
- `domain`：核心业务规则。
- `infrastructure`：DB/MQ/第三方适配。
- 约束：跨模块调用仅允许 `application` 层公开接口，禁止跨模块 DAO 调用。

## 5. 接口与契约
- 对外 REST：统一响应结构 `code/message/data/traceId`。
- 内部事件：统一 envelope 字段
  - `eventId`
  - `eventType`
  - `occurredAt`
  - `traceId`
  - `actorId`
  - `payload`
  - `version`

## 6. 安全与权限
- 鉴权：JWT + Refresh Token。
- 授权：RBAC（用户-角色-权限点）。
- 审计：关键配置变更必须入审计日志。

## 7. MVP 验收标准
- 单体服务可独立部署。
- 模块之间无循环依赖。
- 核心链路有集成测试覆盖。
