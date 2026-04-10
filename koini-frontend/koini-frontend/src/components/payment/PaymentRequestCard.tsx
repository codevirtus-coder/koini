import React from 'react';
import { Button } from '../ui/Button';
import { formatUsd, formatKc } from '../../utils/money';

interface PaymentRequestCardProps {
  amountKc: number;
  passengerMaskedPhone: string;
  onApprove: () => void;
  onDecline: () => void;
  isLoading?: boolean;
}

export function PaymentRequestCard({ amountKc, passengerMaskedPhone, onApprove, onDecline, isLoading }: PaymentRequestCardProps): JSX.Element {
  return (
      <div className="bg-warning-500/10 border border-warning-500/30 rounded-xl p-4">
        <div className="text-sm text-text-secondary">Incoming payment request</div>
        <div className="text-lg font-semibold text-text-primary mt-1">{formatKc(amountKc)} ({formatUsd(amountKc)})</div>
      <div className="text-xs text-text-secondary mt-1">Client: {passengerMaskedPhone}</div>
      <div className="flex gap-2 mt-4">
        <Button variant="success" size="sm" onClick={onApprove} isLoading={isLoading}>Approve</Button>
        <Button variant="danger" size="sm" onClick={onDecline} disabled={isLoading}>Decline</Button>
      </div>
    </div>
  );
}
