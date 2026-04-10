import React from 'react';
import { cn } from '../../design/cn';

interface ProgressBarProps {
  value: number;
  max?: number;
  className?: string;
}

export function ProgressBar({ value, max = 100, className }: ProgressBarProps): JSX.Element {
  const percentage = Math.min(100, Math.max(0, (value / max) * 100));
  return (
    <div className={cn('w-full h-2 bg-surface-borderMd rounded-full overflow-hidden', className)}>
      <div className="h-full bg-accent-500" style={{ width: `${percentage}%` }} />
    </div>
  );
}
