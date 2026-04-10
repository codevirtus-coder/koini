import React from 'react';
import { Outlet } from 'react-router-dom';
import { Home, Wallet, ArrowDownLeft, List } from 'lucide-react';
import { AppShell } from '../../components/layout/AppShell';
import type { NavItem } from '../../components/layout/Sidebar';

const navItems: NavItem[] = [
  { path: '/agent/dashboard', label: 'Dashboard', icon: Home },
  { path: '/agent/topup', label: 'Top Up', icon: Wallet },
  { path: '/agent/withdrawal', label: 'Withdrawal', icon: ArrowDownLeft },
  { path: '/agent/transactions', label: 'Transactions', icon: List },
];

export default function AgentLayout(): JSX.Element {
  return (
    <AppShell navItems={navItems} portalName="Agent" portalColorClass="bg-accent-500">
      <Outlet />
    </AppShell>
  );
}
