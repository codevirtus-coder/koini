import React, { useState } from 'react';
import { useAdminTransactions } from '../../../hooks/useTransactions';
import { FilterBar } from '../../../components/data/FilterBar';
import { Input } from '../../../components/ui/Input';
import { Select } from '../../../components/ui/Select';
import { DataTable } from '../../../components/data/DataTable';
import type { TransactionStatus, TransactionType } from '../../../api/types';

export default function TransactionsPage(): JSX.Element {
  const [type, setType] = useState<TransactionType | ''>('');
  const [status, setStatus] = useState<TransactionStatus | ''>('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const { data, isLoading } = useAdminTransactions({
    page: 0,
    size: 20,
    type: type || undefined,
    status: status || undefined,
    dateFrom: dateFrom || undefined,
    dateTo: dateTo || undefined,
  });
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">System Transactions</h1>
      <FilterBar>
        <Input label="From" fullWidth={false} type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
        <Input label="To" fullWidth={false} type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
        <Select
          label="Type"
          fullWidth={false}
          value={type}
          onChange={(e) => setType(e.target.value as TransactionType | '')}
          options={[{ label: 'All', value: '' }, { label: 'Fare', value: 'FARE_PAYMENT' }, { label: 'Top Up', value: 'TOPUP' }]}
        />
        <Select
          label="Status"
          fullWidth={false}
          value={status}
          onChange={(e) => setStatus(e.target.value as TransactionStatus | '')}
          options={[{ label: 'All', value: '' }, { label: 'Completed', value: 'COMPLETED' }, { label: 'Pending', value: 'PENDING' }]}
        />
      </FilterBar>
      <DataTable
        data={data?.content ?? []}
        isLoading={isLoading}
        rowKey="txId"
        columns={[
          { key: 'reference', header: 'Ref' },
          { key: 'txType', header: 'Type' },
          { key: 'amountKc', header: 'Amount' },
          { key: 'feeKc', header: 'Fee' },
          { key: 'status', header: 'Status' },
          { key: 'createdAt', header: 'Date' },
        ]}
      />
    </div>
  );
}
