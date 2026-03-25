# Architecture Docs

## 阅读顺序

1. [00-overview.md](./00-overview.md)
2. [01-backend-java.md](./01-backend-java.md)
3. [07-bff-and-gateway.md](./07-bff-and-gateway.md)
4. [08-event-and-rule-engine.md](./08-event-and-rule-engine.md)
5. [09-data-model-and-storage.md](./09-data-model-and-storage.md)
6. [10-nfr-observability.md](./10-nfr-observability.md)
7. [02-admin-web-vue3.md](./02-admin-web-vue3.md)
8. [03-admin-h5-vue3.md](./03-admin-h5-vue3.md)
9. [04-wechat-miniapp.md](./04-wechat-miniapp.md)
10. [05-wechat-official-h5.md](./05-wechat-official-h5.md)
11. [06-mobile-app.md](./06-mobile-app.md)

## 文档范围

- 一期 MVP：信息雷达 + 自动提醒 + 打卡。
- 技术基线：模块化单体 + 事件化，后端 Java，管理端 Vue3。
- 三端兼容：微信小程序、公众号 H5、App。

## 命名约定

- API 路径：`/api/{domain}/...`
- 事件主题：`domain.action.result`，例如 `radar.item.discovered`
- Trace 字段：`traceId` 全链路透传
- 幂等键：`idempotencyKey` 用于写操作去重

## 文档维护规则

- 每次涉及架构边界、模块职责、接口契约变更，必须同步更新相关文档。
- 单个 PR 仅修改本次问题相关文档，避免“顺手大改”。
- 文档优先于代码讨论，代码实现必须遵循文档中的职责边界。
