# Tally（对位）

> AI 驱动的 Android 日程 + 费用管理 App

![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-4285F4?logo=jetpackcompose&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-TypeScript%205.3-339933?logo=node.js&logoColor=white)
![AI Agent](https://img.shields.io/badge/AI-Multi--Agent-FF6B6B)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 项目简介

Tally 是一款 **AI 原生 Android 应用**，用户通过自然语言对话即可完成日程创建、冲突检测、费用记录与关联。后端基于 **Multi-Agent 架构**，使用月之暗面 Kimi-k2.5 模型，通过 Vercel AI SDK 的 `streamText(maxSteps=5)` 实现多步工具链式调用，支持流式实时响应。前端使用 Jetpack Compose 构建聊天界面，嵌套 React WebView 展示结构化的日历和费用视图，通过 `@JavascriptInterface` 实现 Native ↔ WebView ↔ AI 三层跨层通信。项目已在小米 17 真机完成端到端验证，后端部署至阿里云 ECS。

**核心能力**：Multi-Agent 链式工具调用 · 流式 SSE 响应 · WebView 跨层通信 · 真机端到端验证

---

## 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                     Android App（Kotlin）                      │
│                                                              │
│  ┌──────────────┐  ┌────────────────┐  ┌───────────────┐    │
│  │  ChatScreen  │  │  CalendarView  │  │  ExpenseView  │    │
│  │  (Compose)   │  │   (WebView)    │  │   (WebView)   │    │
│  └──────┬───────┘  └───────┬────────┘  └───────┬───────┘    │
│         │                  └───────────────────┘             │
│         │           TallyJsBridge (@JavascriptInterface)      │
│  ChatViewModel ◄─────────────────┘                           │
│  (StateFlow)                                                  │
│         │  TallyApiClient（OkHttp 流式 SSE）                   │
└─────────┼────────────────────────────────────────────────────┘
          │ POST /api/chat（Vercel AI Data Stream 协议）
          │
┌─────────▼────────────────────────────────────────────────────┐
│                Backend（Node.js + TypeScript）                 │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Orchestrator Agent                        │   │
│  │    streamText(maxSteps=5, tools=[子 Agent 工具])        │   │
│  └──────────────┬───────────────┬────────────────────────┘   │
│                 │               │                             │
│  ┌──────────────▼────┐  ┌───────▼────────────────────┐      │
│  │  Schedule Agent   │  │     Expense Agent           │      │
│  │  · createEvent    │  │  · createExpense            │      │
│  │  · listEvents     │  │  · listExpenses             │      │
│  │  · updateEvent    │  │  · linkExpenseToEvent       │      │
│  │  · deleteEvent    │  │  · listExpensesByEvent      │      │
│  │  · checkConflict  │  └──────────────┬──────────────┘      │
│  └──────────┬─────── ┘                 │                     │
│             └──────────────┬───────────┘                     │
│                            │                                  │
│  ┌─────────────────────────▼────────────────────────────┐   │
│  │           PostgreSQL 16（Docker）                      │   │
│  │    events · expenses · event_expense_links           │   │
│  └────────────────────────────────────────────────────────┘  │
│  kimiCompatFetch 拦截器（注入 reasoning_content 占位符）       │
└──────────────────────────────────────────────────────────────┘
          │
   月之暗面 Kimi API · kimi-k2.5（OpenAI-compatible）
```

---

## 技术栈

### Android 客户端

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.22 | 主开发语言 |
| Jetpack Compose | BOM 2024.02.00 | 声明式 UI |
| Material 3 | — | 设计系统（深色主题 #111111 + 品牌绿 #1DB954） |
| Jetpack Navigation | 2.7.6 | 三 Tab 路由管理 |
| ViewModel + StateFlow | 2.7.0 | 响应式状态管理 |
| OkHttp | 4.12.0 | 流式 SSE HTTP 客户端 |
| DataStore + Gson | 1.0.0 / 2.10.1 | 会话历史持久化 |
| androidx.webkit | 1.12.1 | WebView 增强 |
| Coroutines | 1.7.3 | 异步任务调度 |
| Min SDK | API 26 | Android 8.0+ |

### 后端服务

| 技术 | 版本 | 用途 |
|------|------|------|
| Node.js + TypeScript | 5.3.3 | 主开发语言 |
| Express | 4.18.2 | HTTP 服务框架 |
| Vercel AI SDK | 4.3.15 | streamText + 工具调用编排 |
| @ai-sdk/openai | 1.3.22 | OpenAI-compatible 适配器（接 Kimi） |
| Kimi-k2.5 | — | 月之暗面，推理型大语言模型 |
| @tavily/core | 0.7.2 | 联网搜索工具 |
| pg | 8.11.3 | PostgreSQL 原生客户端 |
| Zod | 3.22.4 | Schema 验证 |
| Docker + Nginx | — | 容器化部署 + 反向代理限流 |

### WebView UI

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 18.3.1 | 日历 / 费用视图组件 |
| TypeScript | 5.6.3 | 类型安全 |
| Vite | 6.0.6 | 构建工具（IIFE 输出模式，兼容 Android WebView） |

---

## 核心功能

| 功能 | 描述 |
|------|------|
| 💬 **自然语言对话** | 用中文描述日程和费用，AI 自动解析时间、金额、关联关系 |
| 📅 **智能日程管理** | 新建、修改、删除日程；自动检测时间冲突并提示 |
| 💰 **费用追踪** | 记录消费并关联至具体日程事件（如出差代驾报销） |
| 🔍 **联网搜索** | Tavily API 查询实时信息（活动日期等），自动创建日程 |
| 📊 **日历 / 费用视图** | WebView 内嵌 React 月历网格 + 费用列表，点击条目自动跳转 AI 对话 |
| 🌊 **流式实时响应** | AI 回复字符级流式显示，体验接近 ChatGPT |
| 💾 **会话历史持久化** | DataStore + Gson 存储会话，App 重启后历史不丢失 |
| 🚀 **公网部署** | 后端部署至阿里云 ECS，Nginx 反向代理 + 限流配置 |

---

## Demo 场景（Agent 能力边界验证）

| # | 用户输入 | Agent 调用链 | 体现能力 |
|---|---------|-------------|---------|
| 1 | "4月18号，我要去参加AI研讨会，帮我记录" | `createEvent` | 单步工具调用，时间解析 |
| 2 | "帮我查北京车展是什么时候，提前两天到北京" | 联网搜索 → `createEvent` | 知识检索 + 推理 + 工具调用 |
| 3 | "下礼拜一清空所有行程，帮我请假" | `listEvents` → `deleteEvent` × N | **多步调用，动态决策工具次数** |
| 4 | "明天看一下我什么时间有空，安排业务规划会" | `listEvents` → `checkConflict` → `createEvent` | **3步链式推理，冲突感知** |
| 5 | "出差代驾200块，帮我记录并关联出差行程" | `createExpense` → `linkExpenseToEvent` | 跨域 Agent 协作（费用+日程） |
| 6 | "今天给团队买咖啡花了150，帮我记一下" | `createExpense` | 费用域单步，金额自然语言解析 |

> **场景 3、4 是核心压力测试**：场景 3 要求 Orchestrator 动态决策调用次数；场景 4 是三步依赖链，每步输出作为下一步输入。两个场景的稳定运行直接依赖下方 kimi-k2.5 兼容性攻坚成果。

---

## 工程技术亮点

### 亮点 1：kimi-k2.5 多步工具调用兼容性攻坚

**问题背景**：Vercel AI SDK 的 `streamText(maxSteps=N)` 在多步工具调用时，会重建消息历史传给下一步。kimi-k2.5 是思考型模型，要求每条 `assistant` 消息必须携带 `reasoning_content` 字段；SDK 重建的历史消息中该字段丢失，导致 Kimi 返回 HTTP 400。

**根因**：SDK 不感知第三方模型对 `reasoning_content` 的强制要求；空字符串 `""` 同样触发 400，必须是非空占位符；多步链中每条历史 assistant 消息都需修复。

**解决方案**：自定义 `kimiCompatFetch` 拦截器，在 `fetch` 层拦截请求，遍历 `messages` 数组并注入占位符：

```typescript
// backend/src/agents/kimiClient.ts
const kimiCompatFetch: FetchFunction = async (url, options) => {
  const body = JSON.parse(options.body as string);
  body.messages = body.messages.map((msg: Message) => {
    if (msg.role === "assistant" && !msg.reasoning_content) {
      return { ...msg, reasoning_content: "." }; // 非空占位符，空串会 400
    }
    return msg;
  });
  return fetch(url, { ...options, body: JSON.stringify(body) });
};
```

**验证**：`checkConflict → createEvent`（2步）、`listEvents → deleteEvent × N`（N+1步）稳定运行，无 400 错误。

---

### 亮点 2：Android WebView ES Module 兼容性

**问题背景**：使用 `WebViewAssetLoader` 加载本地 React 打包产物时，Vite 默认生成带 `type="module" crossorigin` 的 `<script>` 标签，在 Android WebView 下导致**页面白屏**且无任何 JS 错误输出。

**解决方案**（三步组合）：

```typescript
// webview-ui/vite.config.ts — 自定义 plugin 后处理 index.html
{
  name: 'remove-module-type',
  transformIndexHtml(html) {
    return html
      .replace(/type="module"/g, '')
      .replace(/crossorigin/g, '')
      .replace(/<script /g, '<script defer ');
  }
}
```

① Vite plugin 去除 `type="module"` 和 `crossorigin`，改为带 `defer` 的普通脚本  
② 构建输出格式从 ESM 改为 **IIFE**，消除模块依赖  
③ NavGraph 增加 `mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW`

---

### 亮点 3：WebView Bridge 跨层通信

**完整调用链**：

```
WebView JS（React）
  └─ window.TallyBridge.openChat(contextMessage)
       └─ @JavascriptInterface（TallyJsBridge.kt）
            └─ Handler(Looper.getMainLooper()).post { }   ← 切回主线程
                 └─ chatViewModel.prefillMessage(msg)
                      └─ navController.navigate("chat")
```

**关键细节**：`@JavascriptInterface` 由 WebView 在后台线程调用，必须通过 `Handler(Looper.getMainLooper())` 切回主线程才能操作 ViewModel。共享 `ChatViewModel` 在 Tab 切换时保持消息历史。

---

### 亮点 4：WSL2 真机调试网络隔离突破

**链路**：后端在 WSL2 Linux 中，真机通过 USB 连接 Windows，两者网络命名空间隔离。

```
手机 localhost:3000
  → ADB 反向隧道（Windows PowerShell: adb reverse tcp:3000 tcp:3000）
       → Windows localhost:3000
            → WSL2 自动桥接
                 → WSL 内后端（监听 0.0.0.0）
```

**关键**：`adb reverse` 必须在 Windows PowerShell 执行而非 WSL 内；后端监听 `0.0.0.0` 而非 `127.0.0.1`。

---

## 开发进度

| 阶段 | 内容 | 状态 | 关键交付物 |
|------|------|:----:|-----------|
| Phase 1 | 后端基础架构 | ✅ | PostgreSQL 4张表、Express API、docker-compose |
| Phase 2 | Agent 核心 | ✅ | Kimi 集成、Schedule/Expense 9个工具、流式接口 |
| Phase 3 | Android 聊天界面 | ✅ | OkHttp 流式客户端、Compose ChatScreen、真机联调 |
| Phase 4 | WebView 日历/费用 | ✅ | React + Vite UI、WebView Bridge、三 Tab 导航 |
| Phase 5 | 集成与打磨 | ✅ | 6个 Demo 场景真机验证、键盘/导航栏 Bug 修复、会话持久化 |
| Phase 6 | 阿里云部署 | 🟡 | ECS 部署、Nginx 安全配置、公网访问验证 |

**开发周期**：2026-04-03 ～ 2026-04-13，约 12 天

---

## 代码统计

| 模块 | 语言 | 文件数 | 代码行数 | 主要职责 |
|------|------|:------:|:-------:|---------|
| Android 客户端 | Kotlin | 11 | 1,657 | Compose UI、OkHttp 流客户端、ViewModel、WebView 桥接 |
| 后端服务 | TypeScript | 16 | 1,203 | Multi-Agent Orchestrator、领域工具、REST API、DB 层 |
| WebView UI | TypeScript/TSX | 6 | 452 | React 日历视图、费用视图、API 通信层 |
| **合计** | — | **33** | **3,312** | — |

---

## AI 辅助开发说明

本项目使用 **Claude Code** 作为主要 AI 辅助开发工具，整合三种模型协同完成不同类型任务。

### 模型使用分工

| 模型 | 使用占比 | 主要用途 |
|------|:--------:|---------|
| Claude Sonnet 4.6 | ~40% | 架构设计、Agent 逻辑、复杂推理、集成排查 |
| Kimi-k2.5（月之暗面） | ~40% | 后端 Agent 运行时调用（App 功能本身） |
| Claude Haiku 4.5 | ~20% | UI 组件生成、脚手架、文档更新 |

> Kimi-k2.5 同时扮演两个角色：**开发辅助**（Claude Code 任务分配）和 **App 运行时**（作为 Multi-Agent 的推理引擎）。

---

### Token 消耗统计（Claude Code 部分）

以下数据来自 Claude Code 的 usage tracking，统计时间范围：2026-04-03 ～ 2026-04-07（已落盘 session）。

| 日期 | 输入 tokens | 输出 tokens | 缓存写入 | 缓存读取 | API 调用次数 |
|------|----------:|----------:|--------:|--------:|:-----------:|
| 2026-04-03 | 1,559 | 13,606 | 447,442 | 2,042,785 | 40 |
| 2026-04-06 | 26,746 | 161,906 | 5,784,358 | 55,994,090 | 604 |
| 2026-04-07（估算） | — | — | — | — | ~50 |
| **合计** | **28,305** | **175,512** | **6,231,800** | **58,036,875** | **646+** |

**Token 结构分析**

```
总 token 流量：64,472,492
├── 普通输入：       28,305   （0.04%）← 极少，主要由系统提示缓存代替
├── 输出：          175,512   （0.27%）← 实际生成内容
├── 缓存写入：    6,231,800   （9.7%） ← 首次写入缓存，按折扣价计费
└── 缓存读取：   58,036,875   （90%）  ← 不收费（Claude Code 自动管理 prompt cache）
```

> **缓存命中率 90%** 是 Claude Code 自动管理 prompt cache 的效果。每次 API 调用重复携带大量系统上下文（项目规则、历史工具结果、CLAUDE.md），缓存读取价格仅为正常输入的 1/10，且本项目缓存读取部分**实际计为 $0**（Claude Code 订阅内包含）。

---

### 费用明细（Claude Code 部分，OpenRouter 计价）

| 计费项目 | Token 数 | 单价 | 费用（USD） |
|---------|----------:|------|----------:|
| 普通输入 | 28,305 | $3.00/M | $0.08 |
| 输出 | 175,512 | $15.00/M | $2.63 |
| 缓存写入 | 6,231,800 | $3.75/M | $23.37 |
| 缓存读取 | 58,036,875 | $0.30/M | $17.41 |
| **API 账单小计** | | | **$43.50** |
| OpenRouter 充值手续费（+8%） | | | +$3.78 |
| **Claude Code 实际支付** | | | **$47.28** |

> OpenRouter 信用卡充值收取 8% 手续费，实际到账余额为充值金额的 92%，因此等效多付 $3.78。

### 无缓存对比分析

| 场景 | 费用 |
|------|-----:|
| 实际费用（含缓存） | $43.50 |
| 若无缓存机制（58M 缓存读取全部按正常输入计费） | $176.83 |
| **缓存节省** | **$133.33（节省 75.4%）** |

### Kimi API 运行时费用（月之暗面计价）

| 计费项目 | 估算量 | 单价（官方） | 费用（RMB） |
|---------|------:|------|----------:|
| 输入 tokens（开发测试） | ~500K | ¥12/M | ¥6.00 |
| 输出 tokens（开发测试） | ~200K | ¥12/M | ¥2.40 |
| **Kimi 小计** | | | **≈ ¥8.40** |

> Kimi-k2.5 费用极低，主要成本来自 Claude Code 的辅助开发调用（缓存写入为主）。

---

## 应用截图

<!-- 请将真实 App 截图放入 screenshots/ 目录后替换以下占位符 -->

| 功能 | 截图 |
|------|------|
| AI 聊天界面 | *(待补充真实截图)* |
| 日程创建成功 | *(待补充真实截图)* |
| 多步请假（清空行程） | *(待补充真实截图)* |
| 月历视图 | *(待补充真实截图)* |
| 费用列表 | *(待补充真实截图)* |
| 费用关联日程 | *(待补充真实截图)* |

> 截图目录建议：`screenshots/`，格式建议：PNG，宽度建议：360px（1x）或 720px（2x）

---

## 项目目录结构

```
Tally/
├── android/                            # Android 客户端（Kotlin + Compose）
│   └── app/src/main/kotlin/com/tally/app/
│       ├── MainActivity.kt             # Edge-to-edge + windowInsets 配置
│       ├── data/
│       │   ├── model/ChatMessage.kt
│       │   ├── remote/TallyApiClient.kt        # OkHttp 流式 SSE 客户端
│       │   └── repository/ChatSessionRepository.kt  # DataStore 持久化
│       ├── ui/
│       │   ├── chat/ChatScreen.kt              # Gemini 风格聊天界面
│       │   └── chat/ChatViewModel.kt           # StateFlow 状态管理
│       └── navigation/
│           ├── NavGraph.kt                     # 3-Tab Scaffold
│           └── TallyJsBridge.kt                # @JavascriptInterface 桥接
│
├── backend/                            # Node.js + TypeScript 后端
│   └── src/
│       ├── agents/
│       │   ├── kimiClient.ts           # Kimi 兼容适配器（kimiCompatFetch 拦截器）
│       │   └── orchestrator.ts         # Multi-Agent 协调器
│       ├── domains/
│       │   ├── schedule/               # 日程域（5个工具）
│       │   └── expense/                # 费用域（4个工具）
│       ├── api/routes/                 # REST API（chat / events / expenses / health）
│       └── infrastructure/db.ts        # PostgreSQL 连接池
│
├── webview-ui/                         # React + Vite WebView UI
│   └── src/
│       ├── views/CalendarView.tsx      # 月历网格（24px 圆角，胶囊型 Chips）
│       └── views/ExpenseView.tsx       # 费用列表 + 汇总
│
└── README.md
```

---

## 本地开发启动

> **环境要求**：WSL2（Ubuntu）+ Docker Desktop（Windows 端启动）+ ADB

### 1. 后端启动

```bash
cd backend

# 配置环境变量
cp .env.example .env
# 填入以下 Key：
# KIMI_API_KEY   — 月之暗面：https://platform.moonshot.cn
# TAVILY_API_KEY — Tavily：https://tavily.com（1000次/月免费）
# DATABASE_URL   — PostgreSQL 连接字符串（见 docker-compose.yml）

# 启动 PostgreSQL
docker compose up -d

# 执行数据库迁移
npm run migrate

# 启动开发服务器（port 3000）
npm run dev
```

### 2. Android 真机调试

```bash
# Windows PowerShell — 建立 ADB 反向隧道
adb reverse tcp:3000 tcp:3000

# WSL — 编译 Debug APK
cd android && ./gradlew assembleDebug

# 安装到真机
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. WebView UI 构建（可选，已预构建）

```bash
cd webview-ui
npm install
npm run build
# 产物自动输出到 android/app/src/main/assets/www/
```

---

## 重要技术决策记录

| 问题 | 结论 |
|------|------|
| kimi-k2.5 多步工具调用返回 400 | 注入 `reasoning_content: "."` 占位符（见亮点 1） |
| WebView 白屏无报错 | Vite 插件去除 `type="module"`，改 IIFE + defer（见亮点 2） |
| WSL 真机调试无法连接 | `adb reverse` 在 Windows 执行，后端监听 `0.0.0.0`（见亮点 4） |
| OkHttp 流式读取异常 | `response.use{}` + 捕获 `IOException`，不用 `exhausted()` |
| Compose 输入框键盘空白 | 两阶段修复：Manifest 加 `adjustResize` + NavHost 加 `consumeWindowInsets` |
| 导航栏高度冗余 | Material 3 默认 80dp → 62dp；图标 22→20dp；标签 11→10sp |

---

## License

MIT © 2026 Tally Dev
