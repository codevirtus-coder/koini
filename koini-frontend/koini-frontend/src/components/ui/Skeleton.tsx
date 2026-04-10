import React from 'react';
import { cn } from '../../design/cn';

interface SkeletonProps {
  className?: string;
  lines?: number;
  circle?: boolean;
}

export function Skeleton({ className, lines, circle }: SkeletonProps): JSX.Element {
  if (lines && lines > 1) {
    return (
      <div className="space-y-2">
        {Array.from({ length: lines }).map((_, i) => (
          <div key={`line-${i}`} className={cn('h-3 w-full bg-surface-cardHover animate-pulse rounded', className)} />
        ))}
      </div>
    );
  }

  return (
    <div
      className={cn(
        'bg-surface-cardHover animate-pulse rounded',
        circle ? 'rounded-full' : 'rounded',
        className
      )}
    />
  );
}

export function WalletBalanceSkeleton(): JSX.Element {
  return (
    <div className="bg-surface-card rounded-2xl p-6 space-y-3 animate-pulse">
      <div className="h-4 w-24 bg-surface-cardHover rounded" />
      <div className="h-10 w-40 bg-surface-cardHover rounded" />
      <div className="h-4 w-28 bg-surface-cardHover rounded" />
    </div>
  );
}

export function TransactionRowSkeleton(): JSX.Element {
  return (
    <div className="flex items-center justify-between py-3 animate-pulse">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-full bg-surface-cardHover" />
        <div className="space-y-2">
          <div className="h-3 w-28 bg-surface-cardHover rounded" />
          <div className="h-3 w-20 bg-surface-cardHover rounded" />
        </div>
      </div>
      <div className="h-3 w-16 bg-surface-cardHover rounded" />
    </div>
  );
}

export function StatCardSkeleton(): JSX.Element {
  return (
    <div className="bg-surface-card rounded-xl p-5 animate-pulse space-y-3">
      <div className="h-4 w-24 bg-surface-cardHover rounded" />
      <div className="h-8 w-20 bg-surface-cardHover rounded" />
      <div className="h-3 w-16 bg-surface-cardHover rounded" />
    </div>
  );
}

export function TableSkeleton({ rows = 5 }: { rows?: number }): JSX.Element {
  return (
    <div className="space-y-3 animate-pulse">
      <div className="h-4 w-full bg-surface-cardHover rounded" />
      {Array.from({ length: rows }).map((_, i) => (
        <div key={`row-${i}`} className="h-4 w-full bg-surface-cardHover rounded" />
      ))}
    </div>
  );
}
