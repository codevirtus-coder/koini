import React from 'react';
import { Shield, Zap, Smartphone, Globe, List, Coins } from 'lucide-react';

const features = [
  { icon: Globe, title: 'Works offline', desc: 'USSD works on 2G' },
  { icon: Smartphone, title: 'Any phone', desc: 'Feature phone or smartphone' },
  { icon: Shield, title: 'PIN protected', desc: 'Your money is yours' },
  { icon: Zap, title: 'Instant', desc: 'Payment in under 3 seconds' },
  { icon: List, title: 'Full history', desc: 'Every trip recorded' },
  { icon: Coins, title: 'Exact change', desc: 'Never lose 50c again' },
];

export function FeaturesSection(): JSX.Element {
  return (
    <section id="features" className="py-20">
      <div className="max-w-6xl mx-auto px-4">
        <h2 className="text-2xl font-bold text-text-primary">Built for Zimbabwe. Built for everyone.</h2>
        <div className="grid md:grid-cols-3 gap-4 mt-8">
          {features.map((f) => {
            const Icon = f.icon;
            return (
              <div key={f.title} className="bg-surface-card border border-surface-border rounded-xl p-5">
                <Icon className="w-6 h-6 text-accent-400" />
                <h3 className="text-lg font-semibold text-text-primary mt-3">{f.title}</h3>
                <p className="text-sm text-text-secondary mt-1">{f.desc}</p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
