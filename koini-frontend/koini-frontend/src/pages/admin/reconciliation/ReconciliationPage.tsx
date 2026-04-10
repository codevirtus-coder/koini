import React, { useMemo, useState } from 'react';
import { useReconciliation } from '../../../hooks/useAdmin';
import { Input } from '../../../components/ui/Input';
import { DataTable } from '../../../components/data/DataTable';

export default function ReconciliationPage(): JSX.Element {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const { data } = useReconciliation(date);
  const rows = useMemo(() => {
    if (!data) return [];
    return [
      { label: 'Date', value: data.date },
      { label: 'Status', value: data.status },
      { label: 'Top Ups', value: `${data.totalTopUpsKc}` },
      { label: 'Fares', value: `${data.totalFaresKc}` },
      { label: 'Withdrawals', value: `${data.totalWithdrawalsKc}` },
      { label: 'Net Balance', value: `${data.netBalanceKc}` },
      { label: 'Discrepancy', value: data.discrepancy },
    ];
  }, [data]);

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Reconciliation</h1>
      <Input label="Date" fullWidth={false} type="date" value={date} onChange={(e) => setDate(e.target.value)} />
      <div className="bg-surface-card border border-surface-border rounded-xl p-4">
        <div className="text-sm text-text-secondary mb-2">Reconciliation Summary</div>
        <DataTable
          data={rows}
          rowKey="label"
          columns={[
            { key: 'label', header: 'Metric' },
            { key: 'value', header: 'Value', align: 'right' },
          ]}
          emptyMessage="No reconciliation data"
        />
      </div>
    </div>
  );
}
