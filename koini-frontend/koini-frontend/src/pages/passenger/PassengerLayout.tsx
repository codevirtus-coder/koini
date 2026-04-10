import React from 'react';
import { Outlet } from 'react-router-dom';
import { Home, Wallet, Send, User, ArrowUpRight } from 'lucide-react';
import { AppShell } from '../../components/layout/AppShell';
import type { NavItem } from '../../components/layout/Sidebar';
import { FEATURES } from '../../utils/features';

const navItems: NavItem[] = [
  { path: '/client/dashboard', label: 'Home', icon: Home },
  { path: '/client/pay', label: 'Pay Fare', icon: Wallet },
  { path: '/client/topup', label: 'Top Up', icon: ArrowUpRight },
  ...(FEATURES.p2pTransfers ? [{ path: '/client/transfer', label: 'Transfer', icon: Send } satisfies NavItem] : []),
  { path: '/client/profile', label: 'Profile', icon: User },
];

export default function PassengerLayout(): JSX.Element {
  return (
    <AppShell navItems={navItems} portalName="User" portalColorClass="bg-primary-500">
      <Outlet />
    </AppShell>
  );
}
