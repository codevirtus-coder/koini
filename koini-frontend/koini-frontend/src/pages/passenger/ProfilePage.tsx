import React from 'react';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';

export default function ProfilePage(): JSX.Element {
  const { state, logout } = useAuth();
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Profile</h1>
      <div className="bg-surface-card border border-surface-border rounded-xl p-5">
        <div className="text-sm text-text-secondary">Name</div>
        <div className="text-lg text-text-primary">{state.user?.fullName ?? 'N/A'}</div>
        <div className="mt-4 text-sm text-text-secondary">Phone</div>
        <div className="text-lg text-text-primary">{state.user?.maskedPhone}</div>
        <div className="mt-4 text-sm text-text-secondary">Role</div>
        <div className="text-lg text-text-primary">{state.user?.role}</div>
      </div>
      <Button variant="danger" onClick={logout}>Logout</Button>
    </div>
  );
}
