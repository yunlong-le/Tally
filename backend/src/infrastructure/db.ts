import { Pool, QueryResult, QueryResultRow } from 'pg';
import dotenv from 'dotenv';

// 加载环境变量
dotenv.config();

/**
 * PostgreSQL 连接池
 * 用于与数据库的连接管理
 */
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  // 连接池配置
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

/**
 * 处理连接池错误
 */
pool.on('error', (err) => {
  console.error('数据库连接池错误:', err);
  process.exit(-1);
});

/**
 * 执行数据库查询
 * @param text SQL 查询语句
 * @param params 查询参数（可选）
 * @returns 查询结果
 */
export async function query<T extends QueryResultRow = QueryResultRow>(
  text: string,
  params?: unknown[]
): Promise<QueryResult<T>> {
  const start = Date.now();
  try {
    const result = await pool.query<T>(text, params);
    const duration = Date.now() - start;
    console.log(`[DB Query] 执行耗时: ${duration}ms`);
    return result;
  } catch (error) {
    console.error('数据库查询错误:', error);
    throw error;
  }
}

/**
 * 获取数据库连接（用于事务）
 */
export async function getConnection() {
  return await pool.connect();
}

/**
 * 关闭连接池
 */
export async function closePool(): Promise<void> {
  await pool.end();
}

export { pool };
