import React from 'react';
import { cn } from '../../design/cn';

interface SelectOption {
  label: string;
  value: string;
}

interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  options: SelectOption[];
  error?: string;
  fullWidth?: boolean;
  containerClassName?: string;
}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { label, options, error, fullWidth = true, containerClassName, className, ...props },
  ref
): JSX.Element {
  return (
    <div className={cn(fullWidth ? 'w-full' : 'min-w-[220px]', containerClassName)}>
      {label && <label className="block text-xs font-medium text-text-secondary uppercase tracking-wider mb-2">{label}</label>}
      <select
        ref={ref}
        className={cn(
          'bg-surface-bg border border-surface-borderMd rounded-lg px-4 py-3 text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-primary-500',
          fullWidth ? 'w-full' : 'w-auto min-w-[220px]',
          error && 'border-danger-500 focus:ring-danger-500',
          className
        )}
        {...props}
      >
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      {error && <p className="text-xs text-danger-500 mt-1">{error}</p>}
    </div>
  );
});
