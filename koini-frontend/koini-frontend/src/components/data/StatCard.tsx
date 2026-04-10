import React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../design/cn';
import { useCountUp } from '../../hooks/useCountUp';
import { cardVariants } from '../../design/animations';
import { StatCardSkeleton } from '../ui/Skeleton';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  trend?: {
    value: number;
    direction: 'up' | 'down';
    label?: string;
  };
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger';
  isLoading?: boolean;
  onClick?: () => void;
}

const variantStyles: Record<NonNullable<StatCardProps['variant']>, string> = {
  default: 'bg-surface-card',
  primary: 'bg-surface-card',
  success: 'bg-surface-card',
  warning: 'bg-surface-card',
  danger: 'bg-surface-card',
};

const iconStyles: Record<NonNullable<StatCardProps['variant']>, string> = {
  default: 'bg-surface-borderMd text-text-secondary',
  primary: 'bg-primary-500/10 text-primary-400',
  success: 'bg-success-500/10 text-success-500',
  warning: 'bg-warning-500/10 text-warning-500',
  danger: 'bg-danger-500/10 text-danger-500',
};

export function StatCard({ title, value, subtitle, icon, trend, variant = 'default', isLoading, onClick }: StatCardProps): JSX.Element {
  if (isLoading) return <StatCardSkeleton />;

  const numeric = typeof value === 'number';
  const animated = numeric ? useCountUp(value) : value;

  return (
    <motion.div
      variants={cardVariants}
      initial="initial"
      animate="animate"
      whileHover={{ scale: 1.01 }}
      className={cn('rounded-xl p-5 border border-surface-border', variantStyles[variant], onClick && 'cursor-pointer')}
      onClick={onClick}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs uppercase tracking-wider text-text-muted">{title}</p>
          <p className="text-2xl font-bold text-text-primary mt-2">{animated}</p>
          {subtitle && <p className="text-xs text-text-secondary mt-1">{subtitle}</p>}
        </div>
        <div className={cn('w-10 h-10 rounded-lg flex items-center justify-center', iconStyles[variant])}>{icon}</div>
      </div>
      {trend && (
        <div className="mt-4 text-xs text-text-secondary">
          <span className={cn(trend.direction === 'up' ? 'text-success-500' : 'text-danger-500')}>
            {trend.direction === 'up' ? '+' : '-'}{Math.abs(trend.value)}%
          </span>
          {trend.label && <span className="ml-2">{trend.label}</span>}
        </div>
      )}
    </motion.div>
  );
}
