# TASK PROGRESS

最后更新：2026-03-25

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
  - `frontend/mobile-app`（Vue3 + TS + Vite；后续可替换 uni-app）
- 已完成项目 Maven 独立 settings：
  - `D:\maven\apache-maven-3.8.3\conf\settings-xiaoji.xml`
  - 本地仓：`D:\maven\repo-xiaoji`
  - 项目硬绑定：`.mvn/maven.config`
- 已完成持久化改造拆分第 1 步：
  - 新增 `sql/` 目录与初始化脚本（`00_init_database.sql`、`01_iam.sql`、`02_radar.sql`、`03_rule_engine.sql`、`04_notification.sql`、`05_habit.sql`、`99_seed_dev.sql`）
- 已完成 JDK 切换脚本：
  - `mvn8.cmd`
  - `mvn17.cmd`
  - `mvnx.cmd`（统一入口）
- 已初始化 Git 仓库并完成第一次基线提交（含根级 `.gitignore`）。
- 验证通过：
  - `./mvnx.cmd 8 clean test` -> BUILD SUCCESS
  - `./mvnx.cmd 17 clean test` -> BUILD SUCCESS
  - `npm --prefix frontend/admin-web-vue3 run build` -> SUCCESS
  - `npm --prefix frontend/admin-h5-vue3 run build` -> SUCCESS
  - `npm --prefix frontend/wechat-official-h5 run build` -> SUCCESS
  - `npm --prefix frontend/mobile-app run build` -> SUCCESS

## 2. 当前约束/说明
- 目前核心功能为“内存实现”，重启后数据不会保留。
- `mobile-app` 当前是 Vue 壳，尚未切换为 uni-app 真多端工程。
- 当前项目暂未编写业务测试用例（`No tests to run`）。

## 3. 主线任务（先做）
- 持久化改造（待办 1）第 2 步：后端接入 MySQL/Flyway，并让应用可加载 `sql/` 初始化脚本。

## 4. 待办任务（往后排）
1. 持久化改造第 3 步：`core-iam` 从内存实现切换到 MySQL。
2. 持久化改造第 4 步：`core-radar` 从内存实现切换到 MySQL/ES（先 MySQL，后 ES）。
3. 持久化改造第 5 步：`core-rule-engine`、`core-notification`、`core-tool-habit` 切换到 MySQL/Redis。
4. 接入 XXL-JOB（真实调度）和 MQ（真实事件总线）替换当前编排接口。
5. 将 `frontend/mobile-app` 从 Vue 壳替换为 uni-app 工程。
6. 建立提交门禁：`pre-commit` 强制更新 `TASK_PROGRESS.md`。
7. 为已完成模块补充单元测试与集成测试。
8. 增加 `dev/test/prod` 多环境配置模板。
9. 补充本地一键启动基础设施（MySQL/Redis/ES/MQ）脚本。

## 5. 立即可执行命令
- 查看 Java8 Maven：`./mvnx.cmd 8 -v`
- 查看 Java17 Maven：`./mvnx.cmd 17 -v`
- Java8 测试构建：`./mvnx.cmd 8 clean test`
- Java17 测试构建：`./mvnx.cmd 17 clean test`
- 启动后端：`./mvnx.cmd 8 -pl backend/app-bootstrap spring-boot:run`
- 管理端 Web 启动：`npm --prefix frontend/admin-web-vue3 run dev`
- 管理端 H5 启动：`npm --prefix frontend/admin-h5-vue3 run dev`
- 公众号 H5 启动：`npm --prefix frontend/wechat-official-h5 run dev`
- App 壳启动：`npm --prefix frontend/mobile-app run dev`

## 6. 使用规则
- 每次完成一项任务：
  1. 从“主线任务”或“待办任务”中标记为已完成并迁移到“当前状态（已完成）”。
  2. 更新“最后更新”日期。
  3. 仅追加与本次任务相关的变更，不做无关重排。



