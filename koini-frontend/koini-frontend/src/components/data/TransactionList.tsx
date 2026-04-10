import React from 'react';
import type { Transaction } from '../../api/types';
import { TransactionRow } from './TransactionRow';
import { EmptyState } from '../ui/EmptyState';
import { TransactionRowSkeleton } from '../ui/Skeleton';
import { Wallet } from 'lucide-react';

interface TransactionListProps {
  transactions: Transaction[] | undefined;
  isLoading?: boolean;
  onRowClick?: (tx: Transaction) => void;
}

export function TransactionList({ transactions, isLoading, onRowClick }: TransactionListProps): JSX.Element {
  if (isLoading) {
    return (
      <div>
        <TransactionRowSkeleton />
        <TransactionRowSkeleton />
        <TransactionRowSkeleton />
      </div>
    );
  }

  if (!transactions || transactions.length === 0) {
    return <EmptyState title="No transactions yet" description="Your activity will show here." icon={<Wallet />} />;
  }

  return (
    <div>
      {transactions.map((tx) => (
        <TransactionRow key={tx.txId} tx={tx} onClick={onRowClick ? () => onRowClick(tx) : undefined} />
      ))}
    </div>
  );
}
