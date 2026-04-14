import React, { useEffect } from 'react';
import { motion } from 'framer-motion';
import { CodeCountdownRing } from './CodeCountdownRing';
import { useCountdown } from '../../hooks/useCountdown';
import { formatUsd, formatKc } from '../../utils/money';
import { Button } from '../ui/Button';
import { notify } from '../../utils/notify';
import { cn } from '../../design/cn';

interface PaymentCodeDisplayProps {
  code: string;
  expiresAt: string;
  amountKc: number;
  feeKc?: number;
  onExpire?: () => void;
  onNewCode?: () => void;
}

export function PaymentCodeDisplay({ code, expiresAt, amountKc, feeKc = 0, onExpire, onNewCode }: PaymentCodeDisplayProps): JSX.Element {
  const { secondsLeft, percentage, isExpired, isWarning, isUrgent } = useCountdown(expiresAt);

  useEffect(() => {
    if (isExpired && onExpire) onExpire();
  }, [isExpired, onExpire]);

  const handleCopy = async () => {
    await navigator.clipboard.writeText(code.replace(/\s/g, ''));
    notify.success('Code copied');
  };

  const digits = code.replace(/\s/g, '').padEnd(6, ' ').slice(0, 6).split('');

  return (
    <div className="flex flex-col items-center text-center gap-6">
      <CodeCountdownRing percentage={percentage} secondsLeft={secondsLeft} />
      <div className="space-y-3">
        <div className="flex gap-2 justify-center">
          {digits.map((d, i) => (
            <motion.div
              key={`code-${i}`}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.05 }}
              className={cn(
                'w-12 h-14 rounded-lg border border-surface-borderMd bg-surface-bg flex items-center justify-center text-2xl font-mono',
                (isWarning || isUrgent) && 'border-accent-500',
                isUrgent && 'border-danger-500'
              )}
            >
              {d}
            </motion.div>
          ))}
        </div>
        <div className="text-sm text-text-secondary">Show this to the merchant</div>
        <div className="text-xs uppercase tracking-wider text-text-muted">Fare amount</div>
        <div className="text-lg font-bold text-text-primary">{formatKc(amountKc)} KC • {formatUsd(amountKc)}</div>
        {feeKc > 0 && (
          <div className="text-xs text-text-secondary">
            Service fee: {formatKc(feeKc)} KC • Total charged: {formatKc(amountKc + feeKc)} KC • {formatUsd(amountKc + feeKc)}
          </div>
        )}
      </div>
      <div className="flex gap-2">
        <Button variant="outline" onClick={handleCopy}>Copy Code</Button>
        {onNewCode && (
          <Button variant="secondary" onClick={onNewCode} disabled={!isExpired}>
            Generate New Code
          </Button>
        )}
      </div>
      <div className="text-xs text-text-muted">This code is single-use and expires in {secondsLeft}s</div>
    </div>
  );
}
