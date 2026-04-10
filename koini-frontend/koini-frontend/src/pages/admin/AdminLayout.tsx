import React from 'react';
import { Outlet } from 'react-router-dom';
import { Home, Users, List, Map, Shield, BarChart3, Store, KeyRound } from 'lucide-react';
import { AppShell } from '../../components/layout/AppShell';
import type { NavItem } from '../../components/layout/Sidebar';

const navItems: NavItem[] = [
  { path: '/admin/dashboard', label: 'Dashboard', icon: Home },
  { path: '/admin/users', label: 'Users', icon: Users },
  { path: '/admin/agents', label: 'Agents', icon: Store },
  { path: '/admin/transactions', label: 'Transactions', icon: List },
  { path: '/admin/routes', label: 'Routes', icon: Map },
  { path: '/admin/integrations', label: 'Integrations', icon: KeyRound },
  { path: '/admin/audit', label: 'Audit', icon: Shield },
  { path: '/admin/reconciliation', label: 'Reconciliation', icon: BarChart3 },
];

export default function AdminLayout(): JSX.Element {
  return (
    <AppShell navItems={navItems} portalName="Admin" portalColorClass="bg-danger-500">
      <Outlet />
    </AppShell>
  );
}
