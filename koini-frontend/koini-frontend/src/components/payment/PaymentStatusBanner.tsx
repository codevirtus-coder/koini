import React from 'react';
import { motion } from 'framer-motion';
import { Check, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { formatUsd, formatKc } from '../../utils/money';

interface PaymentStatusBannerProps {
  variant: 'success' | 'failure';
  title: string;
  description?: string;
  amountKc?: number;
  reference?: string;
  passengerMaskedPhone?: string;
  onAction?: () => void;
  actionLabel?: string;
}

export function PaymentStatusBanner({
  variant,
  title,
  description,
  amountKc,
  reference,
  passengerMaskedPhone,
  onAction,
  actionLabel,
}: PaymentStatusBannerProps): JSX.Element {
  const color = variant === 'success' ? 'text-success-500' : 'text-danger-500';
  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center gap-4">
      <motion.div initial={{ scale: 0 }} animate={{ scale: 1 }} className={color}>
        <div className="w-20 h-20 rounded-full border-2 flex items-center justify-center">
          {variant === 'success' ? <Check className="w-8 h-8" /> : <X className="w-8 h-8" />}
        </div>
      </motion.div>
      <h2 className="text-2xl font-bold text-text-primary">{title}</h2>
      {description && <p className="text-sm text-text-secondary max-w-sm">{description}</p>}
      {amountKc !== undefined && (
        <div className="text-lg font-semibold text-text-primary">
          {formatKc(amountKc)} ({formatUsd(amountKc)})
        </div>
      )}
      {passengerMaskedPhone && <div className="text-xs text-text-secondary">Client: {passengerMaskedPhone}</div>}
      {reference && <div className="text-xs text-text-secondary">Reference: {reference}</div>}
      {actionLabel && onAction && (
        <Button variant={variant === 'success' ? 'success' : 'danger'} onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
