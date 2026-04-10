import React from 'react';
import { Sidebar, type NavItem } from './Sidebar';
import { Topbar } from './Topbar';
import { MobileNav } from './MobileNav';

interface AppShellProps {
  children: React.ReactNode;
  navItems: NavItem[];
  portalName: string;
  portalColorClass: string;
}

export function AppShell({ children, navItems, portalName, portalColorClass }: AppShellProps): JSX.Element {
  return (
    <div className="min-h-screen bg-surface-bg text-text-primary flex">
      <Sidebar navItems={navItems} portalName={portalName} portalColorClass={portalColorClass} />
      <div className="flex-1 px-5 pb-20 lg:pb-8">
        <Topbar portalName={portalName} />
        <div className="pt-6">{children}</div>
      </div>
      <MobileNav navItems={navItems} />
    </div>
  );
}
