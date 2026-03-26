# TASK PROGRESS

最后更新：2026-03-26

## 1. 当前状态（已完成）
- 已完成架构文档基线：`docs/architecture/00~13`。
- 已完成仓库骨架目录：`backend/`、`frontend/`、`infra/`、`scripts/`。
- 已新增代码托管建议文档：`docs/architecture/13-code-hosting.md`。
- 已完成后端 Maven 多模块结构：
  - `app-bootstrap`
  - `app-gateway`
  - `app-bff-c`
  - `app-bff-admin`
  - `app-core`（含 `core-shared/core-iam/core-radar/core-rule-engine/core-notification/core-tool-habit/core-platform`）
- 已实现 `core-shared` 基础能力：
  - 统一响应 `ApiResponse`
  - 错误码 `ResultCode`
  - 业务异常 `BizException`
  - trace 上下文 `TraceContext`
- 已实现 `core-iam` 最小闭环：
  - 登录接口：`POST /api/iam/login`
  - 会话接口：`GET /api/iam/me`
  - 权限校验接口：`GET /api/iam/admin/ping`
- 已实现 `core-radar` 最小闭环：
  - 信息源：创建/列表/启停
  - 抓取任务：创建/列表/手动执行
- 已实现 `core-rule-engine` 最小闭环：
  - 规则：创建/列表/启停
  - 命中判定：`POST /api/rules/evaluate`
- 已实现 `core-notification` 最小闭环：
  - 通道抽象：`WECHAT`、`APP_PUSH`
  - 发送与日志：`POST /api/notifications/send`、`GET /api/notifications/logs`
- 已实现 `core-tool-habit` 最小闭环：
  - 事项：创建/列表
  - 打卡：按事项打卡（同用户同日去重）
  - 统计：`GET /api/habits/stats`
- 已打通主链路编排接口：
  - `POST /api/pipeline/radar-rule-notify`（radar -> rule -> notification）
- 已初始化前端三端可运行骨架：
  - `frontend/admin-web-vue3`（Vue3 + TS + Vite）
  - `frontend/admin-h5-vue3`（Vue3 + TS + Vite）
  - `frontend/wechat-official-h5`（Vue3 + TS + Vite）
  - `frontend/wechat-miniapp`（原生小程序骨架，含 index 页面）
  - `frontend/mobile-app`（已切换为 uni-app + Vue3 + TS）
- 已完成项目 Maven 独立 settings：
  - `D:\maven\apache-maven-3.8.3\conf\settings-xiaoji.xml`
  - 本地仓：`D:\maven\repo-xiaoji`
  - 项目硬绑定：`.mvn/maven.config`
- 已完成持久化改造拆分第 1 步：
  - 新增 `sql/` 目录与初始化脚本（`00_init_database.sql`、`01_iam.sql`、`02_radar.sql`、`03_rule_engine.sql`、`04_notification.sql`、`05_habit.sql`、`99_seed_dev.sql`）
- 已完成持久化改造第 2 步：
  - 后端已接入 MySQL/Flyway（`mysql` profile）
  - 应用可加载 `classpath:db/migration` 初始化脚本
- 已完成持久化改造第 3 步：
  - `core-iam` 已在 `mysql` profile 下切换为 MySQL 实现（`JdbcTemplate`）
  - 非 `mysql` profile 保持内存实现
- 已完成持久化改造第 4 步（MySQL 子步）：
  - `core-radar` 已在 `mysql` profile 下切换为 MySQL 实现（`JdbcTemplate`）
  - 非 `mysql` profile 保持内存实现
- 已完成持久化改造第 5 步：
  - `core-rule-engine` 已在 `mysql` profile 下切换为 MySQL 实现，并接入 Redis 评估指标缓存
  - `core-notification` 已在 `mysql` profile 下切换为 MySQL 实现，并接入 Redis 最近发送索引
  - `core-tool-habit` 已在 `mysql` profile 下切换为 MySQL 实现，并接入 Redis 当日打卡统计缓存
  - 以上模块在非 `mysql` profile 保持内存实现
- 已完成 Redis 基础接入（`mysql` profile）：
  - 已接入 `spring-boot-starter-data-redis`
  - Redis 连接配置已接入（默认 `42.194.225.228:6379`）
  - 健康接口已返回 Redis 状态（`/api/health` -> `redis: UP/DOWN/DISABLED`）
- 已完成生产部署基础包（当前阶段）：
  - 新增 `backend/app-bootstrap/Dockerfile`（可执行 jar 镜像）
  - 新增 `infra/deploy/prod/docker-compose.yml`（`backend + nginx`）
  - 新增 `infra/deploy/prod/.env.example` 与 Nginx 反向代理配置
  - 新增 `scripts/deploy/prod-build.sh`、`prod-up.sh`、`prod-down.sh`
- 已完成生产部署模板优化（双机场景）：
  - `infra/deploy/prod/.env.example` 默认改为“应用服务器 A 连接数据服务器 B（内网 IP）”
  - `infra/deploy/prod/README.md` 更新为双机部署建议（A: `4C4G3M40G`；B: `2C8G10M150G`）
  - `infra/README.md` 补充双机部署说明
- 已完成前端静态部署接入（当前阶段）：
  - `nginx` 已托管四个前端产物目录（`admin-web/admin-h5/official-h5/mobile-app`）
  - 新增 `scripts/deploy/prod-frontend-build.sh`（统一构建并同步前端 `dist`）
- 已完成移动端工程切换：
  - `frontend/mobile-app` 已从 Vue 壳替换为 uni-app 工程
  - 已新增 `dev:h5/dev:mp-weixin` 与 `build:h5/build:mp-weixin` 脚本
  - 生产前端构建脚本已适配 uni-app H5 产物目录（`dist/build/h5`）
- 已完成多环境配置模板：
  - 新增 `application-dev.yml`、`application-test.yml`、`application-prod.yml`
  - 新增 `infra/deploy/env/.env.dev.example`、`.env.test.example`、`.env.prod.example`
- 已完成本地基础设施一键脚本：
  - 新增 `infra/local/docker-compose.yml`（MySQL/Redis，可选 ES/MQ）
  - 新增 `scripts/infra/local-up.sh`、`local-down.sh`、`local-logs.sh`
- 已完成 JDK 切换脚本：
  - `mvn8.cmd`
  - `mvn17.cmd`
  - `mvnx.cmd`（统一入口）
- 已初始化 Git 仓库并完成第一次基线提交（含根级 `.gitignore`）。
- 验证通过：
  - `./mvnx.cmd 8 clean test` -> BUILD SUCCESS
  - `./mvnx.cmd 17 clean test` -> BUILD SUCCESS
  - `mysql` profile 启动后 `GET /api/health` -> 200
  - `mysql` profile 启动后 `GET /api/health` 返回 `redis: UP`
  - `mysql` profile 下 `iam/radar` 接口联调通过（登录、创建信息源、创建任务、手动执行）
  - `mysql` profile 下 `rules/notifications/habits` 接口联调通过（建规则、命中评估、发送通知、打卡与统计）
  - `npm --prefix frontend/admin-web-vue3 run build` -> SUCCESS
  - `npm --prefix frontend/admin-h5-vue3 run build` -> SUCCESS
  - `npm --prefix frontend/wechat-official-h5 run build` -> SUCCESS
  - `npm --prefix frontend/mobile-app run build` -> SUCCESS
  - `npm --prefix frontend/mobile-app run build:h5` -> SUCCESS

## 2. 当前约束/说明
- 默认 profile 下核心功能仍为“内存实现”，重启后数据不会保留。
- 启用 `mysql` profile 时，`core-iam`、`core-radar`、`core-rule-engine`、`core-notification`、`core-tool-habit` 已使用 MySQL 持久化。
- ES 还未部署，`core-radar` 的 ES 子步顺延。
- 生产部署当前使用外置 MySQL/Redis，编排中暂不内置 MySQL/Redis/ES/MQ。
- 若 MySQL/Redis 已在同机其他 compose 中运行，可通过 `host.docker.internal` 接入。
- 当前项目暂未编写业务测试用例（`No tests to run`）。

## 3. 主线任务（先做）
- 接入 XXL-JOB（真实调度）和 MQ（真实事件总线）替换当前编排接口。

## 4. 待办任务（往后排）
1. 持久化改造第 4 步（ES 子步，后排）：`core-radar` 从 MySQL 扩展到 ES 索引/检索。
2. 接入 XXL-JOB（真实调度）和 MQ（真实事件总线）替换当前编排接口。
3. 建立提交门禁：`pre-commit` 强制更新 `TASK_PROGRESS.md`。
4. 为已完成模块补充单元测试与集成测试。

## 5. 立即可执行命令
- 查看 Java8 Maven：`./mvnx.cmd 8 -v`
- 查看 Java17 Maven：`./mvnx.cmd 17 -v`
- Java8 测试构建：`./mvnx.cmd 8 clean test`
- Java17 测试构建：`./mvnx.cmd 17 clean test`
- 启动后端：`./mvnx.cmd 8 -pl backend/app-bootstrap spring-boot:run`
- 启动后端（MySQL 持久化）：`$env:SPRING_PROFILES_ACTIVE='mysql'; ./mvnx.cmd 8 -f backend/app-bootstrap/pom.xml spring-boot:run`
- 生产前端构建：`bash scripts/deploy/prod-frontend-build.sh`
- 生产镜像构建：`bash scripts/deploy/prod-build.sh`
- 生产编排启动：`bash scripts/deploy/prod-up.sh`
- 生产编排停止：`bash scripts/deploy/prod-down.sh`
- 本地基础设施启动（MySQL+Redis）：`bash scripts/infra/local-up.sh`
- 本地基础设施启动（全部）：`bash scripts/infra/local-up.sh full`
- 本地基础设施日志：`bash scripts/infra/local-logs.sh`
- 本地基础设施停止：`bash scripts/infra/local-down.sh`
- 管理端 Web 启动：`npm --prefix frontend/admin-web-vue3 run dev`
- 管理端 H5 启动：`npm --prefix frontend/admin-h5-vue3 run dev`
- 公众号 H5 启动：`npm --prefix frontend/wechat-official-h5 run dev`
- App（uni-app）启动 H5：`npm --prefix frontend/mobile-app run dev:h5`
- App（uni-app）启动微信小程序：`npm --prefix frontend/mobile-app run dev:mp-weixin`

## 6. 使用规则
- 每次完成一项任务：
  1. 从“主线任务”或“待办任务”中标记为已完成并迁移到“当前状态（已完成）”。
  2. 更新“最后更新”日期。
  3. 仅追加与本次任务相关的变更，不做无关重排。
