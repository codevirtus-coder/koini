import React, { useMemo, useState } from 'react';
import { PageHeader } from '../../components/layout/PageHeader';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { DataTable } from '../../components/data/DataTable';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Modal } from '../../components/ui/Modal';
import { useCancelMerchantRequest, useMerchantPaymentRequest, useMerchantPaymentRequests } from '../../hooks/useMerchantPortal';
import type { MerchantPaymentRequest } from '../../api/types';
import { notify } from '../../utils/notify';
import { getApiErrorMessage } from '../../utils/apiError';

const statusOptions = [
  { label: 'All', value: '' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Approved', value: 'APPROVED' },
  { label: 'Declined', value: 'DECLINED' },
  { label: 'Expired', value: 'EXPIRED' },
  { label: 'Cancelled', value: 'CANCELLED' },
];

export default function RequestsPage(): JSX.Element {
  const [status, setStatus] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [page, setPage] = useState(0);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);

  const { data, isLoading } = useMerchantPaymentRequests({
    status: status || undefined,
    dateFrom: dateFrom || undefined,
    dateTo: dateTo || undefined,
    page,
    size: 20,
  });

  const cancelRequest = useCancelMerchantRequest();
  const requestDetail = useMerchantPaymentRequest(selectedId);

  const rows = useMemo(() => data?.requests ?? [], [data]);
  const totalPages = data ? Math.ceil(data.total / data.size) : 0;

  return (
    <div className="space-y-6">
      <PageHeader title="Requests" subtitle="Manage payment requests sent to clients." />

      <div className="flex flex-wrap gap-3">
        <Select
          label="Status"
          fullWidth={false}
          value={status}
          onChange={(e) => {
            setStatus(e.target.value);
            setPage(0);
          }}
          options={statusOptions}
        />
        <Input
          label="Date From"
          type="date"
          fullWidth={false}
          value={dateFrom}
          onChange={(e) => {
            setDateFrom(e.target.value);
            setPage(0);
          }}
        />
        <Input
          label="Date To"
          type="date"
          fullWidth={false}
          value={dateTo}
          onChange={(e) => {
            setDateTo(e.target.value);
            setPage(0);
          }}
        />
      </div>

      <DataTable<MerchantPaymentRequest>
        data={rows}
        isLoading={isLoading}
        rowKey="requestId"
        emptyMessage="No requests found"
        columns={[
          { key: 'passengerMaskedPhone', header: 'Client' },
          { key: 'amountUsd', header: 'Amount' },
          { key: 'status', header: 'Status', render: (v) => <Badge variant={v === 'PENDING' ? 'warning' : v === 'APPROVED' ? 'success' : v === 'DECLINED' ? 'danger' : 'default'}>{String(v)}</Badge> },
          { key: 'createdAt', header: 'Created' },
          { key: 'expiresAt', header: 'Expires' },
          {
            key: 'requestId',
            header: '',
            align: 'right',
            render: (_v, row) => (
              <div className="flex items-center justify-end gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setSelectedId(row.requestId);
                    setDetailOpen(true);
                  }}
                >
                  View
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  disabled={row.status !== 'PENDING' || cancelRequest.isPending}
                  onClick={async () => {
                    try {
                      await cancelRequest.mutateAsync(row.requestId);
                      notify.success('Request cancelled.');
                    } catch (e) {
                      notify.error(getApiErrorMessage(e));
                    }
                  }}
                >
                  Cancel
                </Button>
              </div>
            ),
          },
        ]}
        pagination={
          data
            ? {
                page,
                totalPages,
                totalElements: data.total,
                onPageChange: setPage,
                size: data.size,
              }
            : undefined
        }
      />

      <Modal
        isOpen={detailOpen}
        onClose={() => setDetailOpen(false)}
        title="Payment Request"
        description="Full details for this request."
        footer={
          <div className="flex justify-end">
            <Button variant="outline" onClick={() => setDetailOpen(false)}>
              Close
            </Button>
          </div>
        }
      >
        {requestDetail.isPending ? (
          <div className="text-sm text-text-secondary">Loading request...</div>
        ) : requestDetail.data ? (
          <div className="space-y-3 text-sm text-text-secondary">
            <div className="grid sm:grid-cols-2 gap-3">
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Client</div>
                <div className="text-text-primary font-medium">{requestDetail.data.passengerMaskedPhone}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Status</div>
                <div className="text-text-primary font-medium">{requestDetail.data.status}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Amount</div>
                <div className="text-text-primary font-medium">{requestDetail.data.amountUsd} ({requestDetail.data.amountKc} KC)</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Route</div>
                <div className="text-text-primary font-medium">{requestDetail.data.routeId ?? '—'}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Created</div>
                <div className="text-text-primary font-medium">{requestDetail.data.createdAt}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Expires</div>
                <div className="text-text-primary font-medium">{requestDetail.data.expiresAt}</div>
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider opacity-80">Responded</div>
                <div className="text-text-primary font-medium">{requestDetail.data.respondedAt ?? '—'}</div>
              </div>
            </div>
            <div className="text-xs text-text-muted">Request ID: {selectedId}</div>
          </div>
        ) : (
          <div className="text-sm text-text-secondary">No request details available.</div>
        )}
      </Modal>
    </div>
  );
}
