import React from 'react';
import { useForm } from 'react-hook-form';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { useInitiateWithdrawal } from '../../hooks/useAgent';
import { notify } from '../../utils/notify';

interface WithdrawalFormValues {
  passengerPhone: string;
  amountKc: number;
}

interface WithdrawalFormProps {
  onSuccess?: (withdrawalId: string) => void;
}

export function WithdrawalForm({ onSuccess }: WithdrawalFormProps): JSX.Element {
  const { mutateAsync, isPending } = useInitiateWithdrawal();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<WithdrawalFormValues>();

  const onSubmit = async (values: WithdrawalFormValues) => {
    const res = await mutateAsync(values);
    notify.success('Withdrawal initiated');
    onSuccess?.(res.withdrawalId);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input label="Client Phone" error={errors.passengerPhone?.message} {...register('passengerPhone', { required: 'Phone is required' })} />
      <Input label="Amount" type="number" error={errors.amountKc?.message} {...register('amountKc', { valueAsNumber: true, required: 'Amount is required' })} />
      <Button type="submit" fullWidth isLoading={isPending}>
        Initiate Withdrawal
      </Button>
    </form>
  );
}
