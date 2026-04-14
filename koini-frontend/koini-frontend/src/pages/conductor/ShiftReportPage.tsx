import React, { useMemo } from 'react';
import { useConductorShiftReport } from '../../hooks/useConductor';
import { useAuth } from '../../hooks/useAuth';
import { EmptyState } from '../../components/ui/EmptyState';
import { Banknote, ReceiptText, ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { StatCard } from '../../components/data/StatCard';
import { useTransactionHistory } from '../../hooks/useWallet';

export default function ShiftReportPage(): JSX.Element {
  const { state } = useAuth();
  const navigate = useNavigate();
  const isPending = state.user?.status === 'PENDING_VERIFICATION';
  const { data, isLoading: shiftLoading } = useConductorShiftReport({ enabled: !isPending });

  const today = useMemo(() => new Date().toISOString().slice(0, 10), []);
  const { data: faresToday, isLoading: faresLoading } = useTransactionHistory({
    dateFrom: today,
    dateTo: today,
    type: 'FARE_PAYMENT',
    page: 0,
    size: 1,
  });
  const faresCount = faresToday?.totalElements ?? 0;

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
      <div className="grid md:grid-cols-2 gap-4">
        <StatCard
          title="Fares Collected"
          value={faresCount}
          icon={<ReceiptText className="w-5 h-5" />}
          variant="success"
          isLoading={faresLoading}
        />
        <StatCard
          title="Earnings"
          value={data?.totalEarningsUsd ?? '$0.00'}
          subtitle={data?.totalEarningsKc != null ? `${data.totalEarningsKc.toLocaleString()} KC` : undefined}
          icon={<Banknote className="w-5 h-5" />}
          variant="primary"
          isLoading={shiftLoading}
        />
      </div>
    </div>
  );
}
