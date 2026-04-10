import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Settings, Send, Wallet, ArrowUpRight, ArrowDownLeft } from 'lucide-react';
import { useWalletBalance, useTransactionHistory } from '../../hooks/useWallet';
import { AmountDisplay } from '../../components/data/AmountDisplay';
import { TransactionList } from '../../components/data/TransactionList';
import { Button } from '../../components/ui/Button';
import { WalletBalanceSkeleton } from '../../components/ui/Skeleton';
import { PageHeader } from '../../components/layout/PageHeader';
import { FEATURES } from '../../utils/features';

export default function DashboardPage(): JSX.Element {
  const navigate = useNavigate();
  const { data: balance, isLoading } = useWalletBalance();
  const { data: txData, isLoading: txLoading } = useTransactionHistory({ size: 5, page: 0 });

  return (
    <div className="space-y-6">
      <PageHeader title="My Wallet" />
      {isLoading ? (
        <WalletBalanceSkeleton />
      ) : (
      <div className="bg-surface-card border border-surface-border rounded-2xl p-6 text-text-primary shadow-md">
          <div className="flex items-center justify-between">
            <div className="text-xs uppercase tracking-wider opacity-80">My Wallet</div>
            <Settings className="w-4 h-4" />
          </div>
          <div className="mt-4">
            <div className="text-3xl font-bold">{balance?.balanceUsd ?? '$0.00'}</div>
            <div className="text-sm opacity-80">{balance ? `${balance.balanceKc}` : '0'}</div>
            {balance?.points != null && (
              <div className="text-xs opacity-80 mt-1">Tokens: {balance.points.toLocaleString()}</div>
            )}
          </div>
          <div className="mt-4 flex gap-2">
            <Button size="sm" onClick={() => navigate('/client/pay')}>Pay Fare</Button>
            {FEATURES.p2pTransfers && (
              <Button size="sm" variant="outline" onClick={() => navigate('/client/transfer')}>Send</Button>
            )}
          </div>
        </div>
      )}

      <div className="grid grid-cols-2 gap-3">
          <button
            type="button"
            className="bg-surface-card border border-surface-border rounded-xl p-4 flex items-center gap-3 text-left hover:bg-surface-cardHover transition-colors"
            onClick={() => navigate('/client/pay')}
          >
          <Wallet className="w-5 h-5 text-primary-400" />
          <div className="text-sm text-text-primary">Pay Fare</div>
        </button>
        {FEATURES.p2pTransfers ? (
          <button
            type="button"
            className="bg-surface-card border border-surface-border rounded-xl p-4 flex items-center gap-3 text-left hover:bg-surface-cardHover transition-colors"
            onClick={() => navigate('/client/transfer')}
          >
            <Send className="w-5 h-5 text-primary-400" />
            <div className="text-sm text-text-primary">Send Credits</div>
          </button>
        ) : (
          <div className="bg-surface-card border border-surface-border rounded-xl p-4 flex items-center gap-3 opacity-60">
            <Send className="w-5 h-5 text-primary-400" />
            <div className="text-sm text-text-primary">Send Credits (coming soon)</div>
          </div>
        )}
          <button
            type="button"
            className="bg-surface-card border border-surface-border rounded-xl p-4 flex items-center gap-3 text-left hover:bg-surface-cardHover transition-colors"
            onClick={() => navigate('/client/topup')}
          >
          <ArrowUpRight className="w-5 h-5 text-primary-400" />
          <div className="text-sm text-text-primary">Top Up</div>
        </button>
        <div className="bg-surface-card border border-surface-border rounded-xl p-4 flex items-center gap-3 opacity-60">
          <ArrowDownLeft className="w-5 h-5 text-primary-400" />
          <div className="text-sm text-text-primary">Withdraw</div>
        </div>
      </div>

      <div>
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-text-primary">Recent Activity</h3>
          <span className="text-sm text-primary-400">See all</span>
        </div>
        <TransactionList transactions={txData?.content} isLoading={txLoading} />
      </div>

      {balance && (
        <div className="bg-surface-card border border-surface-border rounded-xl p-4">
          <div className="text-sm text-text-secondary">Current balance</div>
          <AmountDisplay amountKc={balance.balanceKc} size="lg" animated />
        </div>
      )}
    </div>
  );
}
