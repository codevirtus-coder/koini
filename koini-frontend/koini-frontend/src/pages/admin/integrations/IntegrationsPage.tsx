import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { PageHeader } from '../../../components/layout/PageHeader';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { Input } from '../../../components/ui/Input';
import { useAdminPesepayIntegrationStatus, useAdminSavePesepayKeys } from '../../../hooks/useAdmin';
import { notify } from '../../../utils/notify';
import { getApiErrorMessage } from '../../../utils/apiError';
import { pesepayKeysSchema } from '../../../utils/validators';
import type { AdminPesepayKeysRequest } from '../../../api/types';

export default function IntegrationsPage(): JSX.Element {
  const { data: status, isLoading, refetch } = useAdminPesepayIntegrationStatus();
  const saveKeys = useAdminSavePesepayKeys();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AdminPesepayKeysRequest>({ resolver: zodResolver(pesepayKeysSchema) });

  const onSubmit = async (values: AdminPesepayKeysRequest) => {
    try {
      await saveKeys.mutateAsync(values);
      notify.success('Pesepay keys saved');
      reset();
      await refetch();
    } catch (e) {
      notify.error(getApiErrorMessage(e));
    }
  };

  const configured = Boolean(status?.configured);

  return (
    <div className="space-y-6">
      <PageHeader title="Integrations" subtitle="Configure payment providers for this environment." />

      <div className="bg-surface-card border border-surface-border rounded-2xl p-6 space-y-4">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-lg font-semibold text-text-primary">Pesepay</h2>
            <p className="text-sm text-text-secondary mt-1">
              Top ups will only work when keys are configured on the server.
            </p>
          </div>
          <div className="text-sm text-text-secondary">
            {isLoading ? 'Checking…' : configured ? 'Configured' : 'Not configured'}
          </div>
        </div>

        <Alert
          variant={configured ? 'success' : 'warning'}
          title={configured ? 'Pesepay is configured' : 'Pesepay is not configured'}
          description={
            configured
              ? 'Client top ups can be initiated.'
              : 'Save keys below, and ensure server env vars are set (master key + return/result URLs).'
          }
        />

        {!configured && (
          <Alert
            variant="info"
            title="Required server env vars"
            description="Set KOINI_SECRETS_MASTER_KEY_BASE64, KOINI_PESEPAY_RETURN_URL, and KOINI_PESEPAY_RESULT_URL on the API and restart it."
          />
        )}

        {configured && (
          <div className="grid sm:grid-cols-2 gap-3 text-sm text-text-secondary">
            <div>
              <div className="text-xs uppercase tracking-wider opacity-80">Integration Key</div>
              <div className="text-text-primary font-medium break-all">{status?.integrationKeyMasked ?? '—'}</div>
            </div>
            <div>
              <div className="text-xs uppercase tracking-wider opacity-80">Encryption Key</div>
              <div className="text-text-primary font-medium break-all">{status?.encryptionKeyMasked ?? '—'}</div>
            </div>
          </div>
        )}

        <div className="pt-2 border-t border-surface-border space-y-4">
          <h3 className="text-sm font-semibold text-text-primary">Save keys</h3>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-2xl">
            <Input
              label="Integration Key"
              placeholder="a111f17c-..."
              error={errors.integrationKey?.message}
              {...register('integrationKey')}
            />
            <Input
              label="Encryption Key"
              placeholder="09c4d8c3ce..."
              error={errors.encryptionKey?.message}
              {...register('encryptionKey')}
            />
            <div className="flex flex-col sm:flex-row gap-2">
              <Button type="submit" isLoading={saveKeys.isPending}>
                Save Pesepay Keys
              </Button>
              <Button type="button" variant="outline" onClick={() => refetch()}>
                Refresh Status
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
