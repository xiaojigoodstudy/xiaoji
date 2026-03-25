# 09 - 数据模型与存储

## 1. 目标与边界
- 为雷达检索、规则判定、提醒发送、打卡统计提供稳定数据底座。

## 2. 存储分工
- MySQL：事务数据（用户、规则、任务、日志索引）。
- Redis：会话缓存、热点数据、频控计数。
- Elasticsearch：雷达内容检索与聚合查询。

## 3. 核心实体（一期）
- `user`
- `radar_source`
- `radar_item`
- `rule_definition`
- `rule_subscription`
- `notification_task`
- `notification_log`
- `habit_item`
- `habit_checkin_record`

## 4. 通用字段规范
- 主键：`id`（雪花或 UUID）
- 审计：`createdAt/updatedAt/createdBy/updatedBy`
- 逻辑删除：`isDeleted`
- 预留：`tenantId`（一期默认单租户）

## 5. MVP 验收标准
- 雷达内容可按关键词与时间检索。
- 规则与发送日志具备可追踪关联。
- 打卡统计按日聚合准确。
