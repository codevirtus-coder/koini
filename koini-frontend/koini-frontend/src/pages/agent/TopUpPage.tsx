import React from 'react';
import { TopUpForm } from '../../components/forms/TopUpForm';

export default function TopUpPage(): JSX.Element {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Load Client Wallet</h1>
      <TopUpForm />
    </div>
  );
}
