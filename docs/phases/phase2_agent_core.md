# Phase 2: Agent 核心

**状态**: ⬜ 待开始  
**前置条件**: Phase 1 全部完成

## 任务清单

- ⬜ 2.1 集成 Vercel AI SDK + Kimi (OpenAI-compatible)
- ⬜ 2.2 Schedule Agent 工具集
- ⬜ 2.3 Expense Agent 工具集
- ⬜ 2.4 Web Search 工具（Tavily API）
- ⬜ 2.5 Orchestrator Agent
- ⬜ 2.6 Plan & Execute 确认流
- ⬜ 2.7 SSE 流式输出
- ⬜ 2.8 对话历史管理

## 验收标准

- curl 发消息，后端能返回 plan_card JSON
- Agent 能正确调用 schedule 工具创建/删除事件
- SSE 流式输出正常工作
