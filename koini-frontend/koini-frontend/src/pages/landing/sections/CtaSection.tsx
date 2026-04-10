import React from 'react';
import { Button } from '../../../components/ui/Button';

export function CtaSection(): JSX.Element {
  return (
    <section className="py-20">
      <div className="max-w-4xl mx-auto px-4 text-center">
        <h2 className="text-3xl font-bold text-text-primary">Ready to ditch the change problem?</h2>
        <p className="text-text-secondary mt-2">Join KOINI and pay exact fare, every time.</p>
        <div className="flex justify-center gap-3 mt-6">
          <Button size="lg">Client sign up</Button>
          <Button variant="outline" size="lg">Merchant sign up</Button>
        </div>
      </div>
    </section>
  );
}
