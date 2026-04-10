import React from 'react';
import { cn } from '../../design/cn';
import { tw } from '../../design/tokens';

type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'info';

interface BadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  className?: string;
}

const variantStyles: Record<BadgeVariant, string> = {
  default: 'bg-surface-cardHover text-text-secondary border border-surface-border',
  success: 'bg-[#E6F7F0] text-[#1F7A55] border border-[#BFE9D6]',
  warning: 'bg-[#FFF4D6] text-[#946200] border border-[#F3DBA6]',
  danger: 'bg-[#FDE7E7] text-[#A61B1B] border border-[#F6B9B9]',
  info: 'bg-[#E9F1FF] text-[#2457B2] border border-[#C9DCF9]',
};

export function Badge({ children, variant = 'default', className }: BadgeProps): JSX.Element {
  return <span className={cn(tw.badge, variantStyles[variant], className)}>{children}</span>;
}
