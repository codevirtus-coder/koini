import { useEffect, useState } from 'react';

export function useCountdown(expiresAt: string | null) {
  const [secondsLeft, setSecondsLeft] = useState<number>(0);

  useEffect(() => {
    if (!expiresAt) return;

    const tick = () => {
      const diff = Math.max(0, Math.floor((new Date(expiresAt).getTime() - Date.now()) / 1000));
      setSecondsLeft(diff);
    };

    tick();
    const id = setInterval(tick, 500);
    return () => clearInterval(id);
  }, [expiresAt]);

  const percentage = expiresAt ? (secondsLeft / 90) * 100 : 0;
  const isExpired = secondsLeft === 0;
  const isWarning = secondsLeft <= 20;
  const isUrgent = secondsLeft <= 10;

  return { secondsLeft, percentage, isExpired, isWarning, isUrgent };
}
