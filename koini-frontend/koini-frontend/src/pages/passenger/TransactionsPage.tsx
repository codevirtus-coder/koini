import React, { useState } from 'react';
import { useTransactionHistory } from '../../hooks/useWallet';
import { FilterBar } from '../../components/data/FilterBar';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';
import { TransactionList } from '../../components/data/TransactionList';
import type { TransactionStatus, TransactionType } from '../../api/types';

export default function TransactionsPage(): JSX.Element {
  const [type, setType] = useState<TransactionType | ''>('');
  const [status, setStatus] = useState<TransactionStatus | ''>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const { data, isLoading } = useTransactionHistory({
    page: 0,
    size: 20,
    type: type || undefined,
    status: status || undefined,
    dateFrom: dateFrom || undefined,
    dateTo: dateTo || undefined,
  });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Transactions</h1>
      <FilterBar>
        <Input label="From" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
        <Input label="To" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
        <Select
          label="Type"
          value={type}
          onChange={(e) => setType(e.target.value as TransactionType | '')}
          options={[
            { label: 'All', value: '' },
            { label: 'Fare', value: 'FARE_PAYMENT' },
            { label: 'Top Up', value: 'TOPUP' },
            { label: 'Transfer', value: 'TRANSFER' },
            { label: 'Withdrawal', value: 'WITHDRAWAL' },
          ]}
        />
        <Select
          label="Status"
          value={status}
          onChange={(e) => setStatus(e.target.value as TransactionStatus | '')}
          options={[
            { label: 'All', value: '' },
            { label: 'Completed', value: 'COMPLETED' },
            { label: 'Pending', value: 'PENDING' },
            { label: 'Failed', value: 'FAILED' },
          ]}
        />
      </FilterBar>
      <TransactionList transactions={data?.content} isLoading={isLoading} />
    </div>
  );
}
