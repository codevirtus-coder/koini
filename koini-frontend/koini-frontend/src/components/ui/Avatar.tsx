import React from 'react';
import { cn } from '../../design/cn';

interface AvatarProps {
  name?: string | null;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeStyles: Record<NonNullable<AvatarProps['size']>, string> = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-14 h-14 text-lg',
};

export function Avatar({ name, size = 'md', className }: AvatarProps): JSX.Element {
  const initials = (name || 'User')
    .split(' ')
    .map((n) => n[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  return (
    <div
      className={cn(
        'rounded-full bg-primary-500/20 text-primary-300 flex items-center justify-center font-semibold',
        sizeStyles[size],
        className
      )}
      aria-label="User avatar"
    >
      {initials}
    </div>
  );
}
