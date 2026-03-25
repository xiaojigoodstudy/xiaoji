# 日常工具集 V1 方案（Java 后端 + 多端兼容）

## 1. 项目目标

打造一个可持续扩展的“个人/家庭日常管理工具集”，首期覆盖：

- 养生
- 打卡
- 纪念日
- 愿望
- 计划
- 菜谱
- 买菜

核心目标：

- 多端统一能力（微信小程序、微信公众号 H5、App）
- 模块化、可扩展（后续可增加记账、家务分工、亲友协同）
- 低成本上线与长期维护

## 2. 技术架构（推荐）

### 2.1 总体架构

- 前端：`uni-app`（一套代码编译到小程序 + H5 + App）
- 网关层：Nginx + API Gateway（可选 Spring Cloud Gateway）
- 后端：Java 17 + Spring Boot 3
- 数据层：MySQL 8 + Redis
- 文件存储：MinIO（私有）或对象存储（云）
- 消息能力：RabbitMQ（可后置，引入异步通知/提醒）
- 鉴权：JWT + Refresh Token
- 定时任务：Spring Scheduler / XXL-JOB（提醒、周期打卡）

### 2.2 部署形态（分阶段）

- V1（快速落地）：单体应用（模块化单体）
- V2（规模提升）：按域拆分微服务
  - 用户与身份服务
  - 日程与提醒服务
  - 内容服务（菜谱/养生）
  - 交易清单服务（买菜）

### 2.3 为什么这样设计

- 单体先快跑：研发和运维成本最低
- 模块先清晰：边界先定好，后续好拆分
- 前端统一：减少三端重复开发

## 3. 功能模块设计（V1）

### 3.1 养生

- 养生知识卡片（图文）
- 个人养生习惯（喝水、早睡、运动）
- 每日建议（规则驱动）

### 3.2 打卡

- 自定义打卡项（名称、周期、提醒时间）
- 打卡记录（成功/补卡/备注）
- 连续打卡天数、月统计

### 3.3 纪念日

- 纪念日管理（生日、周年、重要日）
- 倒计时/正计时
- 提前提醒（如提前 7/3/1 天）

### 3.4 愿望

- 愿望清单（优先级、目标时间、状态）
- 愿望拆解为计划任务
- 达成归档与复盘

### 3.5 计划

- 周计划/月计划
- 任务清单（优先级、截止时间、标签）
- 看板视图（待办/进行中/完成）

### 3.6 菜谱

- 菜谱管理（食材、步骤、口味、烹饪时长）
- 收藏与分类（家常、减脂、儿童餐）
- 与买菜清单联动生成

### 3.7 买菜

- 购物清单（按分类：蔬菜/肉类/调料）
- 勾选完成、预算估算
- 从菜谱一键生成清单

## 4. 角色与权限

- 普通用户：个人数据管理
- 家庭管理员：家庭空间配置、成员邀请
- 家庭成员：共享计划、清单协作

权限模型：RBAC（用户-角色-权限）

## 5. 数据库核心表（建议）

### 5.1 用户与组织

- `user`：用户基础信息
- `family_group`：家庭组
- `family_member`：家庭成员关系

### 5.2 通用能力

- `tag`：标签
- `reminder`：提醒配置
- `operation_log`：关键操作日志

### 5.3 业务域表

- 打卡
  - `habit`
  - `habit_record`
- 纪念日
  - `anniversary`
- 愿望/计划
  - `wish`
  - `plan`
  - `plan_task`
- 菜谱/买菜
  - `recipe`
  - `recipe_ingredient`
  - `shopping_list`
  - `shopping_item`
- 养生
  - `wellness_article`
  - `wellness_user_profile`

## 6. API 设计示例（REST）

- 用户
  - `POST /api/auth/login`
  - `GET /api/users/me`
- 打卡
  - `POST /api/habits`
  - `POST /api/habits/{id}/checkin`
  - `GET /api/habits/stats`
- 纪念日
  - `POST /api/anniversaries`
  - `GET /api/anniversaries/upcoming`
- 愿望/计划
  - `POST /api/wishes`
  - `POST /api/plans`
  - `POST /api/plans/{id}/tasks`
- 菜谱/买菜
  - `POST /api/recipes`
  - `POST /api/shopping-lists/from-recipe/{recipeId}`
  - `PATCH /api/shopping-items/{id}/checked`

## 7. 非功能要求

- 安全
  - JWT 鉴权、接口签名（小程序可选）
  - 敏感字段加密存储（手机号等）
- 性能
  - 热点查询走 Redis
  - 列表分页与索引优化
- 可观测性
  - 接口日志、错误追踪、慢 SQL 监控
- 可扩展
  - 业务域按 package 分层：controller/service/repository/domain
  - 统一事件模型（Domain Event）方便后续解耦

## 8. 推荐项目结构（单体模块化）

```text
daily-toolkit
├─ backend
│  ├─ common
│  ├─ auth
│  ├─ user
│  ├─ wellness
│  ├─ habit
│  ├─ anniversary
│  ├─ wish
│  ├─ plan
│  ├─ recipe
│  └─ shopping
└─ frontend-uniapp
```

## 9. 里程碑（8 周参考）

- 第 1-2 周：用户体系 + 打卡 + 纪念日
- 第 3-4 周：愿望 + 计划
- 第 5-6 周：菜谱 + 买菜联动
- 第 7 周：养生模块 + 提醒系统
- 第 8 周：联调测试 + 上线发布

## 10. V1 最小可用范围（MVP）

优先做这 4 个闭环：

- 打卡闭环：创建打卡项 -> 打卡 -> 统计
- 纪念日闭环：创建 -> 倒计时 -> 提醒
- 计划闭环：计划 -> 任务 -> 完成
- 菜谱买菜闭环：菜谱 -> 自动生成清单 -> 勾选采购

## 11. 下一步落地建议

1. 先初始化后端工程（Spring Boot 3 + MySQL + Redis + Flyway）
2. 明确统一数据字典（状态枚举、提醒规则、优先级）
3. 先完成打卡与纪念日两个模块作为样板
4. 同步搭建 uni-app 基础壳（登录、首页、模块入口）

---

如果你需要，我可以下一步直接给你生成：

- 后端 `Spring Boot` 初始化脚手架目录
- 关键表的 `Flyway SQL` 初稿
- 打卡模块的 Controller/Service/Entity 样例代码
