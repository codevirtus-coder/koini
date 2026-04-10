import React from 'react';
import { useAgentDailySummary } from '../../hooks/useAgent';

export default function DailySummaryPage(): JSX.Element {
  const { data } = useAgentDailySummary();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Daily Summary</h1>
      <pre className="bg-surface-card border border-surface-border rounded-xl p-4 text-xs text-text-secondary overflow-auto">
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
}
