import React from 'react';
import { cn } from '../../design/cn';

interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeMap: Record<NonNullable<SpinnerProps['size']>, string> = {
  sm: 'w-4 h-4 border-2',
  md: 'w-6 h-6 border-2',
  lg: 'w-8 h-8 border-2',
};

export function Spinner({ size = 'md', className }: SpinnerProps): JSX.Element {
  return (
    <div
      aria-label="Loading"
      className={cn(
        'inline-block rounded-full border-surface-border border-t-primary-500 animate-spin',
        sizeMap[size],
        className
      )}
    />
  );
}
