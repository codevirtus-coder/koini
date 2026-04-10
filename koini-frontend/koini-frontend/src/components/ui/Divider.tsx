import React from 'react';
import { cn } from '../../design/cn';

export function Divider({ className }: { className?: string }): JSX.Element {
  return <div className={cn('h-px w-full bg-surface-border', className)} />;
}
