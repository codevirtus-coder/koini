import React from 'react';
import { motion } from 'framer-motion';
import { Button } from '../../../components/ui/Button';
import { PhoneMockup } from '../components/PhoneMockup';

export function HeroSection(): JSX.Element {
  return (
    <section className="relative min-h-screen flex items-center pt-20">
      <div className="absolute inset-0">
        <div className="absolute -top-32 -left-32 w-96 h-96 bg-primary-600/30 blur-3xl rounded-full" />
        <div className="absolute bottom-0 right-0 w-80 h-80 bg-accent-500/30 blur-3xl rounded-full" />
      </div>
      <div className="relative max-w-6xl mx-auto px-4 grid md:grid-cols-2 gap-10 items-center">
        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
          <h1 className="text-4xl md:text-5xl font-extrabold text-text-primary">No more losing change.</h1>
          <p className="text-text-secondary mt-4">
            Pay the exact kombi fare from any phone - even without internet.
          </p>
          <div className="flex gap-3 mt-6">
            <Button size="lg">Get Started</Button>
            <Button variant="ghost" size="lg">See how it works</Button>
          </div>
        </motion.div>
        <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
          <PhoneMockup />
        </motion.div>
      </div>
    </section>
  );
}
