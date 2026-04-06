/**
 * 日程域类型定义
 * 与数据库 events 表对应
 */

export interface Event {
  id: string;
  title: string;
  startTime: Date;
  endTime: Date | null;
  location: string | null;
  description: string | null;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateEventInput {
  title: string;
  startTime: string; // ISO 8601 格式
  endTime?: string;
  location?: string;
  description?: string;
}

export interface UpdateEventInput {
  title?: string;
  startTime?: string;
  endTime?: string;
  location?: string;
  description?: string;
}

export interface ListEventsFilter {
  startAfter?: string; // ISO 8601
  startBefore?: string; // ISO 8601
  limit?: number;
}

export interface ConflictCheckResult {
  hasConflict: boolean;
  conflictingEvents: Event[];
}
