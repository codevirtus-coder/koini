import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { topupSchema } from '../../utils/validators';
import type { TopUpRequest } from '../../api/types';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { useTopUpPassenger } from '../../hooks/useAgent';
import { notify } from '../../utils/notify';

interface TopUpFormProps {
  onSuccess?: () => void;
}

export function TopUpForm({ onSuccess }: TopUpFormProps): JSX.Element {
  const { mutateAsync, isPending } = useTopUpPassenger();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<TopUpRequest>({ resolver: zodResolver(topupSchema) });

  const onSubmit = async (values: TopUpRequest) => {
    await mutateAsync(values);
    notify.success('Wallet topped up successfully');
    onSuccess?.();
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input label="Client Phone" placeholder="+263 77 123 4567" error={errors.holderPhone?.message} {...register('holderPhone')} />
      <Input label="Amount" type="number" error={errors.amountKc?.message} {...register('amountKc', { valueAsNumber: true })} />
      <Button type="submit" fullWidth isLoading={isPending}>
        Confirm Top Up
      </Button>
    </form>
  );
}
