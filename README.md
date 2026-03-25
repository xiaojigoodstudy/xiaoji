# Daily Toolkit

个人向三端兼容工具集（微信小程序/公众号/App）项目骨架。

## 当前状态
- 已完成架构文档：`docs/architecture`
- 已初始化后端 Maven 多模块骨架：`backend`
- 已创建前端与基础设施目录：`frontend`、`infra`、`scripts`

## 快速开始
- 编译后端：`mvn -q -DskipTests compile`
- 启动主程序（后续补充配置后）：`mvn -pl backend/app-bootstrap spring-boot:run`
