import React from 'react';
import { useConductorShiftReport } from '../../hooks/useConductor';
import { DataTable } from '../../components/data/DataTable';
import { useAuth } from '../../hooks/useAuth';
import { EmptyState } from '../../components/ui/EmptyState';
import { ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function ShiftReportPage(): JSX.Element {
  const { state } = useAuth();
  const navigate = useNavigate();
  const isPending = state.user?.status === 'PENDING_VERIFICATION';
  const { data } = useConductorShiftReport({ enabled: !isPending });
  const rows = data?.recentFares ?? [];

  if (isPending) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-text-primary">Shift Report</h1>
        <EmptyState
          icon={<ShieldCheck className="w-7 h-7" />}
          title="Locked until approval"
          description="Shift activity becomes available once an admin approves your merchant account."
          actionLabel="Submit Documents"
          onAction={() => navigate('/merchant/onboarding')}
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Shift Report</h1>
      <DataTable
        data={rows}
        rowKey="reference"
        columns={[
          { key: 'time', header: 'Time' },
          { key: 'passenger', header: 'Client' },
          { key: 'amount', header: 'Amount' },
        ]}
        emptyMessage="No fares yet"
      />
    </div>
  );
}
