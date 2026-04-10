import React, { useState } from 'react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Select } from '../ui/Select';
import { useCreateAgent, useCreateConductor } from '../../hooks/useAdmin';
import { notify } from '../../utils/notify';

interface UserCreateFormProps {
  onSuccess?: () => void;
}

export function UserCreateForm({ onSuccess }: UserCreateFormProps): JSX.Element {
  const [role, setRole] = useState<'CONDUCTOR' | 'AGENT'>('CONDUCTOR');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const createConductor = useCreateConductor();
  const createAgent = useCreateAgent();

  const isPending = createConductor.isPending || createAgent.isPending;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phone.trim()) {
      notify.error('Phone is required');
      return;
    }
    const payload = { fullName, phone };
    if (role === 'CONDUCTOR') await createConductor.mutateAsync(payload);
    else await createAgent.mutateAsync(payload);
    notify.success('User created');
    onSuccess?.();
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Select
        label="Role"
        value={role}
        onChange={(e) => setRole(e.target.value as 'CONDUCTOR' | 'AGENT')}
        options={[
          { label: 'Merchant', value: 'CONDUCTOR' },
          { label: 'Agent', value: 'AGENT' },
        ]}
      />
      <Input label="Full Name" value={fullName} onChange={(e) => setFullName(e.target.value)} />
      <Input label="Phone" value={phone} onChange={(e) => setPhone(e.target.value)} />
      <Button type="submit" fullWidth isLoading={isPending}>
        Create User
      </Button>
    </form>
  );
}
