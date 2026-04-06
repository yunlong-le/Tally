import { Event, Expense, ApiResponse } from '../types';

const BASE_URL = 'http://localhost:3000';

async function apiFetch<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`);
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  const body: ApiResponse<T> = await res.json();
  if (!body.success || !body.data) {
    throw new Error(body.error?.message ?? 'API 错误');
  }
  return body.data;
}

export function fetchEvents(startAfter: string, startBefore: string): Promise<Event[]> {
  const params = new URLSearchParams({
    startAfter,
    startBefore,
    limit: '100',
  });
  return apiFetch<Event[]>(`/api/events?${params.toString()}`);
}

export function fetchExpenses(startDate: string, endDate: string): Promise<Expense[]> {
  const params = new URLSearchParams({
    startDate,
    endDate,
    limit: '100',
  });
  return apiFetch<Expense[]>(`/api/expenses?${params.toString()}`);
}
