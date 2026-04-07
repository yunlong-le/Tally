# Tally（对位）— 技术亮点文档

**AI 驱动的 Android 日程 + 费用管理 App**，面向大模型/智能体开发岗位作品集展示。  
技术栈：Kotlin + Jetpack Compose / Node.js + Vercel AI SDK / kimi-k2.5 / PostgreSQL。  
核心能力：Multi-Agent 链式工具调用、流式响应、WebView 跨层通信、真机端到端验证。

---

## 系统架构

```
┌─────────────────────────────────────────────────────┐
│                  Android App (Kotlin)                │
│                                                     │
│  ┌────────────┐  ┌──────────────┐  ┌─────────────┐ │
│  │ ChatScreen │  │ CalendarView │  │ ExpenseView │ │
│  │ (Compose)  │  │  (WebView)   │  │  (WebView)  │ │
│  └─────┬──────┘  └──────┬───────┘  └──────┬──────┘ │
│        │                └──────────────────┘        │
│        │           TallyJsBridge (@JavascriptInterface)
│        │                    │                        │
│  ChatViewModel ◄────────────┘                        │
│  (StateFlow)                                         │
│        │                                            │
│  TallyApiClient (OkHttp 流式 SSE)                    │
└────────┼────────────────────────────────────────────┘
         │ POST /api/chat (Vercel AI Data Stream)
         │
┌────────▼────────────────────────────────────────────┐
│               Backend (Node.js + TypeScript)         │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │           Orchestrator Agent                  │  │
│  │  streamText(maxSteps=5, tools=[...子Agent工具]) │  │
│  └──────────┬───────────────┬────────────────────┘  │
│             │               │                        │
│  ┌──────────▼──────┐  ┌─────▼──────────────────┐   │
│  │  Schedule Agent │  │    Expense Agent         │   │
│  │  - createEvent  │  │  - createExpense         │   │
│  │  - listEvents   │  │  - listExpenses          │   │
│  │  - updateEvent  │  │  - linkExpenseToEvent    │   │
│  │  - deleteEvent  │  │  - listExpensesByEvent   │   │
│  │  - checkConflict│  └────────────┬─────────────┘  │
│  └──────────┬──────┘               │                 │
│             └──────────┬───────────┘                 │
│                        │                             │
│  ┌─────────────────────▼──────────────────────────┐ │
│  │       PostgreSQL (Docker)                       │ │
│  │       events / expenses / event_expense_links  │ │
│  └────────────────────────────────────────────────┘ │
│                                                     │
│  kimiCompatFetch 拦截器                              │
│  (注入 reasoning_content 占位符)                     │
└─────────────────────────────────────────────────────┘
         │
   月之暗面 Kimi API
   kimi-k2.5（OpenAI-compatible）
```

---

## 技术亮点

### 1. kimi-k2.5 多步工具调用兼容性攻坚

**问题背景**：Vercel AI SDK 的 `streamText(maxSteps=N)` 在多步工具调用时，会重建消息历史传给下一步。然而 kimi-k2.5 是 thinking 模型，要求每条 `assistant` 消息必须携带 `reasoning_content` 字段；SDK 重建的历史消息中该字段丢失，导致 Kimi 返回 HTTP 400。

**根因定位**：
- Vercel AI SDK 不感知第三方模型对 `reasoning_content` 的强制要求
- 空字符串 `""` 同样会触发 400，必须是非空占位符
- 多步链中每一条历史 assistant 消息都需要修复，而非仅最后一条

**解决方案**：自定义 `kimiCompatFetch` 拦截器，在 `fetch` 层面拦截每次发往 Kimi 的请求，遍历 `messages` 数组，对 `role === "assistant"` 且缺少 `reasoning_content` 的条目注入占位符 `"."`：

```typescript
// backend/src/agents/kimiClient.ts（核心逻辑）
const kimiCompatFetch: FetchFunction = async (url, options) => {
  const body = JSON.parse(options.body as string);
  body.messages = body.messages.map((msg: Message) => {
    if (msg.role === "assistant" && !msg.reasoning_content) {
      return { ...msg, reasoning_content: "." };  // 非空占位符，空串会 400
    }
    return msg;
  });
  return fetch(url, { ...options, body: JSON.stringify(body) });
};
```

**验证**：`checkConflict → createEvent` 链式调用（2步），`listEvents → deleteEvent×N`（N+1 步）稳定工作，无 400 错误。

**体现能力**：深入理解 AI SDK 消息流重建机制，能在 SDK 与第三方模型的兼容性缝隙中精准定位问题并实现非侵入式修复。

---

### 2. Android WebView ES Module 兼容性

**问题背景**：使用 `WebViewAssetLoader` 加载本地 React 打包产物时，Vite 默认生成带 `type="module" crossorigin` 的 `<script>` 标签，在 Android WebView 下导致页面白屏，且无任何 JS 错误输出。

**根因定位**：Android WebView 对 ES Module + CORS 属性的支持不一致，`crossorigin` 在本地资产加载场景下触发意外的跨域检查，`type="module"` 的模块解析路径与 `WebViewAssetLoader` 的虚拟域名路由存在冲突。

**解决方案**：
1. 在 `vite.config.ts` 中编写自定义 plugin，后处理 `index.html`，正则去除所有 `type="module"` 和 `crossorigin` 属性，并将 `<script>` 改为带 `defer` 的普通脚本
2. 构建输出格式从 ESM 改为 **IIFE**（`build.lib.formats: ['iife']`），消除模块依赖
3. NavGraph 中对应 WebView 增加 `mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW`

**体现能力**：跨越前端构建工具与移动端 WebView 的技术边界，具备全栈调试能力。

---

### 3. WebView Bridge 跨层通信

**功能**：用户在日历/费用 WebView 中点击任意条目，App 自动跳转聊天 Tab，并在输入框预填带上下文的提示词（如"帮我查一下这个日程的相关费用"），用户直接发送即可与 AI 交互。

**实现链路**：

```
WebView JS (React)
  └─ window.TallyBridge.openChat(contextMessage)
       └─ @JavascriptInterface (TallyJsBridge)
            └─ Handler(Looper.getMainLooper()).post { ... }  ← 切回主线程
                 └─ chatViewModel.prefillMessage(msg)
                      └─ navController.navigate("chat")
```

**关键细节**：`@JavascriptInterface` 注解的方法由 WebView 在**后台线程**调用，直接操作 UI 或 ViewModel 会崩溃；必须通过 `Handler(Looper.getMainLooper())` 切回主线程。共享 `ChatViewModel` 在 Tab 切换时保持消息历史，不因导航重组而丢失。

**体现能力**：理解 Android 多线程模型与 Compose 状态生命周期，能设计跨层（Native ↔ WebView ↔ AI）的完整交互链路。

---

### 4. WSL 真机调试网络隔离突破

**问题背景**：后端运行在 WSL2 Linux 内，Android 真机连接 Windows USB，二者处于不同的网络命名空间，手机无法直接访问 WSL 内的 localhost:3000。

**解决方案**：
- 后端监听 `0.0.0.0`（而非 127.0.0.1），允许所有网络接口
- 在 **Windows PowerShell** 执行 `adb reverse tcp:3000 tcp:3000`，将手机端口反向代理到 Windows localhost
- Windows localhost → WSL localhost 的流量由 WSL2 网络栈自动桥接

**最终链路**：`手机 localhost:3000 → ADB 反向隧道 → Windows localhost:3000 → WSL2 桥接 → 后端进程`

**体现能力**：熟悉 WSL2 网络拓扑与 ADB 调试工具链，能在复杂开发环境中快速建立端到端验证链路。

---

## Demo 场景（展示 Agent 能力边界）

| # | 输入 | Agent 调用链 | 体现能力 |
|---|------|-------------|---------|
| 1 | "4月18号，我要去参加AI研讨会，帮我记录" | `createEvent` | 单步工具调用，时间解析 |
| 2 | "帮我看北京车展是什么时候，提前两天到北京" | Kimi 内置搜索 → `createEvent` | 知识检索 + 推理 + 工具调用 |
| 3 | "下礼拜一清空所有行程，帮我请假" | `listEvents` → `deleteEvent` × N | 多步调用，动态工具数量 |
| 4 | "明天看一下我什么时间有空，安排业务规划会" | `listEvents` → `checkConflict` → `createEvent` | 3步链式推理，冲突感知 |
| 5 | "出差代驾200块，帮我记录并关联出差行程" | `createExpense` → `linkExpenseToEvent` | 跨域 Agent 协作（费用+日程） |
| 6 | "今天给团队买咖啡花了150，帮我记一下" | `createExpense` | 费用域单步，自然语言解析金额 |

场景 3、4 是核心压力测试：场景 3 要求 Orchestrator 动态决策工具调用次数；场景 4 是三步依赖链，每步的输出作为下一步的输入，任意一步错误均会导致最终结果错误。这两个场景的稳定运行，直接依赖上述 kimi-k2.5 兼容性修复。

---

## 一句话总结

Tally 不是 AI 功能的简单调用展示，而是在真实的工程约束下——SDK 与模型兼容性、移动端 WebView 限制、跨进程网络隔离——从零构建完整 Multi-Agent 系统，并在真机上完成端到端验证，体现了对大模型 API 机制、Agent 工具调用链路和全栈工程能力的深度掌握。
