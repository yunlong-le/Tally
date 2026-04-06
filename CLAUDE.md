# Tally Android App — Claude Code 开发指南

## 项目概述
- **项目名**: Tally（对位）
- **平台**: Android App（最低 API 26 / Android 8.0）
- **规模**: 预期中大型项目
- **架构**: 薄客户端 + DDD 领域驱动 + Multi-Agent（Orchestrator + Schedule + Expense）
- **AI 模型**: kimi-k2.5（月之暗面，OpenAI-compatible API）
- **后端**: Node.js + TypeScript + Vercel AI SDK
- **数据库**: PostgreSQL（Docker Compose）

## 工作流

### 1. 每个特性开始前
- 使用 **Plan Mode** 生成实现计划
- 获得用户批准后再开始开发

### 2. 编码时强制 TDD
```
RED → GREEN → REFACTOR
1. 先写测试
2. 测试失败（RED）
3. 实现最小代码使测试通过（GREEN）
4. 重构优化代码
5. 验证覆盖率 >= 80%
```

### 3. 提交代码前必须
- ✅ 所有测试通过
- ✅ 测试覆盖率 >= 80%
- ✅ 使用 code-reviewer agent 审查
- ✅ 无 hardcoded secrets/passwords
- ✅ 遵循提交信息格式: `<type>: <description>`

### 4. 允许的操作
- Read/Edit/Write/Bash — 无需确认
- gradle build/test — 自动允许
- git commit — 需确认（一次性批准后再操作）

### 5. 禁止的操作
- `rm -rf` — 自动阻止
- `git push --force` — 自动阻止
- `git reset --hard` — 自动阻止
- Hardcoded API keys — 检查并拒绝

## 文件组织
- **gradle 项目结构** 标准 Android 项目布局
- 单文件最大 800 行
- 函数最大 50 行
- 按功能/feature 组织，不按类型

## 权限管理
- **预授权模式**: 已启用 `~/.claude.json`
- **Git 操作**: git push/commit 需手动确认
- **危险命令**: 自动阻止（rm -rf/reset --hard 等）
- **代码审查**: 强制使用 code-reviewer agent

## 语言规范
- **与用户交流、计划文件、代码注释**：使用中文
- **代码标识符**（变量名、函数名、类名、接口名）：使用英文
- **推荐英文的技术上下文**（commit message、API 文档、英文库注释）：使用英文

## 执行规范
- **每次只执行一个主体明确的小任务**，工作量大的任务必须拆分后逐一执行
- 始终在 **WSL (Linux)** 环境下工作，注意路径分隔符和换行符差异
- 使用 `docker` 命令前确认 Docker Desktop 已在 Windows 端启动

## 模型选择建议
- **Sonnet 4.6**：Agent 逻辑、架构设计、复杂推理、集成排查
- **Haiku 4.5**：UI 组件、脚手架生成、标准 CRUD、文档更新

## AI 模型配置
- **Provider**: 月之暗面 (Moonshot AI)
- **Model**: `kimi-k2.5`
- **Base URL**: `https://api.moonshot.cn/v1`
- **API Key**: 存储在 `backend/.env`，绝不 hardcode，绝不提交 git
- **网页搜索**: 使用 Kimi 内置搜索（`tools: [{"type":"web_search"}]`），无需 Tavily

## 进度追踪
- **每次 session 开始**：先读取 `docs/PROGRESS.md` 了解当前状态
- **每次 session 结束**：更新 `docs/PROGRESS.md` 和对应 `docs/phases/` 文件
- **推荐指令**：`/save-session` 保存 session 上下文

## 安全规范
- `backend/.env` 已加入 `.gitignore`
- 仅提供 `backend/.env.example`（key 值为占位符）
- Android 客户端不存储任何 API Key

## Android 设备
- **Phase 1-2**：使用 Android 模拟器（AVD）验证编译
- **Phase 3 起**：使用 USB 有线连接真机（ADB）
- 最低目标 SDK：API 26（Android 8.0）

## 规则引用
- 基础规则: `~/.claude/rules/common/`
- 项目规则: 见本文件
