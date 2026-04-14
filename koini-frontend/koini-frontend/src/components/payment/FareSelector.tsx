import React, { useEffect, useState } from 'react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { cn } from '../../design/cn';
import { formatKc, KC_TO_USD } from '../../utils/money';

interface FareSelectorProps {
  fares: number[];
  value: number;
  onSelect: (amount: number) => void;
  onCustom: (amount: number) => void;
}

export function FareSelector({ fares, value, onSelect, onCustom }: FareSelectorProps): JSX.Element {
  const [customValue, setCustomValue] = useState<string>(value.toString());
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    if (!isEditing) {
      setCustomValue(value.toString());
    }
  }, [value, isEditing]);

  const formatUsdCompact = (amountKc: number): string => {
    const usd = amountKc * KC_TO_USD;
    const small = usd > 0 && usd < 0.01;
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: small ? 3 : 2,
      maximumFractionDigits: small ? 4 : 2,
    }).format(usd);
  };

  return (
    <div className="space-y-3">
      <div className="grid sm:grid-cols-2 gap-3">
        {fares.map((fare) => {
          const isActive = value === fare;
          return (
            <Button
              key={fare}
              size="md"
              variant={isActive ? 'primary' : 'outline'}
              className="w-full justify-between"
              onClick={() => onSelect(fare)}
            >
              {formatKc(fare)}
              <span className={cn('text-xs ml-2', isActive ? 'text-white/80' : 'text-text-secondary')}>
                {formatUsdCompact(fare)}
              </span>
            </Button>
          );
        })}
      </div>
      <Input
        label="Custom Amount"
        type="number"
        value={customValue}
        onFocus={() => setIsEditing(true)}
        onBlur={() => {
          setIsEditing(false);
          if (customValue === '') {
            setCustomValue('0');
            onCustom(0);
          }
        }}
        onChange={(e) => {
          const next = e.target.value;
          setCustomValue(next);
          if (next === '') return;
          onCustom(Number(next));
        }}
        fullWidth={false}
        containerClassName="max-w-xs"
      />
    </div>
  );
}
