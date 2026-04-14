import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../../../components/layout/PageHeader';
import { DataTable } from '../../../components/data/DataTable';
import { Badge } from '../../../components/ui/Badge';
import { Button } from '../../../components/ui/Button';
import { usePendingMerchants } from '../../../hooks/useAdmin';
import type { UserSummary } from '../../../api/types';
import { UserCheck } from 'lucide-react';

export default function PendingMerchantsPage(): JSX.Element {
  const navigate = useNavigate();
  const { data, isLoading } = usePendingMerchants();

  const rows = useMemo(() => (data ?? []).filter((u) => u.status === 'PENDING_VERIFICATION'), [data]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Merchant Approvals"
        subtitle="Review merchant verification submissions and approve or reject."
      />
      <DataTable<UserSummary>
        data={rows}
        isLoading={isLoading}
        rowKey="userId"
        emptyMessage="No pending merchants"
        emptyIcon={<UserCheck className="w-6 h-6" />}
        columns={[
          { key: 'fullName', header: 'Name', render: (v) => (v ? String(v) : '—') },
          { key: 'maskedPhone', header: 'Phone' },
          { key: 'role', header: 'Role', render: (v) => <Badge variant="info">{String(v)}</Badge> },
          { key: 'status', header: 'Status', render: (v) => <Badge variant="warning">{String(v)}</Badge> },
          {
            key: 'userId',
            header: '',
            align: 'right',
            render: (_v, row) => (
              <Button size="sm" variant="secondary" onClick={() => navigate(`/admin/users/${row.userId}`)}>
                Review
              </Button>
            ),
          },
        ]}
        onRowClick={(row) => navigate(`/admin/users/${row.userId}`)}
      />
    </div>
  );
}

