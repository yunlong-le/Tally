import { useState, useEffect } from 'react';
import { Expense } from '../types';
import { fetchExpenses } from '../api/client';
import '../styles/expense.css';

function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function endOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}

function formatDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatMonthYear(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

interface ExpensesByCategory {
  [key: string]: Expense[];
}

export default function ExpenseView() {
  const [month, setMonth] = useState(new Date());
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadExpenses = () => {
    setLoading(true);
    setError(null);
    const startDate = formatDate(startOfMonth(month));
    const endDate = formatDate(endOfMonth(month));
    fetchExpenses(startDate, endDate)
      .then(data => {
        setExpenses(data);
      })
      .catch(err => {
        const errMsg = err instanceof Error ? err.message : String(err);
        setError('加载失败: ' + errMsg);
        console.error('Failed to fetch expenses:', err);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadExpenses();
  }, [month]);

  const handlePrevMonth = () => {
    setMonth(new Date(month.getFullYear(), month.getMonth() - 1));
  };

  const handleNextMonth = () => {
    setMonth(new Date(month.getFullYear(), month.getMonth() + 1));
  };

  const total = expenses.reduce((sum, e) => sum + e.amount, 0);

  // 按分类分组
  const byCategory: ExpensesByCategory = {};
  expenses.forEach(expense => {
    const category = expense.category ?? '其他';
    if (!byCategory[category]) {
      byCategory[category] = [];
    }
    byCategory[category].push(expense);
  });

  return (
    <div className="expense-view">
      <div className="expense-header">
        <button onClick={handlePrevMonth} className="btn-nav">←</button>
        <h1>{formatMonthYear(month)}</h1>
        <button onClick={handleNextMonth} className="btn-nav">→</button>
      </div>

      {loading && <p className="loading">加载中...</p>}

      {error && (
        <div className="error-container" style={{ padding: '16px', background: '#ff444422', borderRadius: '8px', margin: '16px 0' }}>
          <p style={{ color: '#ff6666', margin: '0 0 8px 0' }}>{error}</p>
          <button
            onClick={loadExpenses}
            style={{
              padding: '8px 16px',
              background: '#1DB954',
              color: '#000',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            重试
          </button>
        </div>
      )}

      <div className="summary-card">
        <div className="total">
          <span className="label">总支出</span>
          <span className="amount">¥{total.toFixed(2)}</span>
        </div>
        <div className="categories">
          {Object.entries(byCategory).map(([category, items]) => (
            <div key={category} className="category-chip">
              <span className="category-name">{category}</span>
              <span className="category-amount">¥{items.reduce((s, e) => s + e.amount, 0).toFixed(2)}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="expense-list">
        <h2>消费详情</h2>
        {expenses.length > 0 ? (
          <ul>
            {expenses.map(expense => (
              <li
                key={expense.id}
                className="expense-item"
              >
                <div className="expense-info">
                  <div className="expense-description">{expense.description}</div>
                  <div className="expense-meta">
                    {expense.category && <span className="category-badge">{expense.category}</span>}
                    <span className="date">
                      {new Date(expense.expenseDate).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })}
                    </span>
                  </div>
                  <div
                    className="ask-ai-chip"
                    onClick={() => {
                      const date = new Date(expense.expenseDate).toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' });
                      const categoryStr = expense.category ? `，分类：${expense.category}` : '';
                      const msg = `帮我分析这笔消费：${expense.description}，金额 ¥${expense.amount.toFixed(2)}，日期${date}${categoryStr}`;
                      (window as any).Android?.navigateToChat(msg);
                    }}
                  >
                    🤖 问AI
                  </div>
                </div>
                <div className="expense-amount">¥{expense.amount.toFixed(2)}</div>
              </li>
            ))}
          </ul>
        ) : (
          <p className="no-expenses">暂无消费记录</p>
        )}
      </div>
    </div>
  );
}
