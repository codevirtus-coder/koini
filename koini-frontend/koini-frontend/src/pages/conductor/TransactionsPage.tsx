import React from 'react';
import { useTransactionHistory } from '../../hooks/useWallet';
import { TransactionList } from '../../components/data/TransactionList';

export default function TransactionsPage(): JSX.Element {
  const { data, isLoading } = useTransactionHistory({ page: 0, size: 20 });
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Transactions</h1>
      <TransactionList transactions={data?.content} isLoading={isLoading} />
    </div>
  );
}
