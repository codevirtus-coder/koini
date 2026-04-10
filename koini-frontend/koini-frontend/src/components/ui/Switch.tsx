import React from 'react';
import { cn } from '../../design/cn';

interface SwitchProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  checked: boolean;
}

export function Switch({ checked, className, ...props }: SwitchProps): JSX.Element {
  return (
    <button
      type="button"
      aria-pressed={checked}
      className={cn(
        'w-10 h-6 rounded-full transition-colors flex items-center px-1',
        checked ? 'bg-primary-500' : 'bg-surface-borderMd',
        className
      )}
      {...props}
    >
      <span
        className={cn(
          'w-4 h-4 rounded-full bg-white transition-transform',
          checked ? 'translate-x-4' : 'translate-x-0'
        )}
      />
    </button>
  );
}
