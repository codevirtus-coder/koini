import React, { useState } from 'react';
import { WithdrawalForm } from '../../components/forms/WithdrawalForm';
import { Button } from '../../components/ui/Button';
import { useConfirmWithdrawal, useReverseWithdrawal } from '../../hooks/useAgent';
import { notify } from '../../utils/notify';

export default function WithdrawalPage(): JSX.Element {
  const [withdrawalId, setWithdrawalId] = useState<string | null>(null);
  const confirm = useConfirmWithdrawal();
  const reverse = useReverseWithdrawal();

  if (withdrawalId) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold text-text-primary">Confirm Cash Handover</h1>
        <div className="bg-warning-500/10 border border-warning-500/30 rounded-xl p-4">
          IMPORTANT: Hand cash to client before confirming.
        </div>
        <div className="flex gap-3">
          <Button variant="success" onClick={async () => { await confirm.mutateAsync(withdrawalId); notify.success('Withdrawal confirmed'); setWithdrawalId(null); }}>
            Cash Given - Confirm
          </Button>
          <Button variant="danger" onClick={async () => { await reverse.mutateAsync({ id: withdrawalId, reason: 'Reversed by agent' }); notify.warning('Withdrawal reversed'); setWithdrawalId(null); }}>
            Reverse Withdrawal
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Process Cash Withdrawal</h1>
      <WithdrawalForm onSuccess={(id) => setWithdrawalId(id)} />
    </div>
  );
}
