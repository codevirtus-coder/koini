import React from 'react';
import { cn } from '../../design/cn';

interface CheckboxProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
}

export function Checkbox({ label, className, ...props }: CheckboxProps): JSX.Element {
  return (
    <label className="inline-flex items-center gap-2 text-sm text-text-secondary">
      <input
        type="checkbox"
        className={cn('h-4 w-4 rounded border-surface-borderMd bg-surface-bg text-primary-500 focus:ring-primary-500', className)}
        {...props}
      />
      {label && <span>{label}</span>}
    </label>
  );
}
