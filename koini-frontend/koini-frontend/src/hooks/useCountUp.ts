import { useEffect, useState } from 'react';

export function useCountUp(target: number, duration = 800): number {
  const [current, setCurrent] = useState(0);

  useEffect(() => {
    const startTime = performance.now();
    const startValue = 0;

    const step = (timestamp: number) => {
      const progress = Math.min((timestamp - startTime) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setCurrent(Math.round(startValue + (target - startValue) * eased));
      if (progress < 1) requestAnimationFrame(step);
    };

    requestAnimationFrame(step);
  }, [target, duration]);

  return current;
}
