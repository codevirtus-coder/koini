import React from 'react';
import { cn } from '../../design/cn';

type AlertVariant = 'info' | 'success' | 'warning' | 'danger';

interface AlertProps {
  title?: string;
  description?: string;
  variant?: AlertVariant;
  children?: React.ReactNode;
}

const styles: Record<AlertVariant, string> = {
  info: 'border-primary-500/30 bg-primary-500/10 text-primary-200',
  success: 'border-success-500/30 bg-success-500/10 text-success-200',
  warning: 'border-warning-500/30 bg-warning-500/10 text-warning-200',
  danger: 'border-danger-500/30 bg-danger-500/10 text-danger-200',
};

export function Alert({ title, description, variant = 'info', children }: AlertProps): JSX.Element {
  return (
    <div className={cn('border rounded-lg p-4', styles[variant])} role="alert">
      {title && <div className="font-semibold text-sm">{title}</div>}
      {description && <div className="text-sm text-text-secondary mt-1">{description}</div>}
      {children && <div className="text-sm mt-2">{children}</div>}
    </div>
  );
}
