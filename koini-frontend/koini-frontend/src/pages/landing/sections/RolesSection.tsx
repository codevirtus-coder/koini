import React from 'react';
import { Users, Bus, Store, Shield } from 'lucide-react';

const roles = [
  { icon: Users, title: 'Client', desc: 'Pay instantly with tokens.', color: 'text-primary-400' },
  { icon: Bus, title: 'Merchant', desc: 'Redeem codes in seconds.', color: 'text-success-500' },
  { icon: Store, title: 'Agent', desc: 'Top up and withdraw cash.', color: 'text-accent-400' },
  { icon: Shield, title: 'Admin', desc: 'Monitor system health.', color: 'text-danger-400' },
];

export function RolesSection(): JSX.Element {
  return (
    <section id="roles" className="py-20">
      <div className="max-w-6xl mx-auto px-4">
        <h2 className="text-2xl font-bold text-text-primary">One platform, four people</h2>
        <div className="grid md:grid-cols-4 gap-4 mt-8">
          {roles.map((role) => {
            const Icon = role.icon;
            return (
              <div key={role.title} className="bg-surface-card border border-surface-border rounded-xl p-5">
                <Icon className={`w-6 h-6 ${role.color}`} />
                <h3 className="text-lg font-semibold text-text-primary mt-3">{role.title}</h3>
                <p className="text-sm text-text-secondary mt-1">{role.desc}</p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
