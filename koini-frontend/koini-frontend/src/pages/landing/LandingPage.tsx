import React from 'react';
import { LandingNav } from './components/LandingNav';
import { HeroSection } from './sections/HeroSection';
import { HowItWorksSection } from './sections/HowItWorksSection';
import { FeaturesSection } from './sections/FeaturesSection';
import { RolesSection } from './sections/RolesSection';
import { StatsSection } from './sections/StatsSection';
import { CtaSection } from './sections/CtaSection';

export default function LandingPage(): JSX.Element {
  return (
    <div className="bg-surface-bg text-text-primary">
      <LandingNav />
      <HeroSection />
      <HowItWorksSection />
      <FeaturesSection />
      <RolesSection />
      <StatsSection />
      <CtaSection />
    </div>
  );
}
