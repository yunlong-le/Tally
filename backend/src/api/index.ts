import express, { Express } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import healthRouter from './routes/health';
import chatRouter from './routes/chat';
import eventsRouter from './routes/events';
import expensesRouter from './routes/expenses';

// 加载环境变量
dotenv.config();

const app: Express = express();
const PORT = parseInt(process.env.PORT || '3000', 10);

// ============================================================================
// 中间件配置
// ============================================================================

// CORS 支持
app.use(cors());

// JSON 请求体解析
app.use(express.json({ limit: '10mb' }));

// URL-encoded 请求体解析
app.use(express.urlencoded({ limit: '10mb', extended: true }));

// 请求日志中间件
app.use((req, _res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// ============================================================================
// 路由配置
// ============================================================================

// 健康检查路由
app.use('/api', healthRouter);

// 对话路由
app.use('/api', chatRouter);

// 日程查询路由
app.use('/api', eventsRouter);

// 费用查询路由
app.use('/api', expensesRouter);

// 根路由
app.get('/', (_req, res) => {
  res.json({
    name: 'Tally Backend',
    version: '1.0.0',
    description: '对位项目后端服务',
    endpoints: {
      health: '/api/health',
      ready: '/api/ready',
      events: '/api/events',
      expenses: '/api/expenses',
      chat: '/api/chat'
    }
  });
});

// 404 处理
app.use((req, res) => {
  res.status(404).json({
    error: 'Not Found',
    path: req.path,
    method: req.method
  });
});

// ============================================================================
// 错误处理
// ============================================================================

app.use((err: Error, _req: express.Request, res: express.Response, _next: express.NextFunction) => {
  console.error('服务器错误:', err);
  res.status(500).json({
    error: 'Internal Server Error',
    message: process.env.NODE_ENV === 'development' ? err.message : '服务器内部错误'
  });
});

// ============================================================================
// 服务启动
// ============================================================================

app.listen(PORT, "0.0.0.0", () => {
  console.log(`
╔════════════════════════════════════════════════════════════╗
║         Tally 后端服务已启动                              ║
║         ${`http://localhost:${PORT}`.padEnd(50, ' ')}║
╚════════════════════════════════════════════════════════════╝
  `);
  console.log('环境变量:');
  console.log(`  NODE_ENV: ${process.env.NODE_ENV || 'development'}`);
  console.log(`  PORT: ${PORT}`);
  console.log(`  DATABASE_URL: ${process.env.DATABASE_URL ? '已配置' : '未配置'}`);
});

// 优雅关闭
process.on('SIGTERM', () => {
  console.log('收到 SIGTERM 信号，开始优雅关闭...');
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('收到 SIGINT 信号，开始优雅关闭...');
  process.exit(0);
});

export default app;
