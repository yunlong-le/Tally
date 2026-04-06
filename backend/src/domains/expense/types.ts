/**
 * 费用域类型定义
 * 与数据库 expenses 表对应
 */

export interface Expense {
  id: string;
  description: string;
  amount: number;
  category: string | null;
  expenseDate: Date;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateExpenseInput {
  description: string;
  amount: number;
  category?: string;
  expenseDate: string; // ISO 8601
}

export interface ListExpensesFilter {
  category?: string;
  startDate?: string; // ISO 8601
  endDate?: string;   // ISO 8601
  limit?: number;
}
