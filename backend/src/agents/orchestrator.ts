import { streamText, CoreMessage } from 'ai';
import { createKimiClient, KIMI_MODEL } from './kimiClient';
import { scheduleTools } from '../domains/schedule/scheduleTools';
import { expenseTools } from '../domains/expense/expenseTools';

/**
 * Tally Orchestrator Agent 系统提示词
 * 负责理解用户意图，协调日程域和费用域操作
 */
const SYSTEM_PROMPT = `你是 Tally（对位）的 AI 助手，帮助用户通过自然语言管理日程和费用。

你的核心能力：
1. **日程管理**：创建、修改、删除、查询日程事件（createEvent / listEvents / updateEvent / deleteEvent / checkConflict）
2. **费用记录**：记录、分类、统计费用支出（createExpense / listExpenses）
3. **关联管理**：将费用与对应日程关联，查询活动总花费（linkExpenseToEvent / listExpensesByEvent）

工具调用规范：
- 创建或修改日程前，先调用 checkConflict 检查时间冲突
- 有冲突时告知用户并等待确认
- 删除操作前告知用户被删内容，确认后再调用 deleteEvent
- 时间格式统一使用 ISO 8601，含时区（如 2026-04-07T09:00:00+08:00）
- 用户未提供具体时间时，根据上下文合理推断或询问
- 记录费用时若用户提到了关联的日程，主动调用 linkExpenseToEvent 建立关联

回复规范：
- 操作完成后用自然语言告知结果，不暴露内部 ID 等技术细节（除非用户询问）
- 简洁清晰，支持中文和英文`;

/**
 * 流式对话函数
 * 接受消息历史，返回 streamText 结果对象
 */
export function streamChat(messages: CoreMessage[]) {
  const kimi = createKimiClient();

  return streamText({
    model: kimi(KIMI_MODEL),
    system: SYSTEM_PROMPT,
    messages,
    temperature: 1, // kimi-k2.5 仅支持 temperature=1
    tools: { ...scheduleTools, ...expenseTools },
    maxSteps: 5, // 允许最多 5 轮 tool call（支持链式操作，如先 checkConflict 再 createEvent）
    onError: ({ error }) => {
      console.error('[Orchestrator] streamText 错误:', error);
    },
  });
}
