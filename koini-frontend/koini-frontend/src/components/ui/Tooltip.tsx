import React, { useState } from 'react';
import { cn } from '../../design/cn';

interface TooltipProps {
  content: string;
  children: React.ReactNode;
  className?: string;
}

export function Tooltip({ content, children, className }: TooltipProps): JSX.Element {
  const [open, setOpen] = useState(false);
  return (
    <span
      className={cn('relative inline-flex', className)}
      onMouseEnter={() => setOpen(true)}
      onMouseLeave={() => setOpen(false)}
      onFocus={() => setOpen(true)}
      onBlur={() => setOpen(false)}
    >
      {children}
      {open && (
        <span className="absolute z-50 -top-8 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-md bg-surface-card border border-surface-border px-2 py-1 text-xs text-text-secondary shadow-md">
          {content}
        </span>
      )}
    </span>
  );
}
