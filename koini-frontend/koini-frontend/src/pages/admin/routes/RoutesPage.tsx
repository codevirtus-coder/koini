import React from 'react';
import { useAdminRoutes } from '../../../hooks/useRoutes';
import { DataTable } from '../../../components/data/DataTable';
import { Badge } from '../../../components/ui/Badge';

export default function RoutesPage(): JSX.Element {
  const { data, isLoading } = useAdminRoutes();
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Routes</h1>
      <DataTable
        data={data ?? []}
        isLoading={isLoading}
        rowKey="routeId"
        columns={[
          { key: 'name', header: 'Route Name' },
          { key: 'origin', header: 'Origin' },
          { key: 'destination', header: 'Destination' },
          { key: 'fareKc', header: 'Fare' },
          { key: 'isActive', header: 'Status', render: (v) => <Badge variant={v ? 'success' : 'warning'}>{v ? 'Active' : 'Inactive'}</Badge> },
        ]}
      />
    </div>
  );
}
