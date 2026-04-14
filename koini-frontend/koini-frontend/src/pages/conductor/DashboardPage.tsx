import React, { useMemo } from 'react';
import { StatCard } from '../../components/data/StatCard';
import { Banknote, ReceiptText, ShieldCheck, Wallet } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { EmptyState } from '../../components/ui/EmptyState';
import { useNavigate } from 'react-router-dom';
import { formatKc } from '../../utils/money';
import { useMerchantDashboardSummary } from '../../hooks/useMerchantPortal';
import { TransactionList } from '../../components/data/TransactionList';
import type { Transaction } from '../../api/types';

export default function DashboardPage(): JSX.Element {
  const { state } = useAuth();
  const navigate = useNavigate();
  const isPending = state.user?.status === 'PENDING_VERIFICATION';

  const today = useMemo(() => new Date().toISOString().slice(0, 10), []);
  const { data, isLoading } = useMerchantDashboardSummary(today);
  const summary = data;
  const faresCount = summary?.today.faresCount ?? 0;
  const earningsKc = summary?.today.earningsKc ?? 0;
  const earningsUsd = summary?.today.earningsUsd ?? '$0.00';
  const walletBalanceUsd = summary?.wallet.balanceUsd ?? '$0.00';
  const walletBalanceKc = summary?.wallet.balanceKc ?? 0;

  const recentTransactions: Transaction[] | undefined = useMemo(() => {
    if (!summary?.recentTransactions) return undefined;
    return summary.recentTransactions.map((tx) => ({
      txId: tx.transactionId,
      txType: tx.type as Transaction['txType'],
      amountKc: tx.amountKc,
      amountUsd: tx.amountUsd,
      feeKc: tx.feeKc,
      status: tx.status as Transaction['status'],
      reference: tx.reference,
      description: null,
      createdAt: tx.createdAt,
      direction: tx.type === 'FARE_PAYMENT' ? 'credit' : 'debit',
    }));
  }, [summary]);

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
      <h1 className="text-2xl font-bold text-text-primary">Today</h1>
      <div className="grid md:grid-cols-3 gap-4">
        <StatCard
          title="Fares Collected"
          value={faresCount}
          icon={<ReceiptText className="w-5 h-5" />}
          variant="success"
          isLoading={isLoading}
        />
        <StatCard
          title="Earnings (KC)"
          value={earningsKc}
          subtitle={earningsUsd ? `~ ${earningsUsd}` : undefined}
          icon={<Banknote className="w-5 h-5" />}
          variant="primary"
          isLoading={isLoading}
        />
        <StatCard
          title="Earnings (USD)"
          value={earningsUsd}
          subtitle={`${formatKc(earningsKc)} KC`}
          icon={<Banknote className="w-5 h-5" />}
          variant="default"
          isLoading={isLoading}
        />
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        <StatCard
          title="Wallet Balance"
          value={walletBalanceUsd}
          subtitle={`${formatKc(walletBalanceKc)} KC`}
          icon={<Wallet className="w-5 h-5" />}
          variant="default"
          isLoading={isLoading}
        />
        <StatCard
          title="Pending Requests"
          value={summary?.requests.pendingCount ?? 0}
          subtitle={summary?.requests.lastRequestAt ?? 'No recent request'}
          icon={<ReceiptText className="w-5 h-5" />}
          variant="warning"
          isLoading={isLoading}
        />
      </div>

      <div className="bg-surface-card border border-surface-border rounded-2xl p-6">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-xs uppercase tracking-wider text-text-muted">Recent Transactions</div>
            <div className="text-lg font-semibold text-text-primary mt-1">Latest activity</div>
          </div>
          <button
            type="button"
            onClick={() => navigate('/merchant/transactions')}
            className="text-sm text-primary-400"
          >
            View all
          </button>
        </div>
        <div className="mt-4">
          <TransactionList transactions={recentTransactions} isLoading={isLoading} />
        </div>
      </div>
    </div>
  );
}
