import React, { useRef } from 'react';
import { cn } from '../../design/cn';

interface PaymentCodeInputProps {
  value: string;
  onChange: (value: string) => void;
  error?: string;
}

export function PaymentCodeInput({ value, onChange, error }: PaymentCodeInputProps): JSX.Element {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const digits = value.padEnd(6, ' ').slice(0, 6).split('');
  const activeIndex = Math.min(value.length, 5);

  const handleChange = (next: string) => {
    const cleaned = next.replace(/\D/g, '').slice(0, 6);
    onChange(cleaned);
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    handleChange(e.clipboardData.getData('text'));
  };

  return (
    <div className="w-full relative">
      <div className="flex gap-2 justify-center" onClick={() => inputRef.current?.focus()}>
        {digits.map((d, i) => (
          <div
            key={`digit-${i}`}
            className={cn(
              'w-12 h-14 rounded-lg border border-surface-borderMd bg-surface-bg flex items-center justify-center text-2xl font-mono',
              i === activeIndex && 'border-primary-500 ring-2 ring-primary-500/30',
              error && 'border-danger-500'
            )}
          >
            {d}
          </div>
        ))}
      </div>
      <input
        ref={inputRef}
        type="tel"
        inputMode="numeric"
        className="absolute opacity-0 pointer-events-none"
        value={value}
        onChange={(e) => handleChange(e.target.value)}
        onPaste={handlePaste}
      />
      {error && <p className="text-xs text-danger-500 mt-2 text-center">{error}</p>}
    </div>
  );
}
