import React from 'react';
import { PaymentRequestCard } from '../../components/payment/PaymentRequestCard';

export default function RequestDetailPage(): JSX.Element {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Payment Request</h1>
      <PaymentRequestCard amountKc={50} passengerMaskedPhone="2637****4567" onApprove={() => undefined} onDecline={() => undefined} />
    </div>
  );
}
