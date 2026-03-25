# uni-app 前端初始化方案（V1）

## 1. 技术栈

- `uni-app`
- `Vue 3 + TypeScript`
- `Pinia`
- `uni-ui`（后续可替换或并行引入 uView）

## 2. 目录结构

```text
frontend-uniapp
├─ package.json
├─ tsconfig.json
├─ vite.config.ts
└─ src
   ├─ App.vue
   ├─ main.ts
   ├─ manifest.json
   ├─ pages.json
   ├─ uni.scss
   ├─ pages
   │  ├─ home/index.vue
   │  ├─ habit/index.vue
   │  ├─ anniversary/index.vue
   │  ├─ plan/index.vue
   │  ├─ wish/index.vue
   │  ├─ recipe/index.vue
   │  ├─ shopping/index.vue
   │  └─ profile/index.vue
   ├─ store
   │  └─ user.ts
   └─ utils
      └─ request.ts
```

## 3. 路由清单（V1）

- `pages/home/index` 首页（工具导航、今日摘要）
- `pages/habit/index` 打卡
- `pages/anniversary/index` 纪念日
- `pages/plan/index` 计划
- `pages/wish/index` 愿望
- `pages/recipe/index` 菜谱
- `pages/shopping/index` 买菜
- `pages/profile/index` 我的

## 4. TabBar 规划

- 首页
- 打卡
- 计划
- 我的

## 5. 启动命令

- H5：`npm run dev:h5`
- 微信小程序：`npm run dev:mp-weixin`

## 6. 后续建议

1. 先完成登录态与 `request` 封装（token 注入/过期处理）。
2. 按业务模块拆 `api`：`habit.ts`、`plan.ts`、`recipe.ts`。
3. 首页先实现 4 个 MVP 闭环入口（打卡、纪念日、计划、买菜）。
