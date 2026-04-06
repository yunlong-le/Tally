import { QueryResultRow } from 'pg';
import { query } from '../../infrastructure/db';
import { Expense, CreateExpenseInput, ListExpensesFilter } from './types';

interface ExpenseRow extends QueryResultRow {
  id: string;
  description: string;
  amount: string; // pg DECIMAL 返回字符串
  category: string | null;
  expense_date: Date;
  created_at: Date;
  updated_at: Date;
}

function rowToExpense(row: ExpenseRow): Expense {
  return {
    id: row.id,
    description: row.description,
    amount: parseFloat(row.amount),
    category: row.category,
    expenseDate: row.expense_date,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
  };
}

/** 创建费用记录 */
export async function createExpense(input: CreateExpenseInput): Promise<Expense> {
  const result = await query<ExpenseRow>(
    `INSERT INTO expenses (description, amount, category, expense_date)
     VALUES ($1, $2, $3, $4)
     RETURNING *`,
    [input.description, input.amount, input.category ?? null, input.expenseDate]
  );
  return rowToExpense(result.rows[0]);
}

/** 查询费用列表 */
export async function listExpenses(filter: ListExpensesFilter = {}): Promise<Expense[]> {
  const conditions: string[] = [];
  const params: unknown[] = [];

  if (filter.category) {
    params.push(filter.category);
    conditions.push(`category = $${params.length}`);
  }
  if (filter.startDate) {
    params.push(filter.startDate);
    conditions.push(`expense_date >= $${params.length}`);
  }
  if (filter.endDate) {
    params.push(filter.endDate);
    conditions.push(`expense_date <= $${params.length}`);
  }

  const where = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';
  const limit = filter.limit ?? 50;
  params.push(limit);

  const result = await query<ExpenseRow>(
    `SELECT * FROM expenses ${where} ORDER BY expense_date DESC LIMIT $${params.length}`,
    params
  );
  return result.rows.map(rowToExpense);
}

/** 将费用关联到日程事件 */
export async function linkExpenseToEvent(
  expenseId: string,
  eventId: string
): Promise<boolean> {
  await query(
    `INSERT INTO event_expense_links (event_id, expense_id)
     VALUES ($1, $2)
     ON CONFLICT DO NOTHING`,
    [eventId, expenseId]
  );
  return true;
}

/** 查询日程关联的所有费用 */
export async function listExpensesByEvent(eventId: string): Promise<Expense[]> {
  const result = await query<ExpenseRow>(
    `SELECT e.* FROM expenses e
     JOIN event_expense_links l ON l.expense_id = e.id
     WHERE l.event_id = $1
     ORDER BY e.expense_date DESC`,
    [eventId]
  );
  return result.rows.map(rowToExpense);
}
