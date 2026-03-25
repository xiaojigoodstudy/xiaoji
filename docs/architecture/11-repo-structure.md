# 11 - 仓库结构与模块依赖

## 1. 目标
- 固化项目目录与模块边界，指导后续每个 PR 按单一职责实施。

## 2. 目录结构（v0.1）
```text
.
├─ docs/
│  └─ architecture/
├─ backend/
│  ├─ app-bootstrap/
│  ├─ app-gateway/
│  ├─ app-bff-c/
│  ├─ app-bff-admin/
│  └─ app-core/
│     ├─ core-shared/
│     ├─ core-iam/
│     ├─ core-radar/
│     ├─ core-rule-engine/
│     ├─ core-notification/
│     ├─ core-tool-habit/
│     └─ core-platform/
├─ frontend/
│  ├─ admin-web-vue3/
│  ├─ admin-h5-vue3/
│  ├─ wechat-miniapp/
│  ├─ wechat-official-h5/
│  └─ mobile-app/
├─ infra/
└─ scripts/
```

## 3. 后端依赖方向
- `app-bootstrap` -> `app-gateway` + `app-bff-c` + `app-bff-admin`
- `app-bff-c` -> `core-shared`（后续按需依赖具体 core 模块）
- `app-bff-admin` -> `core-shared`（后续按需依赖具体 core 模块）
- `app-core`（聚合）-> 各 `core-*` 子模块
- `core-*` -> `core-shared`（必要时）
- 禁止：`core-*` 之间随意循环依赖。

## 4. PR 约束（必须遵守）
- 一个 PR 只改一个业务问题或一个架构问题。
- 跨模块改动时，先提“边界调整 PR”，再提“业务实现 PR”。
- 文档变更与代码变更同 PR 保持一致。

## 5. 下一步初始化顺序
1. `app-core/core-shared`：统一异常、响应体、trace 工具。
2. `iam`：登录与 RBAC。
3. `radar`：信息源与抓取任务模型。
4. `rule-engine`：规则定义与判定。
5. `notification`：微信/App 推送通道。
6. `tool-habit`：事项与打卡。

