import React, { useRef } from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../design/cn';
import { shakeVariants } from '../../design/animations';
import { tw } from '../../design/tokens';

interface PinInputProps {
  value: string;
  onChange: (value: string) => void;
  error?: string;
  isLoading?: boolean;
  label?: string;
}

export function PinInput({ value, onChange, error, isLoading, label }: PinInputProps): JSX.Element {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const digits = value.padEnd(4, ' ').slice(0, 4).split('');
  const activeIndex = Math.min(value.length, 3);

  const handleChange = (next: string) => {
    const cleaned = next.replace(/\D/g, '').slice(0, 4);
    onChange(cleaned);
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const text = e.clipboardData.getData('text');
    handleChange(text);
  };

  return (
    <div className="w-full relative">
      {label && <label className={tw.label}>{label}</label>}
      <motion.div
        variants={shakeVariants}
        animate={error ? 'shake' : undefined}
        className="flex gap-3"
        onClick={() => inputRef.current?.focus()}
      >
        {digits.map((d, i) => (
          <div
            key={`pin-${i}`}
            className={cn(
              'h-14 w-14 rounded-xl border flex items-center justify-center text-2xl font-mono',
              'bg-surface-bg border-surface-borderMd',
              i === activeIndex && 'border-primary-500 ring-2 ring-primary-500/30',
              error && 'border-danger-500',
              isLoading && 'opacity-60'
            )}
          >
            {d.trim() ? <span className="text-text-primary">?</span> : <span className="text-text-muted"> </span>}
          </div>
        ))}
      </motion.div>
      <input
        ref={inputRef}
        type="tel"
        inputMode="numeric"
        autoComplete="one-time-code"
        className="absolute opacity-0 pointer-events-none"
        value={value}
        onChange={(e) => handleChange(e.target.value)}
        onPaste={handlePaste}
      />
      {error && <p className={cn(tw.errorMsg, 'animate-slide-down')}>{error}</p>}
    </div>
  );
}
