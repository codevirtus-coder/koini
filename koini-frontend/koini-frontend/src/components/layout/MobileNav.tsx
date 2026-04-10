import React from 'react';
import { NavLink } from 'react-router-dom';
import { cn } from '../../design/cn';
import type { NavItem } from './Sidebar';

interface MobileNavProps {
  navItems: NavItem[];
}

export function MobileNav({ navItems }: MobileNavProps): JSX.Element {
  return (
    <nav className="lg:hidden fixed bottom-0 left-0 right-0 bg-surface-card border-t border-surface-border px-4 py-2 flex justify-around">
      {navItems.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              cn(
                'flex flex-col items-center gap-1 text-xs px-3 py-1 rounded-lg',
                isActive ? 'bg-primary-500 text-white' : 'text-text-secondary'
              )
            }
          >
            <Icon className="w-5 h-5" />
            <span>{item.label}</span>
          </NavLink>
        );
      })}
    </nav>
  );
}
