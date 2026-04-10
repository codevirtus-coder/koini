import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useSearchParams } from 'react-router-dom';
import { PageHeader } from '../../components/layout/PageHeader';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { useAuth } from '../../hooks/useAuth';
import { usePesepayTopupConfirm, usePesepayTopupInitiate } from '../../hooks/useTopups';
import { useWalletBalance } from '../../hooks/useWallet';
import type { PendingPesepayTopup } from '../../utils/storage';
import { clearPendingPesepayTopup, getPendingPesepayTopup, setPendingPesepayTopup } from '../../utils/storage';
import { notify } from '../../utils/notify';
import { getApiErrorMessage } from '../../utils/apiError';
import { pesepayTopupInitiateSchema } from '../../utils/validators';
import type { PesepayTopupConfirmResponse, PesepayTopupInitiateRequest, PesepayTopupInitiateResponse } from '../../api/types';
import { formatKc, formatUsd, KC_TO_USD } from '../../utils/money';

type FormValues = PesepayTopupInitiateRequest;

const PESEPAY_WINDOW_NAME = 'koini_pesepay';

export default function TopUpPage(): JSX.Element {
  const { state } = useAuth();
  const userId = state.user?.userId ?? null;
  const [searchParams] = useSearchParams();

  const initiate = usePesepayTopupInitiate();
  const confirm = usePesepayTopupConfirm();
  const { data: walletBalance, isLoading: walletLoading } = useWalletBalance();

  const [pending, setPending] = useState<PendingPesepayTopup | null>(null);
  const [lastConfirm, setLastConfirm] = useState<PesepayTopupConfirmResponse | null>(null);
  const didAutoConfirmRef = useRef<string | null>(null);
  const pesepayWindowRef = useRef<Window | null>(null);
  const [popupBlocked, setPopupBlocked] = useState(false);
  const submitInFlightRef = useRef(false);

  const txIdFromUrl = searchParams.get('txId');
  const referenceNumberFromUrl = searchParams.get('referenceNumber') ?? undefined;

  useEffect(() => {
    if (!userId) return;
    setPending(getPendingPesepayTopup(userId));
  }, [userId]);

  useEffect(() => {
    if (!userId || !txIdFromUrl) return;
    const existing = getPendingPesepayTopup(userId);
    if (existing?.txId === txIdFromUrl) return;

    const next: PendingPesepayTopup = {
      txId: txIdFromUrl,
      createdAt: new Date().toISOString(),
    };
    setPendingPesepayTopup(userId, next);
    setPending(next);
  }, [txIdFromUrl, userId]);

  useEffect(() => {
    if (!userId || !txIdFromUrl) return;
    if (didAutoConfirmRef.current === txIdFromUrl) return;
    didAutoConfirmRef.current = txIdFromUrl;

    confirm
      .mutateAsync({
        txId: txIdFromUrl,
        body: referenceNumberFromUrl ? { referenceNumber: referenceNumberFromUrl } : undefined,
      })
      .then((res) => {
        setLastConfirm(res);
        if (res.paid) {
          clearPendingPesepayTopup(userId);
          setPending(null);
          notify.success('Top up confirmed. Wallet updated.');
        }
      })
      .catch((e) => {
        notify.error(getApiErrorMessage(e));
      });
  }, [confirm, referenceNumberFromUrl, txIdFromUrl, userId]);

  const defaultValues = useMemo<FormValues>(
    () => ({
      amountKc: 100,
      currencyCode: 'USD',
    }),
    []
  );

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(pesepayTopupInitiateSchema), defaultValues });

  const watchedAmount = watch('amountKc');
  const kcPerUsd = useMemo(() => Math.round(1 / KC_TO_USD), []);

  const openPesepayWindow = (url: string): void => {
    if (pesepayWindowRef.current && !pesepayWindowRef.current.closed) {
      try {
        pesepayWindowRef.current.location.replace(url);
        pesepayWindowRef.current.focus();
        setPopupBlocked(false);
        return;
      } catch {
        // fall through to window.open
      }
    }
    const win = window.open(url, PESEPAY_WINDOW_NAME);
    if (!win) {
      setPopupBlocked(true);
      notify.warning('Popup blocked. Please click "Open Pesepay" to continue.');
      return;
    }
    setPopupBlocked(false);
    pesepayWindowRef.current = win;
    try {
      win.opener = null;
      win.focus();
    } catch {
      // ignore
    }
  };

  const clearPending = (): void => {
    if (!userId) return;
    clearPendingPesepayTopup(userId);
    setPending(null);
    setLastConfirm(null);
  };

  const onSubmit = async (values: FormValues) => {
    if (!userId) return;
    if (submitInFlightRef.current) return;
    submitInFlightRef.current = true;
    setLastConfirm(null);

    try {
      // Open a placeholder tab immediately (in the click gesture) so browsers don't block it.
      const placeholder = window.open('about:blank', PESEPAY_WINDOW_NAME);
      if (!placeholder) {
        setPopupBlocked(true);
      } else {
        setPopupBlocked(false);
        pesepayWindowRef.current = placeholder;
        try {
          placeholder.opener = null;
          placeholder.document.title = 'Redirecting to Pesepay...';
        } catch {
          // ignore
        }
      }

      const payload: FormValues = {
        amountKc: values.amountKc,
        currencyCode: values.currencyCode?.toUpperCase() || undefined,
      };

      const res: PesepayTopupInitiateResponse = await initiate.mutateAsync(payload);
      const next: PendingPesepayTopup = {
        txId: res.txId,
        reference: res.reference,
        amountKc: res.amountKc,
        redirectUrl: res.redirectUrl,
        createdAt: new Date().toISOString(),
      };
      setPendingPesepayTopup(userId, next);
      setPending(next);

      if (placeholder && !placeholder.closed) {
        try {
          placeholder.location.replace(res.redirectUrl);
          placeholder.focus();

          // If the window is still blank shortly after, assume navigation was blocked.
          window.setTimeout(() => {
            try {
              if (placeholder.closed) return;
              if (placeholder.location.href === 'about:blank') {
                placeholder.close();
                pesepayWindowRef.current = null;
                setPopupBlocked(true);
                notify.warning('Could not open Pesepay automatically. Please click "Open Pesepay".');
              }
            } catch {
              // Cross-origin navigation succeeded; ignore.
            }
          }, 1200);
        } catch {
          openPesepayWindow(res.redirectUrl);
        }
      } else {
        // No placeholder => user likely has popups blocked. Let them open manually.
        setPopupBlocked(true);
      }
      notify.info('Complete payment in Pesepay, then return here to confirm.');
    } catch (e) {
      if (pesepayWindowRef.current && !pesepayWindowRef.current.closed) {
        try {
          pesepayWindowRef.current.close();
        } catch {
          // ignore
        } finally {
          pesepayWindowRef.current = null;
        }
      }
      notify.error(getApiErrorMessage(e));
    } finally {
      submitInFlightRef.current = false;
    }
  };

  const onConfirm = async () => {
    if (!userId || !pending) return;

    try {
      const res = await confirm.mutateAsync({
        txId: pending.txId,
        body: referenceNumberFromUrl ? { referenceNumber: referenceNumberFromUrl } : undefined,
      });

      setLastConfirm(res);
      if (res.paid) {
        clearPendingPesepayTopup(userId);
        setPending(null);
        notify.success('Top up confirmed. Wallet updated.');
      } else {
        notify.info('Payment not confirmed yet. Try again in a moment.');
      }
    } catch (e) {
      notify.error(getApiErrorMessage(e));
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Top Up" subtitle="Add KC to your wallet using Pesepay." />

      <div className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-2xl">
        <div className="text-xs uppercase tracking-wider text-text-muted">Current Wallet</div>
        {walletLoading ? (
          <div className="mt-3 text-sm text-text-secondary">Loading...</div>
        ) : (
          <div className="mt-3">
            <div className="text-2xl font-bold text-text-primary">{walletBalance?.balanceUsd ?? '$0.00'}</div>
            <div className="text-sm text-text-secondary">
              {walletBalance ? `${formatKc(walletBalance.balanceKc)} KC (${formatUsd(walletBalance.balanceKc)})` : '—'}
            </div>
            {walletBalance?.points != null && (
              <div className="text-xs text-text-secondary mt-1">Tokens: {walletBalance.points.toLocaleString()}</div>
            )}
          </div>
        )}
      </div>

      {!pending ? (
        <div className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-2xl space-y-5">
          <Alert
            variant="info"
            title="How it works"
            description="You will be redirected to Pesepay to complete the payment. Keep this tab open so you can confirm when done."
          />
          {popupBlocked && (
            <Alert
              variant="warning"
              title="Popup blocked"
              description="Allow popups for this site, or complete the payment by clicking 'Open Pesepay' after initiation."
            />
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Amount (KC)"
              type="number"
              error={errors.amountKc?.message}
              {...register('amountKc', { valueAsNumber: true })}
            />
            <div className="text-xs text-text-secondary">
              Rate: 1 USD = <span className="text-text-primary font-semibold">{kcPerUsd.toLocaleString()} KC</span>
            </div>
            <div className="text-sm text-text-secondary">
              You are topping up: <span className="text-text-primary font-semibold">{formatKc(Number(watchedAmount || 0))} KC</span> ({formatUsd(Number(watchedAmount || 0))})
            </div>
            <Select
              label="Currency"
              options={[
                { label: 'USD', value: 'USD' },
              ]}
              error={errors.currencyCode?.message}
              {...register('currencyCode')}
            />
            <Button type="submit" size="lg" isLoading={initiate.isPending} fullWidth>
              Continue to Pesepay
            </Button>
          </form>
        </div>
      ) : (
        <div className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-2xl space-y-5">
          <Alert
            variant="warning"
            title="Top up pending"
            description="Complete the payment in Pesepay, then confirm here to credit your wallet."
          />

          <div className="grid sm:grid-cols-2 gap-3 text-sm text-text-secondary">
            <div>
              <div className="text-xs uppercase tracking-wider opacity-80">Transaction</div>
              <div className="text-text-primary font-medium break-all">{pending.txId}</div>
            </div>
            <div>
              <div className="text-xs uppercase tracking-wider opacity-80">Reference</div>
              <div className="text-text-primary font-medium break-all">{pending.reference ?? '—'}</div>
            </div>
            <div>
              <div className="text-xs uppercase tracking-wider opacity-80">Amount</div>
              <div className="text-text-primary font-medium">
                {pending.amountKc != null ? `${formatKc(pending.amountKc)} KC (${formatUsd(pending.amountKc)})` : '—'}
              </div>
            </div>
          </div>

          {lastConfirm && (
            <Alert
              variant={lastConfirm.paid ? 'success' : 'info'}
              title={lastConfirm.paid ? 'Payment confirmed' : 'Not paid yet'}
              description={
                lastConfirm.paid
                  ? `Status: ${lastConfirm.status} • Credited: ${lastConfirm.amountKcCredited ?? 0} KC • Tokens: ${lastConfirm.pointsAdded ?? 0}`
                  : `Status: ${lastConfirm.status}`
              }
            />
          )}

          <div className="flex flex-col sm:flex-row gap-2">
            <Button
              variant="outline"
              onClick={() => {
                if (pending.redirectUrl) openPesepayWindow(pending.redirectUrl);
                else notify.warning('Missing redirect URL for this transaction.');
              }}
            >
              Open Pesepay
            </Button>
            <Button onClick={onConfirm} isLoading={confirm.isPending}>
              Check Payment
            </Button>
            <Button variant="ghost" onClick={clearPending}>
              Start New
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
