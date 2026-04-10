import React, { useState } from 'react';
import { useAdminUsers } from '../../../hooks/useAdmin';
import { FilterBar } from '../../../components/data/FilterBar';
import { Input } from '../../../components/ui/Input';
import { Select } from '../../../components/ui/Select';
import { DataTable } from '../../../components/data/DataTable';
import { Badge } from '../../../components/ui/Badge';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../../../components/layout/PageHeader';
import { Button } from '../../../components/ui/Button';
import { Modal } from '../../../components/ui/Modal';
import { UserCreateForm } from '../../../components/forms/UserCreateForm';

export default function UsersPage(): JSX.Element {
  const [search, setSearch] = useState('');
  const [role, setRole] = useState('');
  const [status, setStatus] = useState('');
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  const { data, isLoading } = useAdminUsers({ search, role, status });
  const users = data?.content ?? [];

  return (
    <div className="space-y-6">
      <PageHeader title="Users" action={<Button onClick={() => setOpen(true)}>Add User</Button>} />
      <FilterBar>
        <Input label="Search" fullWidth={false} value={search} onChange={(e) => setSearch(e.target.value)} />
        <Select
          label="Role"
          fullWidth={false}
          value={role}
          onChange={(e) => setRole(e.target.value)}
          options={[
            { label: 'All', value: '' },
            { label: 'Client', value: 'PASSENGER' },
            { label: 'Merchant', value: 'CONDUCTOR' },
            { label: 'Agent', value: 'AGENT' },
          ]}
        />
        <Select
          label="Status"
          fullWidth={false}
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          options={[
            { label: 'All', value: '' },
            { label: 'Active', value: 'ACTIVE' },
            { label: 'Pending Verification', value: 'PENDING_VERIFICATION' },
            { label: 'Suspended', value: 'SUSPENDED' },
            { label: 'Locked', value: 'LOCKED' },
          ]}
        />
      </FilterBar>
      <DataTable
        data={users}
        rowKey="userId"
        isLoading={isLoading}
        columns={[
          { key: 'fullName', header: 'Name' },
          { key: 'maskedPhone', header: 'Phone' },
          { key: 'role', header: 'Role', render: (v) => <Badge variant="info">{v}</Badge> },
          {
            key: 'status',
            header: 'Status',
            render: (v) => (
              <Badge
                variant={
                  v === 'ACTIVE'
                    ? 'success'
                    : v === 'PENDING_VERIFICATION'
                    ? 'warning'
                    : v === 'SUSPENDED' || v === 'LOCKED'
                    ? 'danger'
                    : 'warning'
                }
              >
                {v}
              </Badge>
            ),
          },
          { key: 'kycLevel', header: 'KYC' },
          { key: 'lastLogin', header: 'Last Login' },
        ]}
        onRowClick={(row) => navigate(`/admin/users/${row.userId}`)}
      />
      <Modal isOpen={open} onClose={() => setOpen(false)} title="Create User">
        <UserCreateForm onSuccess={() => setOpen(false)} />
      </Modal>
    </div>
  );
}
