import { QueryResultRow } from 'pg';
import { query } from '../../infrastructure/db';
import {
  Event,
  CreateEventInput,
  UpdateEventInput,
  ListEventsFilter,
} from './types';

/** 数据库行映射到 Event 对象 */
interface EventRow extends QueryResultRow {
  id: string;
  title: string;
  start_time: Date;
  end_time: Date | null;
  location: string | null;
  description: string | null;
  created_at: Date;
  updated_at: Date;
}

function rowToEvent(row: EventRow): Event {
  return {
    id: row.id,
    title: row.title,
    startTime: row.start_time,
    endTime: row.end_time,
    location: row.location,
    description: row.description,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
  };
}

/** 创建日程 */
export async function createEvent(input: CreateEventInput): Promise<Event> {
  const result = await query<EventRow>(
    `INSERT INTO events (title, start_time, end_time, location, description)
     VALUES ($1, $2, $3, $4, $5)
     RETURNING *`,
    [
      input.title,
      input.startTime,
      input.endTime ?? null,
      input.location ?? null,
      input.description ?? null,
    ]
  );
  return rowToEvent(result.rows[0]);
}

/** 查询日程（支持时间范围过滤）*/
export async function listEvents(filter: ListEventsFilter = {}): Promise<Event[]> {
  const conditions: string[] = [];
  const params: unknown[] = [];

  if (filter.startAfter) {
    params.push(filter.startAfter);
    conditions.push(`start_time >= $${params.length}`);
  }
  if (filter.startBefore) {
    params.push(filter.startBefore);
    conditions.push(`start_time <= $${params.length}`);
  }

  const where = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';
  const limit = filter.limit ?? 50;
  params.push(limit);

  const result = await query<EventRow>(
    `SELECT * FROM events ${where} ORDER BY start_time ASC LIMIT $${params.length}`,
    params
  );
  return result.rows.map(rowToEvent);
}

/** 按 ID 查询日程 */
export async function getEventById(id: string): Promise<Event | null> {
  const result = await query<EventRow>(
    'SELECT * FROM events WHERE id = $1',
    [id]
  );
  return result.rows.length > 0 ? rowToEvent(result.rows[0]) : null;
}

/** 更新日程 */
export async function updateEvent(id: string, input: UpdateEventInput): Promise<Event | null> {
  const setClauses: string[] = [];
  const params: unknown[] = [];

  if (input.title !== undefined) {
    params.push(input.title);
    setClauses.push(`title = $${params.length}`);
  }
  if (input.startTime !== undefined) {
    params.push(input.startTime);
    setClauses.push(`start_time = $${params.length}`);
  }
  if (input.endTime !== undefined) {
    params.push(input.endTime);
    setClauses.push(`end_time = $${params.length}`);
  }
  if (input.location !== undefined) {
    params.push(input.location);
    setClauses.push(`location = $${params.length}`);
  }
  if (input.description !== undefined) {
    params.push(input.description);
    setClauses.push(`description = $${params.length}`);
  }

  if (setClauses.length === 0) return getEventById(id);

  params.push(id);
  const result = await query<EventRow>(
    `UPDATE events SET ${setClauses.join(', ')} WHERE id = $${params.length} RETURNING *`,
    params
  );
  return result.rows.length > 0 ? rowToEvent(result.rows[0]) : null;
}

/** 删除日程 */
export async function deleteEvent(id: string): Promise<boolean> {
  const result = await query(
    'DELETE FROM events WHERE id = $1',
    [id]
  );
  return (result.rowCount ?? 0) > 0;
}

/** 检查时间段冲突 */
export async function findConflicts(
  startTime: string,
  endTime: string,
  excludeId?: string
): Promise<Event[]> {
  const params: unknown[] = [startTime, endTime];
  let excludeClause = '';

  if (excludeId) {
    params.push(excludeId);
    excludeClause = `AND id != $${params.length}`;
  }

  // 检测重叠条件：现有事件的开始 < 新事件结束 AND 现有事件的结束 > 新事件开始
  const result = await query<EventRow>(
    `SELECT * FROM events
     WHERE start_time < $2
       AND (end_time IS NULL OR end_time > $1)
       ${excludeClause}
     ORDER BY start_time ASC`,
    params
  );
  return result.rows.map(rowToEvent);
}
