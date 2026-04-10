import React from 'react';
import { motion } from 'framer-motion';

const steps = [
  {
    title: 'Load your wallet',
    description: 'Hand cash to any KOINI agent. Credits hit your phone in seconds.',
  },
  {
    title: 'Generate your code',
    description: 'Dial *123# or open the app. Get a 6-digit code before boarding.',
  },
  {
    title: 'Pay exact fare',
    description: 'Show the merchant the code. $0.50 leaves your wallet. Exactly $0.50.',
  },
];

export function HowItWorksSection(): JSX.Element {
  return (
    <section id="how" className="py-20">
      <div className="max-w-6xl mx-auto px-4">
        <h2 className="text-2xl font-bold text-text-primary">Three steps. Exact change. Every time.</h2>
        <div className="grid md:grid-cols-3 gap-4 mt-8">
          {steps.map((step, i) => (
            <motion.div key={step.title} initial={{ opacity: 0, y: 12 }} whileInView={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.1 }} className="bg-surface-card border border-surface-border rounded-xl p-5">
              <div className="w-8 h-8 rounded-full bg-primary-500/10 text-primary-400 flex items-center justify-center text-sm font-bold">
                {i + 1}
              </div>
              <h3 className="text-lg font-semibold text-text-primary mt-3">{step.title}</h3>
              <p className="text-sm text-text-secondary mt-2">{step.description}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
