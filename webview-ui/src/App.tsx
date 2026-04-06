import { useState, useEffect } from 'react';
import CalendarView from './views/CalendarView';
import ExpenseView from './views/ExpenseView';

type ViewType = 'calendar' | 'expense';

function getInitialView(): ViewType {
  const hash = window.location.hash;
  if (hash.includes('expense')) return 'expense';
  return 'calendar';
}

export default function App() {
  const [view, setView] = useState<ViewType>(getInitialView);

  useEffect(() => {
    const handleHashChange = () => {
      setView(getInitialView());
    };
    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  return (
    <div className="app">
      {view === 'expense' ? <ExpenseView /> : <CalendarView />}
    </div>
  );
}
