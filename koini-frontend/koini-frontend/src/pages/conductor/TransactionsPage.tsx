import React, { useState } from 'react';
import { useTransactionHistory, useWalletBalance } from '../../hooks/useWallet';
import { TransactionList } from '../../components/data/TransactionList';
import { useMerchantReceipt } from '../../hooks/useMerchantPortal';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { PageHeader } from '../../components/layout/PageHeader';
import { StatCard } from '../../components/data/StatCard';
import { Wallet } from 'lucide-react';
import { formatKc } from '../../utils/money';
import { notify } from '../../utils/notify';
import { getApiErrorMessage } from '../../utils/apiError';

export default function TransactionsPage(): JSX.Element {
  const { data, isLoading } = useTransactionHistory({ page: 0, size: 20 });
  const { data: balance, isLoading: balanceLoading } = useWalletBalance();
  const [open, setOpen] = useState(false);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const receiptQuery = useMerchantReceipt();

  const openReceipt = async (txId: string) => {
    setSelectedId(txId);
    try {
      await receiptQuery.mutateAsync(txId);
      setOpen(true);
    } catch (e) {
      notify.error(getApiErrorMessage(e));
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Transactions" subtitle="Review recent activity and receipts." />
      <div className="grid md:grid-cols-2 gap-4">
        <StatCard
          title="Wallet Balance"
          value={balance?.balanceUsd ?? '$0.00'}
          subtitle={balance ? `${formatKc(balance.balanceKc)} KC` : undefined}
          icon={<Wallet className="w-5 h-5" />}
          variant="default"
          isLoading={balanceLoading}
        />
        <StatCard
          title="Bank Koins"
          value={balance?.points ?? 0}
          subtitle={balance?.status ? `Status: ${balance.status}` : undefined}
          icon={<Wallet className="w-5 h-5" />}
          variant="primary"
          isLoading={balanceLoading}
        />
      </div>
      <TransactionList
        transactions={data?.content}
        isLoading={isLoading}
        onRowClick={(tx) => openReceipt(tx.txId)}
      />

      <Modal
        isOpen={open}
        onClose={() => setOpen(false)}
        title="Transaction Receipt"
        description="Details for this transaction."
        footer={
          <div className="flex justify-end">
            <Button variant="outline" onClick={() => setOpen(false)}>
              Close
            </Button>
          </div>
        }
      >
        {receiptQuery.isPending ? (
          <div className="text-sm text-text-secondary">Loading receipt...</div>
        ) : receiptQuery.data ? (
          <div className="space-y-3 text-sm text-text-secondary">
            <div className="grid sm:grid-cols-2 gap-3">
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Reference</div>
                <div className="text-text-primary font-medium">{receiptQuery.data.reference}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Status</div>
                <div className="text-text-primary font-medium">{receiptQuery.data.status}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Amount</div>
                <div className="text-text-primary font-medium">
                  {receiptQuery.data.amountUsd} ({receiptQuery.data.amountKc} KC)
                </div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Fee</div>
                <div className="text-text-primary font-medium">
                  {receiptQuery.data.feeUsd} ({receiptQuery.data.feeKc} KC)
                </div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Type</div>
                <div className="text-text-primary font-medium">{receiptQuery.data.type}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Date</div>
                <div className="text-text-primary font-medium">{receiptQuery.data.createdAt}</div>
              </div>
            </div>
            {receiptQuery.data.description && (
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Description</div>
                <div className="text-text-primary font-medium">{receiptQuery.data.description}</div>
              </div>
            )}
            <div className="text-xs text-text-muted">Transaction ID: {selectedId}</div>
          </div>
        ) : (
          <div className="text-sm text-text-secondary">No receipt details available.</div>
        )}
      </Modal>
    </div>
  );
}
