import React from 'react';
import { cn } from '../../design/cn';

interface FilterBarProps {
  children: React.ReactNode;
  className?: string;
}

export function FilterBar({ children, className }: FilterBarProps): JSX.Element {
  return (
    <div className={cn('flex flex-wrap gap-3 items-end bg-surface-card border border-surface-border rounded-xl p-4', className)}>
      {children}
    </div>
  );
}
