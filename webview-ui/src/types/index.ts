export interface Event {
  id: string;
  title: string;
  startTime: string; // ISO 8601
  endTime: string | null;
  location: string | null;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Expense {
  id: string;
  description: string;
  amount: number;
  category: string | null;
  expenseDate: string; // ISO 8601
  createdAt: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
  metadata?: { total: number; limit: number };
}
