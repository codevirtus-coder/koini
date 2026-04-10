import React, { useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useAdminMerchantKycApplication, useAdminUserDetail, useActivateUser, useSuspendUser } from '../../../hooks/useAdmin';
import { apiClient } from '../../../api/client';
import { ENDPOINTS } from '../../../api/endpoints';
import { Avatar } from '../../../components/ui/Avatar';
import { Badge } from '../../../components/ui/Badge';
import { Tabs } from '../../../components/ui/Tabs';
import { DataTable } from '../../../components/data/DataTable';
import { Button } from '../../../components/ui/Button';
import { Alert } from '../../../components/ui/Alert';
import { notify } from '../../../utils/notify';
import { getApiErrorMessage } from '../../../utils/apiError';

export default function UserDetailPage(): JSX.Element {
  const { id } = useParams();
  const [tab, setTab] = useState('wallet');
  const activate = useActivateUser();
  const suspend = useSuspendUser();
  const { data } = useAdminUserDetail(id ?? '');

  const isMerchant = data?.role === 'CONDUCTOR' || data?.role === 'MERCHANT';
  const isPending = data?.status === 'PENDING_VERIFICATION';
  const isActive = data?.status === 'ACTIVE';
  const kycQuery = useAdminMerchantKycApplication(id ?? '', Boolean(id && isMerchant));

  const viewDocument = async (type: 'idDocument' | 'proofOfAddress') => {
    if (!id) return;
    try {
      const res = await apiClient.get(ENDPOINTS.admin.merchantKycDocument(id, type), { responseType: 'blob' });
      const blob = res.data as Blob;
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank', 'noopener,noreferrer');
      window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
    } catch (e) {
      notify.error(getApiErrorMessage(e));
    }
  };

  const tabs = useMemo(() => {
    const base = [
      { id: 'wallet', label: 'Wallet' },
      { id: 'transactions', label: 'Transactions' },
      { id: 'audit', label: 'Audit' },
    ];
    if (isMerchant) base.splice(2, 0, { id: 'verification', label: 'Verification' });
    return base;
  }, [isMerchant]);

  if (!data) {
    return <div className="text-text-secondary">Loading...</div>;
  }

  return (
    <div className="grid md:grid-cols-5 gap-6">
      <div className="md:col-span-2 bg-surface-card border border-surface-border rounded-xl p-5">
        <div className="flex items-center gap-3">
          <Avatar name={data.fullName} size="lg" />
          <div>
            <div className="text-lg text-text-primary">{data.fullName ?? 'User'}</div>
            <div className="text-sm text-text-secondary">{data.maskedPhone}</div>
          </div>
        </div>
        <div className="mt-4 flex gap-2">
          <Badge variant="info">{data.role}</Badge>
          <Badge
            variant={
              data.status === 'ACTIVE'
                ? 'success'
                : data.status === 'PENDING_VERIFICATION'
                ? 'warning'
                : data.status === 'SUSPENDED' || data.status === 'LOCKED'
                ? 'danger'
                : 'warning'
            }
          >
            {data.status}
          </Badge>
        </div>

        {isMerchant && isPending && (
          <div className="mt-4">
            <Alert
              variant="warning"
              title="Merchant pending approval"
              description="This merchant submitted verification and is waiting for admin approval. Approve to unlock merchant actions."
            />
          </div>
        )}

        <div className="mt-4 flex flex-col gap-2">
          {isMerchant && isPending && (
            <Button
              onClick={async () => {
                if (!id) return;
                try {
                  await activate.mutateAsync(id);
                  notify.success('Merchant approved (activated).');
                } catch (e) {
                  notify.error(getApiErrorMessage(e));
                }
              }}
              isLoading={activate.isPending}
            >
              Approve Merchant
            </Button>
          )}

          {!isPending && (
            <Button
              variant={isActive ? 'outline' : 'primary'}
              onClick={async () => {
                if (!id) return;
                try {
                  if (isActive) {
                    await suspend.mutateAsync(id);
                    notify.success('User suspended.');
                  } else {
                    await activate.mutateAsync(id);
                    notify.success('User activated.');
                  }
                } catch (e) {
                  notify.error(getApiErrorMessage(e));
                }
              }}
              isLoading={activate.isPending || suspend.isPending}
            >
              {isActive ? 'Suspend User' : 'Activate User'}
            </Button>
          )}
        </div>

        {data.agentDetail && (
          <div className="mt-4 text-sm text-text-secondary">
            <div>Business: {data.agentDetail.businessName ?? 'N/A'}</div>
            <div>Location: {data.agentDetail.location ?? 'N/A'}</div>
            <div>Float Balance: {data.agentDetail.floatBalanceKc}</div>
          </div>
        )}
      </div>
      <div className="md:col-span-3 bg-surface-card border border-surface-border rounded-xl p-5">
        <Tabs
          tabs={tabs}
          activeId={tab}
          onChange={setTab}
        />
        {tab === 'wallet' && (
          <div className="mt-4">
            <div className="text-sm text-text-secondary">Balance</div>
            <div className="text-2xl font-bold text-text-primary">{data.wallet.balanceUsd}</div>
            <div className="text-sm text-text-secondary">{data.wallet.balanceKc}</div>
          </div>
        )}

        {tab === 'verification' && isMerchant && (
          <div className="mt-4 space-y-4">
            {kycQuery.isLoading ? (
              <div className="text-sm text-text-secondary">Loading verification details...</div>
            ) : kycQuery.isError ? (
              <Alert
                variant="warning"
                title="No verification submission found"
                description="This merchant has not submitted verification documents yet."
              />
            ) : (
              <>
                <div className="grid sm:grid-cols-2 gap-3 text-sm text-text-secondary">
                  <div>
                    <div className="text-xs uppercase tracking-wider opacity-80">Business</div>
                    <div className="text-text-primary font-medium">{kycQuery.data?.businessName ?? '—'}</div>
                  </div>
                  <div>
                    <div className="text-xs uppercase tracking-wider opacity-80">Trading Name</div>
                    <div className="text-text-primary font-medium">{kycQuery.data?.tradingName ?? '—'}</div>
                  </div>
                  <div className="sm:col-span-2">
                    <div className="text-xs uppercase tracking-wider opacity-80">Address</div>
                    <div className="text-text-primary font-medium">
                      {kycQuery.data?.addressLine1 ?? '—'}
                      {kycQuery.data?.city ? `, ${kycQuery.data.city}` : ''}
                      {kycQuery.data?.country ? `, ${kycQuery.data.country}` : ''}
                    </div>
                  </div>
                  <div>
                    <div className="text-xs uppercase tracking-wider opacity-80">ID</div>
                    <div className="text-text-primary font-medium">{kycQuery.data?.idNumberMasked ?? '—'}</div>
                  </div>
                  <div>
                    <div className="text-xs uppercase tracking-wider opacity-80">Submitted</div>
                    <div className="text-text-primary font-medium">{kycQuery.data?.submittedAt ?? '—'}</div>
                  </div>
                </div>

                <div className="flex flex-col sm:flex-row gap-2">
                  <Button variant="outline" onClick={() => viewDocument('idDocument')}>
                    View ID Document
                  </Button>
                  <Button variant="outline" onClick={() => viewDocument('proofOfAddress')}>
                    View Proof of Address
                  </Button>
                </div>

                {isPending && (
                  <Alert
                    variant="warning"
                    title="Approval checklist"
                    description="Review the documents above before approving. Once approved, this merchant can redeem and request payments."
                  />
                )}
              </>
            )}
          </div>
        )}

        {tab === 'transactions' && (
          <div className="mt-4">
            <DataTable
              data={data.recentTransactions}
              rowKey="txId"
              columns={[
                { key: 'reference', header: 'Ref' },
                { key: 'txType', header: 'Type' },
                { key: 'amountKc', header: 'Amount' },
                { key: 'status', header: 'Status' },
              ]}
            />
          </div>
        )}
        {tab === 'audit' && (
          <div className="mt-4 text-sm text-text-secondary">No audit events found.</div>
        )}
      </div>
    </div>
  );
}
