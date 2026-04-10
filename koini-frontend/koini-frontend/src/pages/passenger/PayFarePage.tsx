import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { FareSelector } from '../../components/payment/FareSelector';
import { PinInput } from '../../components/ui/PinInput';
import { Button } from '../../components/ui/Button';
import { PaymentCodeDisplay } from '../../components/payment/PaymentCodeDisplay';
import { useGeneratePaymentCode } from '../../hooks/usePayment';
import { useWalletBalance } from '../../hooks/useWallet';
import { formatUsd, formatKc } from '../../utils/money';
import { getApiErrorCode, getApiErrorMessage, getApiErrorStatus } from '../../utils/apiError';
import { notify } from '../../utils/notify';

export default function PayFarePage(): JSX.Element {
  const [step, setStep] = useState<'idle' | 'pin' | 'code'>('idle');
  const [amount, setAmount] = useState(50);
  const [pin, setPin] = useState('');
  const { data: balance } = useWalletBalance();
  const generate = useGeneratePaymentCode();
  const [codeData, setCodeData] = useState<{ code: string; expiresAt: string } | null>(null);
  const navigate = useNavigate();
  const location = useLocation();

  const handleGenerate = async () => {
    try {
      const res = await generate.mutateAsync({ amountKc: amount, pin });
      setCodeData({ code: res.code, expiresAt: res.expiresAt });
      setStep('code');
    } catch (e) {
      const code = getApiErrorCode(e);
      const status = getApiErrorStatus(e);
      if (code === 'AUTH_006' || status === 428) {
        notify.info('Please set your PIN to continue.');
        navigate(`/setup-pin?next=${encodeURIComponent(location.pathname)}`);
        return;
      }
      notify.error(getApiErrorMessage(e));
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Pay Fare</h1>

      {step === 'idle' && (
        <div className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-3xl space-y-5">
          <FareSelector fares={[30, 50, 100, 200]} value={amount} onSelect={setAmount} onCustom={setAmount} />
          <div className="grid sm:grid-cols-3 gap-3 text-sm text-text-secondary">
            <div>Balance: {balance ? `${formatKc(balance.balanceKc)} (${balance.balanceUsd})` : '...'}</div>
            <div>Fee: {formatKc(2)} ({formatUsd(2)})</div>
            <div className="font-semibold text-text-primary">You will pay: {formatKc(amount + 2)} ({formatUsd(amount + 2)})</div>
          </div>
          <div className="flex justify-end">
            <Button size="lg" onClick={() => setStep('pin')}>
              Generate Code
            </Button>
          </div>
        </div>
      )}

      {step === 'pin' && (
        <div className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-xl space-y-4">
          <h2 className="text-lg font-semibold text-text-primary">Enter your PIN</h2>
          <PinInput value={pin} onChange={setPin} />
          <div className="flex justify-end">
            <Button size="lg" isLoading={generate.isPending} onClick={handleGenerate}>
              Confirm
            </Button>
          </div>
        </div>
      )}

      {step === 'code' && codeData && (
        <div className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-3xl">
          <PaymentCodeDisplay
            code={codeData.code}
            expiresAt={codeData.expiresAt}
            amountKc={amount}
            onNewCode={() => {
              setStep('idle');
              setPin('');
            }}
          />
        </div>
      )}
    </div>
  );
}
