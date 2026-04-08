import { streamText, CoreMessage } from 'ai';
import { createKimiClient, KIMI_MODEL } from './kimiClient';
import { scheduleTools } from '../domains/schedule/scheduleTools';
import { expenseTools } from '../domains/expense/expenseTools';
import { searchTools } from '../domains/search/searchTools';

/**
 * Tally Orchestrator Agent 系统提示词
 * 负责理解用户意图，协调日程域和费用域操作
 */
function buildSystemPrompt(): string {
  const now = new Date();
  // 格式化为北京时间
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const weekday = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()];
  const currentDateStr = `${year}年${month}月${day}日（星期${weekday}）`;

  return `你是 Tally（对位）的 AI 助手，帮助用户通过自然语言管理日程和费用。

**当前日期**：${currentDateStr}

你的核心能力：
1. **日程管理**：创建、修改、删除、查询日程事件（createEvent / listEvents / updateEvent / deleteEvent / checkConflict）
2. **费用记录**：记录、分类、统计费用支出（createExpense / listExpenses）
3. **关联管理**：将费用与对应日程关联，查询活动总花费（linkExpenseToEvent / listExpensesByEvent）
4. **网络搜索**：查询实时信息（如展会日期、活动安排、最新新闻）时，调用 webSearch 工具获取准确数据

工具调用规范：
- 创建或修改日程前，先调用 checkConflict 检查时间冲突
- 有冲突时告知用户并等待确认
- 删除操作前告知用户被删内容，确认后再调用 deleteEvent
- 时间格式统一使用 ISO 8601，含时区（如 2026-04-07T09:00:00+08:00）
- 用户未提供具体时间时，根据上下文合理推断或询问
- 记录费用时若用户提到了关联的日程，主动调用 linkExpenseToEvent 建立关联
- 涉及实时/最新信息时（如"今年的XX展是什么时候"），优先调用 webSearch 搜索，而非依赖训练数据

回复规范：
- 操作完成后用自然语言告知结果，不暴露内部 ID 等技术细节（除非用户询问）
- 简洁清晰，支持中文和英文`;
}

/**
 * 流式对话函数
 * 接受消息历史，返回 streamText 结果对象
 */
export function streamChat(messages: CoreMessage[]) {
  const kimi = createKimiClient();

  return streamText({
    model: kimi(KIMI_MODEL),
    system: buildSystemPrompt(),
    messages,
    temperature: 1, // kimi-k2.5 仅支持 temperature=1
    tools: { ...scheduleTools, ...expenseTools, ...searchTools },
    maxSteps: 6, // +1 为 webSearch → createEvent 这类搜索后建议链
    onError: ({ error }) => {
      console.error('[Orchestrator] streamText 错误:', error);
    },
  });
}
