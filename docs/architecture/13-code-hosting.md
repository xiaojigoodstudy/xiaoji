# 13 - 代码托管平台建议

## 1. 推荐结论
- 主仓建议放 `GitHub`。
- 同步镜像到 `Gitee`（国内网络访问更稳）。
- 如后续有内网协作需求，再加 `GitLab` 私有仓。

## 2. 平台选型对比
- GitHub：生态最强，CI/CD 与第三方集成最丰富。
- Gitee：国内访问速度和稳定性更好，适合镜像与国内协作。
- GitLab（自建/私有）：权限与合规控制强，运维成本更高。

## 3. 本项目落地方案（建议）
- 方案：`GitHub 主仓 + Gitee 镜像仓`。
- 分支：`main`（稳定）+ `feature/*`（开发）。
- 合并规则：一个 PR 只解决一个问题（单一职责）。

## 4. 初始化与双远端命令
```bash
git init
git checkout -b main
git add .
git commit -m "chore: bootstrap project skeleton"

git remote add origin <github-repo-url>
git remote add gitee <gitee-repo-url>

git push -u origin main
git push -u gitee main
```

## 5. 日常同步命令
```bash
# 提交后同时推送到两端
git push origin main
git push gitee main
```

## 6. 安全建议
- 使用 PAT 或 SSH Key，不使用账号密码直连。
- 不提交密钥、token、生产配置。
- 开启仓库保护：`main` 禁止强推、必须走 PR。
