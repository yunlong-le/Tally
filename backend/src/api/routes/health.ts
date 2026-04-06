import { Router, Request, Response } from 'express';
import { pool } from '../../infrastructure/db';

const router = Router();

/**
 * 健康检查端点
 * 检查 API 和数据库连接状态
 */
router.get('/health', async (_req: Request, res: Response) => {
  try {
    // 测试数据库连接
    await pool.query('SELECT 1');

    res.status(200).json({
      status: 'ok',
      timestamp: new Date().toISOString(),
      database: 'connected',
      service: 'tally-backend'
    });
  } catch (error) {
    console.error('健康检查失败:', error);
    res.status(503).json({
      status: 'error',
      timestamp: new Date().toISOString(),
      database: 'disconnected',
      message: '数据库连接失败'
    });
  }
});

/**
 * 就绪检查端点
 * 用于 Kubernetes readiness probe
 */
router.get('/ready', async (_req: Request, res: Response) => {
  try {
    const result = await pool.query('SELECT NOW()');
    if (result.rows.length > 0) {
      res.status(200).json({ ready: true });
    } else {
      res.status(503).json({ ready: false });
    }
  } catch (error) {
    res.status(503).json({ ready: false });
  }
});

export default router;
