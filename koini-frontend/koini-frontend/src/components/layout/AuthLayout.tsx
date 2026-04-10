import React from 'react';
import { KoiniLogo } from '../shared/KoiniLogo';
import { Link } from 'react-router-dom';

interface AuthLayoutProps {
  children: React.ReactNode;
}

export function AuthLayout({ children }: AuthLayoutProps): JSX.Element {
  return (
    <div className="min-h-screen bg-surface-bg flex items-center justify-center px-4">
      <div className="w-full max-w-md bg-surface-card border border-surface-border rounded-2xl p-8">
        <div className="flex justify-center mb-6">
          <Link to="/" aria-label="Go to homepage" title="Go to homepage">
            <KoiniLogo size={40} />
          </Link>
        </div>
        {children}
      </div>
    </div>
  );
}
