import React from 'react';
import { useConductorShiftReport } from '../../hooks/useConductor';
import { StatCard } from '../../components/data/StatCard';
import { Bus, Banknote, Clock, ShieldCheck } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { EmptyState } from '../../components/ui/EmptyState';
import { useNavigate } from 'react-router-dom';

export default function DashboardPage(): JSX.Element {
  const { state } = useAuth();
  const navigate = useNavigate();
  const isPending = state.user?.status === 'PENDING_VERIFICATION';
  const { data } = useConductorShiftReport({ enabled: !isPending });

  if (isPending) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-text-primary">Merchant Dashboard</h1>
        <EmptyState
          icon={<ShieldCheck className="w-7 h-7" />}
          title="Verification required"
          description="Submit your verification documents to unlock redeem and payment requests. An admin must approve your account."
          actionLabel="Submit Documents"
          onAction={() => navigate('/merchant/onboarding')}
        />
      </div>
    );
  }
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Shift Overview</h1>
      <div className="grid md:grid-cols-3 gap-4">
        <StatCard title="Fares Collected" value={data?.fares ?? 0} icon={<Bus className="w-5 h-5" />} variant="success" />
        <StatCard title="Revenue" value={data?.revenueKc ?? 0} icon={<Banknote className="w-5 h-5" />} variant="primary" />
        <StatCard title="Active Since" value={data?.activeSince ?? '07:30 AM'} icon={<Clock className="w-5 h-5" />} variant="default" />
      </div>
    </div>
  );
}
