import React from 'react';
import { Button } from '../components/ui/Button';

export default function NotFoundPage(): JSX.Element {
  return (
    <div className="min-h-screen bg-surface-bg flex flex-col items-center justify-center text-center gap-4">
      <h1 className="text-3xl font-bold text-text-primary">Page not found</h1>
      <p className="text-text-secondary">The page you are looking for does not exist.</p>
      <Button onClick={() => (window.location.href = '/')}>Go Home</Button>
    </div>
  );
}
