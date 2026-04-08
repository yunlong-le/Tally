import { useState, useEffect } from 'react';
import { Event } from '../types';
import { fetchEvents } from '../api/client';
import '../styles/calendar.css';

// 使用本地时间格式，避免 toISOString() 的 UTC 转换问题
function formatLocalDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function endOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59);
}

function formatMonthYear(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

function daysInMonth(date: Date): number {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
}

function firstDayOfMonth(date: Date): number {
  return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
}

export default function CalendarView() {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    const startAfter = formatLocalDate(startOfMonth(currentMonth)) + 'T00:00:00+08:00';
    const startBefore = formatLocalDate(endOfMonth(currentMonth)) + 'T23:59:59+08:00';
    fetchEvents(startAfter, startBefore)
      .then(setEvents)
      .catch(err => console.error('Failed to fetch events:', err))
      .finally(() => setLoading(false));
  }, [currentMonth]);

  const handlePrevMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1));
  };

  const handleNextMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1));
  };

  const days = daysInMonth(currentMonth);
  const firstDay = firstDayOfMonth(currentMonth);

  // 获取选中日期的事件
  const selectedDateStr = selectedDate ? formatLocalDate(selectedDate) : null;
  const eventsOnSelectedDate = selectedDateStr
    ? events.filter(e => e.startTime.slice(0, 10) === selectedDateStr)
    : [];

  return (
    <div className="calendar-view">
      <div className="calendar-header">
        <button onClick={handlePrevMonth} className="btn-nav">←</button>
        <h1>{formatMonthYear(currentMonth)}</h1>
        <button onClick={handleNextMonth} className="btn-nav">→</button>
      </div>

      {loading && <p className="loading">加载中...</p>}

      <div className="month-grid">
        <div className="weekdays">
          <div className="weekday">日</div>
          <div className="weekday">一</div>
          <div className="weekday">二</div>
          <div className="weekday">三</div>
          <div className="weekday">四</div>
          <div className="weekday">五</div>
          <div className="weekday">六</div>
        </div>
        <div className="days">
          {Array.from({ length: firstDay }).map((_, i) => (
            <div key={`empty-${i}`} className="day empty" />
          ))}
          {Array.from({ length: days }).map((_, i) => {
            const day = i + 1;
            const date = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
            const dateStr = formatLocalDate(date);
            const dayEvents = events.filter(e => e.startTime.slice(0, 10) === dateStr);
            const isSelected = selectedDate && formatLocalDate(selectedDate) === dateStr;

            return (
              <div
                key={day}
                className={`day ${isSelected ? 'selected' : ''} ${dayEvents.length > 0 ? 'has-events' : ''}`}
                onClick={() => setSelectedDate(date)}
              >
                <div className="day-number">{day}</div>
                {dayEvents.length > 0 && <div className="event-dot" />}
              </div>
            );
          })}
        </div>
      </div>

      {selectedDate && (
        <div className="event-list">
          <h2>{selectedDate.toLocaleDateString('zh-CN', { weekday: 'long', month: 'long', day: 'numeric' })}</h2>
          {eventsOnSelectedDate.length > 0 ? (
            <ul>
              {eventsOnSelectedDate.map(event => (
                <li
                  key={event.id}
                  className="event-item"
                  onClick={() => {
                    const date = selectedDate?.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
                    const time = new Date(event.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
                    const msg = `帮我查看${date}的日程「${event.title}」，时间是${time}`;
                    (window as any).Android?.navigateToChat(msg);
                  }}
                  style={{ cursor: 'pointer' }}
                >
                  <div className="event-title">{event.title}</div>
                  <div className="event-time">
                    {new Date(event.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
                    {event.endTime && (
                      <>
                        {' - '}
                        {new Date(event.endTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
                      </>
                    )}
                  </div>
                  {event.location && <div className="event-location">📍 {event.location}</div>}
                  {event.description && <div className="event-description">{event.description}</div>}
                </li>
              ))}
            </ul>
          ) : (
            <p className="no-events">暂无日程</p>
          )}
        </div>
      )}
    </div>
  );
}
