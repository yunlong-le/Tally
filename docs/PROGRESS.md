# Tally 项目总进度

> 新 session 进入时请先读此文件，再读对应 `docs/phases/` 文件获取详细上下文。

## 项目状态：Phase 6 🟡 部署中（2026-04-13）

**最后更新**：2026-04-13  
**当前阶段**：Phase 6 — 阿里云部署（进行中）+ UI 细节打磨

**Phase 6 完成情况**：
- ✅ ECS 服务器配置（Docker、docker-compose）
- ✅ 后端服务部署（Nginx + Node.js + PostgreSQL）
- ✅ 安全组配置（80端口开放）
- ✅ Nginx 安全配置（限流、防扫描）
- ✅ 数据库 migrations 执行
- ✅ API Key 配置
- ✅ Android BASE_URL 修改为公网 IP
- ✅ WebView CORS 配置（mixedContentMode）
- ✅ WebView 前端 BASE_URL 修改
- 🟡 聊天功能正常
- 🟡 新建日程功能正常
- ❌ 日历页面显示（前端错误显示已添加，待验证）
- ❌ 费用页面显示（前端错误显示已添加，待验证）
- ❌ Bug：补充标题后 App 卡住退出（待排查）  
**Phase 5 完成情况**：
- ✅ Tavily 网络搜索接入
- ✅ WebView 错误处理  
- ✅ 深色主题 CSS 统一
- ✅ HIGHLIGHTS.md 求职亮点文档
- ✅ 6 个 Demo 场景真机验证通过
- ✅ Phase 1-4 回归测试通过

**Phase 5 Bug 修复记录**：
- 迭代 1：日历时区、选中样式、系统日期、状态栏 padding、Markdown 渲染、新对话按钮
- 迭代 2：样式优先级、表格渲染、布局重叠、对话历史功能
- 迭代 3：表格边框、顶部/底部栏高度优化、输入法重叠修复、App 图标

---

## 各阶段总览

| 阶段 | 名称 | 状态 | 详情 |
|------|------|------|------|
| Phase 1 | 基础架构搭建 | ✅ 完成 | [phase1_backend_setup.md](phases/phase1_backend_setup.md) |
| Phase 2 | Agent 核心 | ✅ 完成 | [phase2_agent_core.md](phases/phase2_agent_core.md) |
| Phase 3 | Android 聊天界面 | ✅ 完成 | [phase3_android_chat.md](phases/phase3_android_chat.md) |
| Phase 4 | WebView 日历/费用视图 | ✅ 完成 | [phase4_webview_ui.md](phases/phase4_webview_ui.md) |
| Phase 5 | 集成与打磨 | ✅ 完成 | [phase5_integration.md](phases/phase5_integration.md) |
| Phase 6 | 阿里云部署 | 🟡 待实施 | [phase6_deployment.md](phases/phase6_deployment.md) |

---

## Phase 2 任务清单

- ✅ 2.1 Vercel AI SDK + Kimi 集成（`POST /api/chat` 流式接口）
- ✅ 2.2 Schedule Agent 工具集（createEvent / updateEvent / deleteEvent / listEvents / checkConflict）
- ✅ 2.3 Expense Agent 工具集（createExpense / listExpenses / linkExpenseToEvent / listExpensesByEvent）

## Phase 3 任务清单

- ✅ 3.1 数据模型（ChatMessage）
- ✅ 3.2 OkHttp 流式客户端（TallyApiClient，解析 Vercel AI Data Stream）
- ✅ 3.3 ChatViewModel（StateFlow 状态管理）
- ✅ 3.4 ChatScreen（消息气泡 + 输入栏 + 流式渲染）
- ✅ 3.5 NavGraph 接入
- ✅ 3.6 真机联调（小米17，adb reverse 隧道）

## Phase 4 任务清单

- ✅ 4.1 后端 REST API（`GET /api/events`, `GET /api/expenses`）
- ✅ 4.2 WebView UI（React + Vite，日历视图 + 费用视图，构建到 Android assets）
- ✅ 4.3 Android WebView 接入（webkit 依赖，NavGraph 替换为 WebViewScreen）
- ✅ 4.4 真机验证（截图确认：日历月历网格 + 点击日期显示事件；费用总计 + 详情列表；聊天 AI 查询日程。月份切换、断线重连等未测试）
- ✅ 4.7 WebView Bridge 验证（2026-04-07）：日历/费用点击 → 跳转聊天 + 预填上下文消息，发送正常，AI 回复正常，Tab 切换消息历史保留

---

## 重要技术决策 & 避坑记录

| 日期 | 决策/问题 | 结论 |
|------|-----------|------|
| 2026-04-06 | Android 语言 | Kotlin（Compose 仅支持 Kotlin）|
| 2026-04-06 | AI 模型 | kimi-k2.5（用户指定，OpenAI-compatible）|
| 2026-04-07 | 网络搜索 | 改用 Tavily（Kimi 不支持 web_search 工具类型，会 400）；Tavily 1000 次/月免费 |
| 2026-04-06 | kimi-k2.5 多步工具调用兼容性 | 需在 fetch 拦截器中为 assistant 消息注入 `reasoning_content: "."` 占位符，空字符串不行 |
| 2026-04-06 | reasoning_content 是否需要真实内容 | 否，`"."` 占位符足够，tool_result 驱动下一步决策 |
| 2026-04-06 | WSL 真机调试方式 | `adb reverse tcp:3000 tcp:3000`（Windows PowerShell 执行），后端监听 `0.0.0.0` |
| 2026-04-06 | OkHttp 流读取 | 用 `response.use{}` + 捕获 `IOException`，不用 `source.exhausted()` |
| 2026-04-06 | WebView ES module in Android | `type="module" crossorigin` 在 Android WebView + WebViewAssetLoader 下不可靠；解决方案：vite.config.ts 加自定义 plugin 去掉 `type="module"` 和 `crossorigin`，改为 IIFE 格式 + `defer`，同时 NavGraph 加 `mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW` |
| 2026-04-07 | Phase 4.7 WebView Bridge | 验证通过：日历/费用点击跳转聊天 + 预填上下文，消息发送正常，Tab 切换历史保留 |
| 2026-04-08 | Phase 5 完成 | 6 个 Demo 场景真机验证通过，Phase 1-4 回归测试通过 |
| 2026-04-08 | Phase 6 调研 | 阿里云ECS环境调研完成：Docker已安装，2核2G，待部署 |
| 2026-04-10 | UI重构完成 | ChatScreen重构Gemini风格，4个Insets/键盘问题全部修复 |
| 2026-04-11 | Phase 6 部署 | 后端部署完成，公网IP: 8.140.192.167，聊天功能正常 |
| 2026-04-11 | Bug待修复 | 新建日程补充标题后App卡住退出（需Windows端Claude排查）|
| 2026-04-13 | 键盘空白 bug 彻底修复 | 两阶段修复：① Manifest 加 `adjustResize`（减半）→ ② NavHost 加 `consumeWindowInsets(innerPadding)`（完全消除）|
| 2026-04-13 | 导航栏高度优化 | Material 3 默认 80dp → 62dp；图标 22→20dp；标签 11→10sp；系统导航区用独立 Spacer 补高 |

---

## UI重构完成（2026-04-10）

### 完成项

| # | 问题描述 | 状态 |
|---|---------|------|
| 1 | 日历/费用页面顶部与状态栏间距不一致 | ✅ 已修复 |
| 2 | 底部导航栏被页面内容遮挡 | ✅ 已修复 |
| 3 | 输入框与软键盘之间有巨大空白 | ✅ 已修复（2026-04-13 彻底修复，两阶段：adjustResize + consumeWindowInsets）|
| 4 | 历史页面搜索框无法弹出输入法 | ✅ 已修复 |

- ✅ ChatScreen 重构为 Gemini 风格（顶部栏、垂直快捷按钮、悬浮输入框）
- ✅ 主题更新：深色背景 #111111、品牌绿 #1DB954、大圆角设计
- ✅ WebView 日历/费用样式统一（24px圆角、胶囊型Chips）

### 关键代码位置

```
android/app/src/main/kotlin/com/tally/app/
├── navigation/NavGraph.kt          # Scaffold, bottomBar, WebViewScreen
├── ui/chat/ChatScreen.kt           # TopBar, SimpleInputBar, HistorySidebar
└── MainActivity.kt                 # enableEdgeToEdge, windowInsets配置
```

---

## 目录结构（当前）

```
Tally/
├── backend/                    ✅ 已创建
│   ├── src/
│   │   ├── agents/             ✅ kimiClient.ts, orchestrator.ts
│   │   ├── domains/
│   │   │   ├── schedule/       ✅ types, repository, tools（5个）
│   │   │   └── expense/        ✅ types, repository, tools（4个）
│   │   ├── api/routes/         ✅ health.ts, chat.ts
│   │   └── infrastructure/     ✅ db.ts
│   ├── migrations/             ✅ 4张表
│   ├── docker-compose.yml      ✅ PostgreSQL 16
│   └── .env                    ✅ KIMI_API_KEY 已填入
├── android/                    ✅ 已创建
│   └── app/src/main/kotlin/com/tally/app/
│       ├── data/
│       │   ├── model/          ✅ ChatMessage.kt
│       │   └── remote/         ✅ TallyApiClient.kt
│       ├── ui/chat/            ✅ ChatViewModel.kt, ChatScreen.kt
│       ├── navigation/         ✅ NavGraph.kt
│       └── ui/theme/           ✅ 深色主题
├── webview-ui/                 ⬜ 待创建（Phase 4）
├── docs/                       ✅ 已创建
└── CLAUDE.md                   ✅ 已更新
```

---

## 启动命令

```bash
# 后端（WSL）
cd /home/realw/Tally/backend
docker compose up -d        # 启动 PostgreSQL
npm run dev                 # 启动后端 port 3000

# Android 调试（Windows PowerShell）
adb reverse tcp:3000 tcp:3000                          # 建立端口隧道
adb install -r C:\Users\realw\Desktop\tally.apk        # 安装/更新 APK

# Android 编译 + 更新 APK（WSL）
cd /home/realw/Tally/android && ./gradlew assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk /mnt/c/Users/realw/Desktop/tally.apk
```
