import React from 'react';
import { motion, type HTMLMotionProps } from 'framer-motion';
import { cn } from '../../design/cn';
import { Spinner } from './Spinner';

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'success';
type ButtonSize = 'sm' | 'md' | 'lg' | 'xl';

interface ButtonProps extends Omit<HTMLMotionProps<'button'>, 'ref' | 'children'> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  fullWidth?: boolean;
  children?: React.ReactNode;
}

const variantStyles: Record<ButtonVariant, string> = {
  primary: 'bg-primary-600 hover:bg-primary-500 text-white shadow-sm hover:shadow-glow',
  secondary: 'bg-surface-card hover:bg-surface-cardHover text-text-primary border border-surface-border',
  outline: 'border border-surface-borderMd hover:border-primary-500 text-text-primary',
  ghost: 'bg-transparent hover:bg-surface-card text-text-secondary',
  danger: 'bg-danger-500 hover:bg-danger-600 text-white',
  success: 'bg-success-500 hover:bg-success-700 text-white',
};

const sizeStyles: Record<ButtonSize, string> = {
  sm: 'px-3 py-1.5 text-xs rounded-md',
  md: 'px-4 py-2.5 text-sm rounded-lg',
  lg: 'px-6 py-3 text-base rounded-lg',
  xl: 'px-8 py-4 text-lg rounded-xl',
};

export function Button({
  variant = 'primary',
  size = 'md',
  isLoading,
  leftIcon,
  rightIcon,
  fullWidth,
  className,
  disabled,
  children,
  ...props
}: ButtonProps): JSX.Element {
  const isDisabled = disabled || isLoading;
  return (
    <motion.button
      whileTap={{ scale: 0.97 }}
      type="button"
      aria-busy={isLoading}
      disabled={isDisabled}
      className={cn(
        'inline-flex items-center justify-center gap-2 font-semibold transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500/40',
        variantStyles[variant],
        sizeStyles[size],
        fullWidth && 'w-full',
        isDisabled && 'opacity-60 cursor-not-allowed',
        className
      )}
      {...props}
    >
      {isLoading ? (
        <Spinner size="sm" className="border-white border-t-transparent" />
      ) : (
        <>
          {leftIcon && <span className="inline-flex">{leftIcon}</span>}
          <span>{children}</span>
          {rightIcon && <span className="inline-flex">{rightIcon}</span>}
        </>
      )}
    </motion.button>
  );
}
