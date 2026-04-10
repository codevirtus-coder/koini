import React, { useState } from 'react';
import { PaymentCodeInput } from '../../components/payment/PaymentCodeInput';
import { Button } from '../../components/ui/Button';
import { PaymentStatusBanner } from '../../components/payment/PaymentStatusBanner';
import { useRedeemCode } from '../../hooks/usePayment';
import type { RedeemCodeResponse } from '../../api/types';
import { useAuth } from '../../hooks/useAuth';
import { EmptyState } from '../../components/ui/EmptyState';
import { ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

function getErrorCode(err: unknown): string | undefined {
  const e = err as { response?: { data?: { errorCode?: string } } };
  return e?.response?.data?.errorCode;
}

export default function RedeemCodePage(): JSX.Element {
  const { state: authState } = useAuth();
  const navigate = useNavigate();
  const isPending = authState.user?.status === 'PENDING_VERIFICATION';

  const [code, setCode] = useState('');
  const [state, setState] = useState<'idle' | 'success' | 'failure'>('idle');
  const [error, setError] = useState<string | undefined>(undefined);
  const [result, setResult] = useState<RedeemCodeResponse | null>(null);
  const redeem = useRedeemCode();

  if (isPending) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-text-primary">Enter Client Code</h1>
        <EmptyState
          icon={<ShieldCheck className="w-7 h-7" />}
          title="Locked until approval"
          description="Redeeming client codes is disabled until an admin approves your merchant account."
          actionLabel="Submit Documents"
          onAction={() => navigate('/merchant/onboarding')}
        />
      </div>
    );
  }

  const handleConfirm = async () => {
    setError(undefined);
    try {
      const res = await redeem.mutateAsync({ code });
      setResult(res);
      setState('success');
    } catch (err: unknown) {
      const codeErr = getErrorCode(err);
      const message =
        codeErr === 'PAY_001'
          ? 'Code has expired. Ask client for a new code.'
          : codeErr === 'PAY_002'
          ? 'Invalid code. Please check and try again.'
          : 'This code has already been used.';
      setError(message);
      setState('failure');
    }
  };

  if (state === 'success' && result) {
    return (
      <PaymentStatusBanner
        variant="success"
        title="Payment Confirmed!"
        amountKc={result.amountKc}
        passengerMaskedPhone={result.passengerMaskedPhone}
        reference={result.reference}
        actionLabel="New Payment"
        onAction={() => {
          setState('idle');
          setCode('');
        }}
      />
    );
  }

  if (state === 'failure') {
    return (
      <PaymentStatusBanner
        variant="failure"
        title="Payment Failed"
        description={error}
        actionLabel="Try Again"
        onAction={() => {
          setState('idle');
          setCode('');
        }}
      />
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Enter Client Code</h1>
      <p className="text-sm text-text-secondary">Ask the client for their 6-digit code.</p>
      <PaymentCodeInput value={code} onChange={setCode} />
      <Button fullWidth onClick={handleConfirm} isLoading={redeem.isPending} disabled={code.length < 6}>
        Confirm Payment
      </Button>
    </div>
  );
}
