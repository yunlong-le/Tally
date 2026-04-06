import { tool } from 'ai';
import { z } from 'zod';
import * as repo from './scheduleRepository';

/**
 * 日程管理工具集（供 Orchestrator Agent 使用）
 * 每个 tool 对应一个数据库操作，AI 自动决策何时调用
 */

/** 创建日程 */
export const createEventTool = tool({
  description: '创建新的日程事件。需要标题和开始时间，结束时间、地点、描述可选。',
  parameters: z.object({
    title: z.string().describe('日程标题'),
    startTime: z.string().describe('开始时间，ISO 8601 格式，如 2026-04-07T09:00:00+08:00'),
    endTime: z.string().optional().describe('结束时间，ISO 8601 格式'),
    location: z.string().optional().describe('地点'),
    description: z.string().optional().describe('备注或描述'),
  }),
  execute: async (input) => {
    const event = await repo.createEvent(input);
    return {
      success: true,
      event: {
        id: event.id,
        title: event.title,
        startTime: event.startTime.toISOString(),
        endTime: event.endTime?.toISOString() ?? null,
        location: event.location,
        description: event.description,
      },
    };
  },
});

/** 查询日程列表 */
export const listEventsTool = tool({
  description: '查询日程列表。可按时间范围过滤，默认返回最近 50 条。',
  parameters: z.object({
    startAfter: z.string().optional().describe('查询此时间之后的日程，ISO 8601'),
    startBefore: z.string().optional().describe('查询此时间之前的日程，ISO 8601'),
    limit: z.number().int().min(1).max(100).optional().describe('返回数量上限，默认 50'),
  }),
  execute: async (filter) => {
    const events = await repo.listEvents(filter);
    return {
      success: true,
      count: events.length,
      events: events.map((e) => ({
        id: e.id,
        title: e.title,
        startTime: e.startTime.toISOString(),
        endTime: e.endTime?.toISOString() ?? null,
        location: e.location,
        description: e.description,
      })),
    };
  },
});

/** 更新日程 */
export const updateEventTool = tool({
  description: '修改已有日程的标题、时间、地点或描述。需要提供日程 ID。',
  parameters: z.object({
    id: z.string().uuid().describe('要修改的日程 ID'),
    title: z.string().optional().describe('新标题'),
    startTime: z.string().optional().describe('新开始时间，ISO 8601'),
    endTime: z.string().optional().describe('新结束时间，ISO 8601'),
    location: z.string().optional().describe('新地点'),
    description: z.string().optional().describe('新备注'),
  }),
  execute: async ({ id, ...updates }) => {
    const event = await repo.updateEvent(id, updates);
    if (!event) {
      return { success: false, error: `未找到 ID 为 ${id} 的日程` };
    }
    return {
      success: true,
      event: {
        id: event.id,
        title: event.title,
        startTime: event.startTime.toISOString(),
        endTime: event.endTime?.toISOString() ?? null,
        location: event.location,
        description: event.description,
      },
    };
  },
});

/** 删除日程 */
export const deleteEventTool = tool({
  description: '删除指定 ID 的日程事件。此操作不可撤销，执行前应向用户确认。',
  parameters: z.object({
    id: z.string().uuid().describe('要删除的日程 ID'),
  }),
  execute: async ({ id }) => {
    const deleted = await repo.deleteEvent(id);
    if (!deleted) {
      return { success: false, error: `未找到 ID 为 ${id} 的日程` };
    }
    return { success: true, deletedId: id };
  },
});

/** 检查时间冲突 */
export const checkConflictTool = tool({
  description: '检查指定时间段是否与已有日程冲突。创建或修改日程时可先调用此工具。',
  parameters: z.object({
    startTime: z.string().describe('检查开始时间，ISO 8601'),
    endTime: z.string().describe('检查结束时间，ISO 8601'),
    excludeId: z.string().uuid().optional().describe('排除此 ID 的日程（修改场景用）'),
  }),
  execute: async ({ startTime, endTime, excludeId }) => {
    const conflicts = await repo.findConflicts(startTime, endTime, excludeId);
    return {
      hasConflict: conflicts.length > 0,
      conflictCount: conflicts.length,
      conflictingEvents: conflicts.map((e) => ({
        id: e.id,
        title: e.title,
        startTime: e.startTime.toISOString(),
        endTime: e.endTime?.toISOString() ?? null,
      })),
    };
  },
});

/** 导出所有 schedule 工具（供 orchestrator 使用）*/
export const scheduleTools = {
  createEvent: createEventTool,
  listEvents: listEventsTool,
  updateEvent: updateEventTool,
  deleteEvent: deleteEventTool,
  checkConflict: checkConflictTool,
};
