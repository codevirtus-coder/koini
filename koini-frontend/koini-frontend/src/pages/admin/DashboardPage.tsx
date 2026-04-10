import React from 'react';
import { useAdminDashboard } from '../../hooks/useAdmin';
import { StatCard } from '../../components/data/StatCard';
import { Users, Wallet, Activity, AlertTriangle } from 'lucide-react';
import { RevenueChart } from '../../components/charts/RevenueChart';
import { RoleDonutChart } from '../../components/charts/RoleDonutChart';
import { DataTable } from '../../components/data/DataTable';
import type { Transaction } from '../../api/types';

export default function DashboardPage(): JSX.Element {
  const { data } = useAdminDashboard();

  const revenueData = [
    { day: 'Mon', fares: 1200, topups: 800 },
    { day: 'Tue', fares: 1400, topups: 900 },
    { day: 'Wed', fares: 1100, topups: 700 },
    { day: 'Thu', fares: 1600, topups: 950 },
    { day: 'Fri', fares: 2000, topups: 1100 },
    { day: 'Sat', fares: 1800, topups: 1000 },
    { day: 'Sun', fares: 1300, topups: 850 },
  ];

  const roleData = [
    { role: 'Clients', count: data?.totalUsers?.passengers ?? 0 },
    { role: 'Merchant', count: data?.totalUsers?.conductors ?? 0 },
    { role: 'Agent', count: data?.totalUsers?.agents ?? 0 },
  ];

  const recent: Transaction[] = [];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Admin Dashboard</h1>
      <div className="grid md:grid-cols-4 gap-4">
        <StatCard title="Clients" value={data?.totalUsers?.passengers ?? 0} icon={<Users className="w-5 h-5" />} variant="primary" />
        <StatCard title="Wallet Balance" value={data?.totalWalletBalanceKc ?? 0} icon={<Wallet className="w-5 h-5" />} variant="success" />
        <StatCard title="Transactions Today" value={data?.transactionsToday ?? 0} icon={<Activity className="w-5 h-5" />} variant="default" />
        <StatCard title="Flagged Accounts" value={data?.flaggedAccounts ?? 0} icon={<AlertTriangle className="w-5 h-5" />} variant="danger" />
      </div>
      <div className="grid md:grid-cols-5 gap-4">
        <div className="md:col-span-3 bg-surface-card border border-surface-border rounded-xl p-4">
          <div className="text-sm text-text-secondary mb-2">Revenue (last 7 days)</div>
          <RevenueChart data={revenueData} />
        </div>
        <div className="md:col-span-2 bg-surface-card border border-surface-border rounded-xl p-4">
          <div className="text-sm text-text-secondary mb-2">Users by role</div>
          <RoleDonutChart data={roleData} />
        </div>
      </div>
      <div className="bg-surface-card border border-surface-border rounded-xl p-4">
        <div className="text-sm text-text-secondary mb-2">Recent Activity</div>
        <DataTable
          data={recent}
          rowKey="txId"
          columns={[
            { key: 'createdAt', header: 'Time' },
            { key: 'txType', header: 'Type' },
            { key: 'reference', header: 'User' },
            { key: 'amountKc', header: 'Amount' },
            { key: 'status', header: 'Status' },
          ]}
          emptyMessage="No transactions yet"
        />
      </div>
    </div>
  );
}
