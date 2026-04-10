import React from 'react';
import { useNavigate } from 'react-router-dom';
import { TransferForm } from '../../components/forms/TransferForm';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { PageHeader } from '../../components/layout/PageHeader';
import { FEATURES } from '../../utils/features';

export default function TransferPage(): JSX.Element {
  const navigate = useNavigate();

  if (!FEATURES.p2pTransfers) {
    return (
      <div className="space-y-6">
        <PageHeader title="Transfer" subtitle="Send KC to another user." />
        <div className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-2xl space-y-4">
          <Alert
            variant="warning"
            title="Transfers are disabled"
            description="P2P transfers are turned off for now. You can still top up and pay fares."
          />
          <Button variant="outline" onClick={() => navigate('/client/dashboard')}>
            Back to Wallet
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader title="Send Credits" />
      <TransferForm />
    </div>
  );
}
