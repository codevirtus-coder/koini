import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { transferSchema } from '../../utils/validators';
import type { TransferRequest } from '../../api/types';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { apiClient } from '../../api/client';
import { ENDPOINTS } from '../../api/endpoints';
import { notify } from '../../utils/notify';
import { getApiErrorCode, getApiErrorMessage, getApiErrorStatus } from '../../utils/apiError';

interface TransferFormProps {
  onSuccess?: () => void;
}

export function TransferForm({ onSuccess }: TransferFormProps): JSX.Element {
  const navigate = useNavigate();
  const location = useLocation();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<TransferRequest>({ resolver: zodResolver(transferSchema) });

  const onSubmit = async (values: TransferRequest) => {
    try {
      await apiClient.post(ENDPOINTS.client.transfer, values);
      notify.success('Transfer sent');
      onSuccess?.();
    } catch (e) {
      const code = getApiErrorCode(e);
      const status = getApiErrorStatus(e);
      if (code === 'AUTH_006' || status === 428) {
        notify.info('Please set your PIN to continue.');
        navigate(`/setup-pin?next=${encodeURIComponent(location.pathname)}`);
        return;
      }
      notify.error(getApiErrorMessage(e));
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input label="Recipient Phone" placeholder="+263 77 123 4567" error={errors.toPhone?.message} {...register('toPhone')} />
      <Input label="Amount" type="number" error={errors.amountKc?.message} {...register('amountKc', { valueAsNumber: true })} />
      <Input label="PIN" type="password" error={errors.pin?.message} {...register('pin')} />
      <Button type="submit" fullWidth isLoading={isSubmitting}>
        Send Credits
      </Button>
    </form>
  );
}
