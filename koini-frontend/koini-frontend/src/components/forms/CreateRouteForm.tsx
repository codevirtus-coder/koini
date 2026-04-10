import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createRouteSchema } from '../../utils/validators';
import type { CreateRouteRequest } from '../../api/types';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { useCreateRoute } from '../../hooks/useRoutes';
import { notify } from '../../utils/notify';

interface CreateRouteFormProps {
  onSuccess?: () => void;
}

export function CreateRouteForm({ onSuccess }: CreateRouteFormProps): JSX.Element {
  const { mutateAsync, isPending } = useCreateRoute();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateRouteRequest>({ resolver: zodResolver(createRouteSchema) });

  const onSubmit = async (values: CreateRouteRequest) => {
    await mutateAsync(values);
    notify.success('Route created');
    onSuccess?.();
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input label="Route Name" error={errors.name?.message} {...register('name')} />
      <Input label="Origin" error={errors.origin?.message} {...register('origin')} />
      <Input label="Destination" error={errors.destination?.message} {...register('destination')} />
      <Input label="Fare" type="number" error={errors.fareKc?.message} {...register('fareKc', { valueAsNumber: true })} />
      <Button type="submit" fullWidth isLoading={isPending}>
        Create Route
      </Button>
    </form>
  );
}
