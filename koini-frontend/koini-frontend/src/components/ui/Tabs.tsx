import React from 'react';
import { cn } from '../../design/cn';

interface TabItem {
  id: string;
  label: string;
}

interface TabsProps {
  tabs: TabItem[];
  activeId: string;
  onChange: (id: string) => void;
}

export function Tabs({ tabs, activeId, onChange }: TabsProps): JSX.Element {
  return (
    <div className="flex gap-2 border-b border-surface-border">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          type="button"
          onClick={() => onChange(tab.id)}
          className={cn(
            'px-3 py-2 text-sm font-medium',
            activeId === tab.id ? 'text-primary-400 border-b-2 border-primary-500' : 'text-text-secondary'
          )}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}
