import React from 'react';
import { AmountDisplay } from '../../../components/data/AmountDisplay';
import { Button } from '../../../components/ui/Button';

export function PhoneMockup(): JSX.Element {
  return (
    <div className="w-full max-w-xs mx-auto bg-surface-card border border-surface-border rounded-3xl p-5 shadow-lg">
      <div className="h-4 w-20 bg-surface-border rounded-full mx-auto mb-4" />
      <div className="bg-surface-card border border-surface-border rounded-2xl p-4 text-text-primary shadow-md">
        <div className="text-xs uppercase tracking-wider opacity-80">My Wallet</div>
        <div className="mt-3">
          <AmountDisplay amountKc={450} size="2xl" animated direction="neutral" />
        </div>
        <div className="flex gap-2 mt-4">
          <Button size="sm">Pay Fare</Button>
          <Button variant="outline" size="sm">Send</Button>
        </div>
      </div>
      <div className="mt-4 grid grid-cols-2 gap-2">
        <div className="bg-surface-cardHover rounded-xl p-3 text-xs text-text-secondary">Pay Fare</div>
        <div className="bg-surface-cardHover rounded-xl p-3 text-xs text-text-secondary">Send Credits</div>
        <div className="bg-surface-cardHover rounded-xl p-3 text-xs text-text-secondary">Top Up</div>
        <div className="bg-surface-cardHover rounded-xl p-3 text-xs text-text-secondary">Withdraw</div>
      </div>
    </div>
  );
}
