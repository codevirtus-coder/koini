import React from 'react';
import { ArrowDownLeft, ArrowUpRight, Bus, ArrowLeftRight, Banknote, Receipt } from 'lucide-react';
import type { Transaction } from '../../api/types';
import { formatRelative } from '../../utils/date';
import { formatUsd } from '../../utils/money';
import { StatusBadge } from './StatusBadge';
import { cn } from '../../design/cn';

interface TransactionRowProps {
  tx: Transaction;
  onClick?: () => void;
}

function typeIcon(type: string): React.ReactNode {
  switch (type) {
    case 'FARE_PAYMENT':
      return <Bus className="w-4 h-4" />;
    case 'TRANSFER':
      return <ArrowLeftRight className="w-4 h-4" />;
    case 'WITHDRAWAL':
      return <Banknote className="w-4 h-4" />;
    default:
      return <Receipt className="w-4 h-4" />;
  }
}

export function TransactionRow({ tx, onClick }: TransactionRowProps): JSX.Element {
  const DirectionIcon = tx.direction === 'debit' ? ArrowUpRight : ArrowDownLeft;
  return (
    <div
      onClick={onClick}
      className={cn('flex items-center justify-between py-3 border-b border-surface-border', onClick && 'cursor-pointer hover:bg-surface-cardHover')}
    >
      <div className="flex items-center gap-3">
        <div className={cn('w-10 h-10 rounded-full flex items-center justify-center', tx.direction === 'debit' ? 'bg-danger-500/10 text-danger-400' : 'bg-success-500/10 text-success-400')}>
          <DirectionIcon className="w-4 h-4" />
        </div>
        <div>
          <div className="text-sm text-text-primary flex items-center gap-2">
            {typeIcon(tx.txType)}
            <span>{tx.txType.replace('_', ' ')}</span>
          </div>
          <div className="text-xs text-text-secondary">{formatRelative(tx.createdAt)}</div>
        </div>
      </div>
      <div className="text-right">
        <div className={cn('font-mono font-semibold', tx.direction === 'debit' ? 'text-debit' : 'text-credit')}>
          {tx.direction === 'debit' ? '-' : '+'}
          {formatUsd(tx.amountKc)}
        </div>
        <div className="mt-1">
          <StatusBadge status={tx.status} />
        </div>
      </div>
    </div>
  );
}
