import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { listExpenses } from '../../domains/expense/expenseRepository';

const router = Router();

const expensesQuerySchema = z.object({
  category: z.string().min(1).optional(),
  startDate: z.string().date().optional(),   // YYYY-MM-DD
  endDate: z.string().date().optional(),     // YYYY-MM-DD
  limit: z.coerce.number().int().min(1).max(200).optional(),
});

/**
 * GET /api/expenses
 * 查询费用列表
 * Query: category, startDate (YYYY-MM-DD), endDate (YYYY-MM-DD), limit (1-200, 默认 50)
 */
router.get('/expenses', async (req: Request, res: Response) => {
  const parsed = expensesQuerySchema.safeParse(req.query);
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

  const { category, startDate, endDate, limit } = parsed.data;

  try {
    const expenses = await listExpenses({ category, startDate, endDate, limit });
    res.status(200).json({
      success: true,
      data: expenses,
      metadata: { total: expenses.length, limit: limit ?? 50 },
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
