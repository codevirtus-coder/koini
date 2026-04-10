import React from 'react';
import { useAuth } from '../../hooks/useAuth';
import { Avatar } from '../ui/Avatar';

interface TopbarProps {
  portalName: string;
}

export function Topbar({ portalName }: TopbarProps): JSX.Element {
  const { state } = useAuth();
  return (
    <div className="flex items-center justify-between py-4 border-b border-surface-border">
      <div className="text-lg font-semibold text-text-primary">{portalName}</div>
      <div className="flex items-center gap-3">
        <Avatar name={state.user?.fullName ?? state.user?.maskedPhone} size="sm" />
        <div className="text-sm text-text-secondary hidden sm:block">{state.user?.maskedPhone}</div>
      </div>
    </div>
  );
}
