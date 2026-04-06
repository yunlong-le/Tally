/**
 * API 响应类型定义
 * 遵循统一的响应格式规范
 */

/**
 * API 成功响应格式
 */
export interface ApiSuccessResponse<T = any> {
  success: true;
  status: 'ok' | 'created' | 'accepted';
  data: T;
  timestamp: string;
  metadata?: {
    total?: number;
    page?: number;
    limit?: number;
  };
}

/**
 * API 错误响应格式
 */
export interface ApiErrorResponse {
  success: false;
  status: 'error' | 'validation_error' | 'not_found' | 'unauthorized' | 'forbidden';
  error: {
    code: string;
    message: string;
    details?: Record<string, any>;
  };
  timestamp: string;
}

/**
 * API 响应类型（联合类型）
 */
export type ApiResponse<T = any> = ApiSuccessResponse<T> | ApiErrorResponse;

/**
 * 分页响应
 */
export interface PaginatedData<T> {
  items: T[];
  total: number;
  page: number;
  limit: number;
  hasMore: boolean;
}

/**
 * 数据库错误
 */
export class DatabaseError extends Error {
  constructor(
    public code: string,
    message: string,
    public details?: Record<string, any>
  ) {
    super(message);
    this.name = 'DatabaseError';
  }
}

/**
 * 验证错误
 */
export class ValidationError extends Error {
  constructor(
    message: string,
    public details?: Record<string, any>
  ) {
    super(message);
    this.name = 'ValidationError';
  }
}
