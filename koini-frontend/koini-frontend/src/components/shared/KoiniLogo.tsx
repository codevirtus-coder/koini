import React from 'react';
import { cn } from '../../design/cn';

const sizeMap: Record<number, string> = {
  24: 'w-6 h-6 text-sm',
  28: 'w-7 h-7 text-sm',
  32: 'w-8 h-8 text-base',
  40: 'w-10 h-10 text-lg',
  48: 'w-12 h-12 text-xl',
};

export function KoiniLogo({ size = 32 }: { size?: number }): JSX.Element {
  const sizeClass = sizeMap[size] ?? 'w-8 h-8 text-base';
  return (
    <div className="inline-flex items-center gap-2" aria-label="KOINI">
      <div className={cn('rounded-xl bg-primary-600 text-white font-bold flex items-center justify-center', sizeClass)}>
        K
      </div>
      <span className="text-lg font-semibold text-text-primary">KOINI</span>
    </div>
  );
}
