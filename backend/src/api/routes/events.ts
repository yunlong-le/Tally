import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { listEvents } from '../../domains/schedule/scheduleRepository';

const router = Router();

const eventsQuerySchema = z.object({
  startAfter: z.string().datetime({ offset: true }).optional(),
  startBefore: z.string().datetime({ offset: true }).optional(),
  limit: z.coerce.number().int().min(1).max(200).optional(),
});

/**
 * GET /api/events
 * 查询日程列表
 * Query: startAfter (ISO 8601), startBefore (ISO 8601), limit (1-200, 默认 50)
 */
router.get('/events', async (req: Request, res: Response) => {
  const parsed = eventsQuerySchema.safeParse(req.query);
  if (!parsed.success) {
    res.status(400).json({
      success: false,
      error: {
        code: 'VALIDATION_ERROR',
        message: '查询参数格式错误',
        details: parsed.error.flatten(),
      },
    });
    return;
  }

  const { startAfter, startBefore, limit } = parsed.data;

  try {
    const events = await listEvents({ startAfter, startBefore, limit });
    res.status(200).json({
      success: true,
      data: events,
      metadata: { total: events.length, limit: limit ?? 50 },
    });
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : '未知错误';
    res.status(500).json({
      success: false,
      error: { code: 'DB_ERROR', message },
    });
  }
});

export default router;
