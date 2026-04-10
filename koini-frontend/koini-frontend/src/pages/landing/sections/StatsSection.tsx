import React from 'react';
import { useCountUp } from '../../../hooks/useCountUp';

export function StatsSection(): JSX.Element {
  const seconds = useCountUp(3);
  const expiry = useCountUp(90);
  return (
    <section className="py-10 bg-surface-card border-y border-surface-border">
      <div className="max-w-6xl mx-auto px-4 grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
        <div>
          <div className="text-2xl font-bold text-text-primary">&lt; {seconds}s</div>
          <div className="text-xs text-text-secondary">Seconds to pay</div>
        </div>
        <div>
          <div className="text-2xl font-bold text-text-primary">{expiry}s</div>
          <div className="text-xs text-text-secondary">Code expires in</div>
        </div>
        <div>
          <div className="text-2xl font-bold text-text-primary">$0.01</div>
          <div className="text-xs text-text-secondary">Min denomination</div>
        </div>
        <div>
          <div className="text-2xl font-bold text-text-primary">Any phone</div>
          <div className="text-xs text-text-secondary">Works on</div>
        </div>
      </div>
    </section>
  );
}
