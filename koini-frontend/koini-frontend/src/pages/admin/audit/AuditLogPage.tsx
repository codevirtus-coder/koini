import React from 'react';
import { useAdminAuditLogs } from '../../../hooks/useAdmin';
import { DataTable } from '../../../components/data/DataTable';

export default function AuditLogPage(): JSX.Element {
  const { data, isLoading } = useAdminAuditLogs();
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Audit Logs</h1>
      <DataTable
        data={data?.content ?? []}
        isLoading={isLoading}
        rowKey="logId"
        columns={[
          { key: 'createdAt', header: 'Timestamp' },
          { key: 'actorType', header: 'Actor' },
          { key: 'action', header: 'Action' },
          { key: 'entityType', header: 'Entity' },
          { key: 'ipAddress', header: 'IP' },
          { key: 'outcome', header: 'Outcome' },
        ]}
      />
    </div>
  );
}
