import React from 'react';
import { useAgentFloatBalance } from '../../hooks/useAgent';
import { ProgressBar } from '../../components/ui/ProgressBar';
import { StatCard } from '../../components/data/StatCard';
import { Wallet, Banknote, ArrowUpRight, ArrowDownLeft } from 'lucide-react';

export default function DashboardPage(): JSX.Element {
  const { data } = useAgentFloatBalance();
  const balanceKc = data?.balanceKc ?? 0;
  const limitKc = 100000;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Agent Dashboard</h1>
      <div className="bg-surface-card border border-surface-border rounded-2xl p-6 shadow-md">
        <div className="text-xs uppercase tracking-wider text-text-secondary">Float Balance</div>
        <div className="text-3xl font-bold text-text-primary mt-2">{balanceKc}</div>
        <div className="text-sm text-text-secondary">Limit: {limitKc}</div>
        <div className="mt-3">
          <ProgressBar value={balanceKc} max={limitKc} />
        </div>
      </div>
      <div className="grid md:grid-cols-4 gap-4">
        <StatCard title="Top Ups" value={8} icon={<ArrowUpRight className="w-5 h-5" />} variant="success" />
        <StatCard title="Top Up Volume" value={4000} icon={<Wallet className="w-5 h-5" />} variant="primary" />
        <StatCard title="Withdrawals" value={2} icon={<ArrowDownLeft className="w-5 h-5" />} variant="warning" />
        <StatCard title="Cash on Hand" value={35} icon={<Banknote className="w-5 h-5" />} variant="default" />
      </div>
    </div>
  );
}
