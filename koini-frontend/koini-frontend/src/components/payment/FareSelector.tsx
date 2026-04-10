import React from 'react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { cn } from '../../design/cn';
import { formatUsd, formatKc } from '../../utils/money';

interface FareSelectorProps {
  fares: number[];
  value: number;
  onSelect: (amount: number) => void;
  onCustom: (amount: number) => void;
}

export function FareSelector({ fares, value, onSelect, onCustom }: FareSelectorProps): JSX.Element {
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
                {formatUsd(fare)}
              </span>
            </Button>
          );
        })}
      </div>
      <Input
        label="Custom Amount"
        type="number"
        value={value}
        onChange={(e) => onCustom(Number(e.target.value))}
        fullWidth={false}
        containerClassName="max-w-xs"
      />
    </div>
  );
}
