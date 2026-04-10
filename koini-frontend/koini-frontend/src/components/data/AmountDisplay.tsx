import React from 'react';
import { cn } from '../../design/cn';
import { formatUsd, formatKc } from '../../utils/money';
import { useCountUp } from '../../hooks/useCountUp';

type AmountSize = 'sm' | 'md' | 'lg' | 'xl' | '2xl';

interface AmountDisplayProps {
  amountKc: number;
  showKc?: boolean;
  showUsd?: boolean;
  size?: AmountSize;
  direction?: 'credit' | 'debit' | 'neutral';
  animated?: boolean;
}

const sizeMap: Record<AmountSize, string> = {
  sm: 'text-sm',
  md: 'text-base',
  lg: 'text-lg',
  xl: 'text-2xl',
  '2xl': 'text-4xl',
};

export function AmountDisplay({
  amountKc,
  showKc = true,
  showUsd = true,
  size = 'md',
  direction = 'neutral',
  animated,
}: AmountDisplayProps): JSX.Element {
  const animatedValue = useCountUp(amountKc);
  const value = animated ? animatedValue : amountKc;
  const sign = direction === 'credit' ? '+' : direction === 'debit' ? '-' : '';
  const color =
    direction === 'credit' ? 'text-credit' : direction === 'debit' ? 'text-debit' : 'text-text-primary';

  return (
    <div className="flex flex-col">
      <div className={cn('font-mono font-bold tabular-nums', sizeMap[size], color)}>
        {sign}
        {formatUsd(value)}
      </div>
      {showKc && <div className="text-xs text-text-secondary">{formatKc(value)}</div>}
      {!showKc && showUsd && <div className="text-xs text-text-secondary">{formatUsd(value)}</div>}
    </div>
  );
}
