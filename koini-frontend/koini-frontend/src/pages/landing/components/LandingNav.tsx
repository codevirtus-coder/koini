import React, { useState } from 'react';
import { Menu } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { Drawer } from '../../../components/ui/Drawer';
import { KoiniLogo } from '../../../components/shared/KoiniLogo';

export function LandingNav(): JSX.Element {
  const [open, setOpen] = useState(false);
  return (
    <div className="fixed top-0 left-0 right-0 z-40 bg-surface-bg/80 backdrop-blur-md border-b border-surface-border">
      <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
        <KoiniLogo size={28} />
        <div className="hidden md:flex items-center gap-6 text-sm text-text-secondary">
          <a href="#how">How it works</a>
          <a href="#features">Features</a>
          <a href="#roles">Roles</a>
        </div>
        <div className="hidden md:flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => (window.location.href = '/login')}>Sign In</Button>
          <Button size="sm" onClick={() => (window.location.href = '/register')}>Get Started</Button>
        </div>
        <button className="md:hidden text-text-primary" onClick={() => setOpen(true)} aria-label="Open menu">
          <Menu className="w-5 h-5" />
        </button>
      </div>
      <Drawer isOpen={open} onClose={() => setOpen(false)} title="Menu">
        <div className="flex flex-col gap-3">
          <a href="#how" onClick={() => setOpen(false)}>How it works</a>
          <a href="#features" onClick={() => setOpen(false)}>Features</a>
          <a href="#roles" onClick={() => setOpen(false)}>Roles</a>
          <Button variant="outline" size="sm" onClick={() => (window.location.href = '/login')}>Sign In</Button>
          <Button size="sm" onClick={() => (window.location.href = '/register')}>Get Started</Button>
        </div>
      </Drawer>
    </div>
  );
}
