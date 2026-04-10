import React, { useMemo } from 'react';
import { Outlet } from 'react-router-dom';
import { Home, Ticket, Send, List, ShieldCheck } from 'lucide-react';
import { AppShell } from '../../components/layout/AppShell';
import type { NavItem } from '../../components/layout/Sidebar';
import { useAuth } from '../../hooks/useAuth';
import { Alert } from '../../components/ui/Alert';

export default function ConductorLayout(): JSX.Element {
  const { state } = useAuth();
  const isPending = state.user?.status === 'PENDING_VERIFICATION';

  const navItems = useMemo<NavItem[]>(() => {
    if (isPending) {
      return [
        { path: '/merchant/dashboard', label: 'Dashboard', icon: Home },
        { path: '/merchant/onboarding', label: 'Verification', icon: ShieldCheck },
        { path: '/merchant/transactions', label: 'Transactions', icon: List },
      ];
    }
    return [
      { path: '/merchant/dashboard', label: 'Dashboard', icon: Home },
      { path: '/merchant/redeem', label: 'Redeem', icon: Ticket },
      { path: '/merchant/request', label: 'Request', icon: Send },
      { path: '/merchant/transactions', label: 'Transactions', icon: List },
    ];
  }, [isPending]);

  return (
    <AppShell navItems={navItems} portalName="Merchant" portalColorClass="bg-success-500">
      {isPending && (
        <div className="max-w-3xl">
          <Alert
            variant="warning"
            title="Merchant pending approval"
            description="Submit your verification documents. You can browse the dashboard, but redeem/request actions stay locked until an admin approves your account."
          />
        </div>
      )}
      <Outlet />
    </AppShell>
  );
}
