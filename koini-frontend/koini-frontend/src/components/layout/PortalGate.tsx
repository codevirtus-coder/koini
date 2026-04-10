import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import type { UserRole } from '../../api/types';
import { useAuth } from '../../hooks/useAuth';
import { KoiniLogo } from '../shared/KoiniLogo';

interface PortalGateProps {
  allowedRoles: UserRole[];
  children: React.ReactNode;
  redirectTo?: string;
}

export function PortalGate({ allowedRoles, children, redirectTo = '/login' }: PortalGateProps): JSX.Element {
  const { state, portalPath } = useAuth();
  const location = useLocation();

  if (state.isLoading) {
    return (
      <div className="min-h-screen bg-surface-bg flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <KoiniLogo size={48} />
          <div className="w-8 h-8 border-2 border-surface-border border-t-primary-500 rounded-full animate-spin" />
        </div>
      </div>
    );
  }

  if (!state.isAuthenticated) {
    return <Navigate to={redirectTo} replace state={{ from: location }} />;
  }

  if (state.user && !allowedRoles.includes(state.user.role)) {
    return <Navigate to={portalPath} replace />;
  }

  return <>{children}</>;
}
