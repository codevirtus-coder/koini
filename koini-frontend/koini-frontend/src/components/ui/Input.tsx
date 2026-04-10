import React from 'react';
import { cn } from '../../design/cn';
import { tw } from '../../design/tokens';
import { Spinner } from './Spinner';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
  leftIcon?: React.ReactNode;
  rightElement?: React.ReactNode;
  isLoading?: boolean;
  fullWidth?: boolean;
  containerClassName?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(function Input(
  {
    label,
    error,
    hint,
    leftIcon,
    rightElement,
    isLoading,
    fullWidth = true,
    containerClassName,
    className,
    id,
    ...props
  },
  ref
): JSX.Element {
  const inputId = id || props.name;
  return (
    <div className={cn(fullWidth ? 'w-full' : 'min-w-[220px]', containerClassName)}>
      {label && (
        <label htmlFor={inputId} className={tw.label}>
          {label}
        </label>
      )}
      <div className="relative">
        {leftIcon && (
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted">
            {leftIcon}
          </span>
        )}
        <input
          id={inputId}
          ref={ref}
          className={cn(
            tw.input,
            !fullWidth && 'w-auto min-w-[220px]',
            leftIcon && 'pl-10',
            rightElement || isLoading ? 'pr-10' : undefined,
            error && tw.inputError,
            className
          )}
          {...props}
        />
        {isLoading && (
          <span className="absolute right-3 top-1/2 -translate-y-1/2">
            <Spinner size="sm" />
          </span>
        )}
        {!isLoading && rightElement && (
          <span className="absolute right-3 top-1/2 -translate-y-1/2">
            {rightElement}
          </span>
        )}
      </div>
      {error && <p className={cn(tw.errorMsg, 'animate-slide-down')}>{error}</p>}
      {hint && !error && <p className="text-xs text-text-muted mt-1">{hint}</p>}
    </div>
  );
});
