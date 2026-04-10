import React from 'react';
import { useAdminAgents } from '../../../hooks/useAdmin';
import { DataTable } from '../../../components/data/DataTable';

export default function AgentsPage(): JSX.Element {
  const { data, isLoading } = useAdminAgents();
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Agents</h1>
      <DataTable
        data={data ?? []}
        isLoading={isLoading}
        rowKey="userId"
        columns={[
          { key: 'fullName', header: 'Name' },
          { key: 'maskedPhone', header: 'Phone' },
          { key: 'status', header: 'Status' },
        ]}
      />
    </div>
  );
}
