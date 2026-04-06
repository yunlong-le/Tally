import { tool } from 'ai';
import { z } from 'zod';
import * as repo from './expenseRepository';

/** 记录费用 */
export const createExpenseTool = tool({
  description: '记录一笔费用支出。需要描述、金额和日期，分类可选。',
  parameters: z.object({
    description: z.string().describe('费用描述，如"午餐"、"打车"、"会议室预订费"'),
    amount: z.number().positive().describe('金额（人民币元）'),
    category: z.string().optional().describe('费用分类，如"餐饮"、"交通"、"办公"、"住宿"'),
    expenseDate: z.string().describe('消费日期，ISO 8601 格式，如 2026-04-07T12:00:00+08:00'),
  }),
  execute: async (input) => {
    const expense = await repo.createExpense(input);
    return {
      success: true,
      expense: {
        id: expense.id,
        description: expense.description,
        amount: expense.amount,
        category: expense.category,
        expenseDate: expense.expenseDate.toISOString(),
      },
    };
  },
});

/** 查询费用列表 */
export const listExpensesTool = tool({
  description: '查询费用记录列表，可按分类和日期范围过滤，默认返回最近 50 条。',
  parameters: z.object({
    category: z.string().optional().describe('按分类过滤，如"餐饮"、"交通"'),
    startDate: z.string().optional().describe('查询此日期之后的费用，ISO 8601'),
    endDate: z.string().optional().describe('查询此日期之前的费用，ISO 8601'),
    limit: z.number().int().min(1).max(100).optional().describe('返回数量上限，默认 50'),
  }),
  execute: async (filter) => {
    const expenses = await repo.listExpenses(filter);
    const total = expenses.reduce((sum, e) => sum + e.amount, 0);
    return {
      success: true,
      count: expenses.length,
      total: parseFloat(total.toFixed(2)),
      expenses: expenses.map((e) => ({
        id: e.id,
        description: e.description,
        amount: e.amount,
        category: e.category,
        expenseDate: e.expenseDate.toISOString(),
      })),
    };
  },
});

/** 将费用关联到日程 */
export const linkExpenseToEventTool = tool({
  description: '将一笔费用记录关联到某个日程事件，用于追踪活动产生的支出。',
  parameters: z.object({
    expenseId: z.string().uuid().describe('费用记录的 ID'),
    eventId: z.string().uuid().describe('日程事件的 ID'),
  }),
  execute: async ({ expenseId, eventId }) => {
    await repo.linkExpenseToEvent(expenseId, eventId);
    return { success: true, expenseId, eventId };
  },
});

/** 查询日程关联的费用 */
export const listExpensesByEventTool = tool({
  description: '查询某个日程事件关联的所有费用记录及总金额。',
  parameters: z.object({
    eventId: z.string().uuid().describe('日程事件的 ID'),
  }),
  execute: async ({ eventId }) => {
    const expenses = await repo.listExpensesByEvent(eventId);
    const total = expenses.reduce((sum, e) => sum + e.amount, 0);
    return {
      success: true,
      eventId,
      count: expenses.length,
      total: parseFloat(total.toFixed(2)),
      expenses: expenses.map((e) => ({
        id: e.id,
        description: e.description,
        amount: e.amount,
        category: e.category,
        expenseDate: e.expenseDate.toISOString(),
      })),
    };
  },
});

export const expenseTools = {
  createExpense: createExpenseTool,
  listExpenses: listExpensesTool,
  linkExpenseToEvent: linkExpenseToEventTool,
  listExpensesByEvent: listExpensesByEventTool,
};
