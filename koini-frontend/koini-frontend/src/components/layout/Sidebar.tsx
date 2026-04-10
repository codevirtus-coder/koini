import React from 'react';
import { NavLink } from 'react-router-dom';
import { cn } from '../../design/cn';
import { KoiniLogo } from '../shared/KoiniLogo';
import { Avatar } from '../ui/Avatar';
import { Button } from '../ui/Button';
import { useAuth } from '../../hooks/useAuth';

export interface NavItem {
  path: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: number;
}

interface SidebarProps {
  navItems: NavItem[];
  portalName: string;
  portalColorClass: string;
  collapsed?: boolean;
}

export function Sidebar({ navItems, portalName, portalColorClass, collapsed }: SidebarProps): JSX.Element {
  const { state, logout } = useAuth();
  return (
    <aside className="hidden lg:flex lg:flex-col w-64 bg-surface-card border-r border-surface-border min-h-screen">
      <div className={cn('h-2', portalColorClass)} />
      <div className="p-5">
        <KoiniLogo size={28} />
        <div className="text-xs text-text-secondary mt-2">{portalName}</div>
      </div>
      <nav className="flex-1 px-3 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 px-3 py-2 rounded-lg text-sm',
                  isActive
                    ? 'bg-primary-500 text-white border-l-2 border-primary-500'
                    : 'text-text-secondary hover:bg-surface-cardHover'
                )
              }
            >
              <Icon className="w-4 h-4" />
              {!collapsed && <span>{item.label}</span>}
              {item.badge && <span className="ml-auto text-xs bg-accent-500/20 text-accent-400 px-2 rounded-full">{item.badge}</span>}
            </NavLink>
          );
        })}
      </nav>
      <div className="p-4 border-t border-surface-border">
        <div className="flex items-center gap-3">
          <Avatar name={state.user?.fullName ?? state.user?.maskedPhone} size="sm" />
          <div className="flex-1">
            <div className="text-sm text-text-primary truncate">{state.user?.fullName ?? 'User'}</div>
            <div className="text-xs text-text-secondary truncate">{state.user?.maskedPhone}</div>
          </div>
        </div>
        <div className="mt-3">
          <Button variant="ghost" size="sm" fullWidth onClick={logout}>
            Logout
          </Button>
        </div>
      </div>
    </aside>
  );
}
