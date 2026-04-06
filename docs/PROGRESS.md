# Tally 项目总进度

> 新 session 进入时请先读此文件，再读对应 `docs/phases/` 文件获取详细上下文。

## 项目状态：✅ Phase 4 完成，下一步 Phase 5

**最后更新**：2026-04-06 17:55  
**当前阶段**：Phase 5 — 集成与打磨  
**下一个任务**：4.7 Android WebView Bridge（可选）或直接进入 Phase 5 Demo 场景验证

---

## 各阶段总览

| 阶段 | 名称 | 状态 | 详情 |
|------|------|------|------|
| Phase 1 | 基础架构搭建 | ✅ 完成 | [phase1_backend_setup.md](phases/phase1_backend_setup.md) |
| Phase 2 | Agent 核心 | ✅ 完成 | [phase2_agent_core.md](phases/phase2_agent_core.md) |
| Phase 3 | Android 聊天界面 | ✅ 完成 | [phase3_android_chat.md](phases/phase3_android_chat.md) |
| Phase 4 | WebView 日历/费用视图 | ✅ 完成 | [phase4_webview_ui.md](phases/phase4_webview_ui.md) |
| Phase 5 | 集成与打磨 | ⬜ 待开始 | [phase5_integration.md](phases/phase5_integration.md) |

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

---

## 重要技术决策 & 避坑记录

| 日期 | 决策/问题 | 结论 |
|------|-----------|------|
| 2026-04-06 | Android 语言 | Kotlin（Compose 仅支持 Kotlin）|
| 2026-04-06 | AI 模型 | kimi-k2.5（用户指定，OpenAI-compatible）|
| 2026-04-06 | 网络搜索 | Kimi 内置搜索，不用 Tavily |
| 2026-04-06 | kimi-k2.5 多步工具调用兼容性 | 需在 fetch 拦截器中为 assistant 消息注入 `reasoning_content: "."` 占位符，空字符串不行 |
| 2026-04-06 | reasoning_content 是否需要真实内容 | 否，`"."` 占位符足够，tool_result 驱动下一步决策 |
| 2026-04-06 | WSL 真机调试方式 | `adb reverse tcp:3000 tcp:3000`（Windows PowerShell 执行），后端监听 `0.0.0.0` |
| 2026-04-06 | OkHttp 流读取 | 用 `response.use{}` + 捕获 `IOException`，不用 `source.exhausted()` |
| 2026-04-06 | WebView ES module in Android | `type="module" crossorigin` 在 Android WebView + WebViewAssetLoader 下不可靠；解决方案：vite.config.ts 加自定义 plugin 去掉 `type="module"` 和 `crossorigin`，改为 IIFE 格式 + `defer`，同时 NavGraph 加 `mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW` |

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
