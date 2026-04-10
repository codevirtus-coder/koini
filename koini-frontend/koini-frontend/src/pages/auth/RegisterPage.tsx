import React, { useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Lock, Phone, User } from 'lucide-react';
import { AuthLayout } from '../../components/layout/AuthLayout';
import { Alert } from '../../components/ui/Alert';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Tabs } from '../../components/ui/Tabs';
import { phoneSchema, passwordSchema } from '../../utils/validators';
import { apiClient } from '../../api/client';
import { ENDPOINTS } from '../../api/endpoints';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { getApiErrorMessage } from '../../utils/apiError';

const registerWithConfirmSchema = z
  .object({
    phone: phoneSchema,
    password: passwordSchema,
    fullName: z.string().max(150).optional(),
    confirmPassword: z.string().min(1, 'Confirm your password'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Passwords do not match',
  });

type RegisterForm = z.infer<typeof registerWithConfirmSchema>;

type AccountKind = 'client' | 'merchant';

function strengthScore(password: string): number {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/[0-9]/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  return score;
}

export default function RegisterPage(): JSX.Element {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const score = useMemo(() => strengthScore(password), [password]);
  const allowMerchantSelfRegister = import.meta.env.VITE_ENABLE_MERCHANT_SELF_REGISTER === 'true';
  const merchantRegisterPathFromEnv = import.meta.env.VITE_MERCHANT_REGISTER_PATH as string | undefined;
  const merchantRegisterSendRole = import.meta.env.VITE_MERCHANT_REGISTER_SEND_ROLE === 'true';
  const merchantRegisterRole = (import.meta.env.VITE_MERCHANT_REGISTER_ROLE as string | undefined) || 'CONDUCTOR';

  const next = useMemo(() => {
    const raw = new URLSearchParams(location.search).get('next');
    if (!raw) return null;
    if (!raw.startsWith('/') || raw.startsWith('//')) return null;
    return raw;
  }, [location.search]);

  const initialAccountKind = useMemo<AccountKind>(() => {
    const rawRole = searchParams.get('role')?.toLowerCase();
    if (rawRole === 'merchant') return 'merchant';
    if (rawRole === 'client') return 'client';

    if (next?.startsWith('/merchant') || next?.startsWith('/conductor')) return 'merchant';
    return 'client';
  }, [next, searchParams]);

  const [accountKind, setAccountKind] = useState<AccountKind>(initialAccountKind);

  const nextForSetupPin = useMemo(() => {
    if (!next) return null;
    if (accountKind === 'client' && (next.startsWith('/client') || next.startsWith('/passenger'))) return next;
    if (accountKind === 'merchant' && (next.startsWith('/merchant') || next.startsWith('/conductor'))) return next;
    return null;
  }, [accountKind, next]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterForm>({ resolver: zodResolver(registerWithConfirmSchema) });

  const onSubmit = async (values: RegisterForm) => {
    setError(null);
    if (accountKind === 'merchant' && !allowMerchantSelfRegister) {
      setError('Merchant accounts are currently created by an admin. Switch to Client to create a regular account.');
      return;
    }

    try {
      const isMerchant = accountKind === 'merchant';
      const registerPath =
        isMerchant
          ? merchantRegisterPathFromEnv || '/auth/register/merchant'
          : ENDPOINTS.auth.register;

      const payload = {
        phone: values.phone,
        password: values.password,
        fullName: values.fullName,
        ...(isMerchant && merchantRegisterSendRole ? { role: merchantRegisterRole } : {}),
      };

      await apiClient.post(registerPath, payload);
      await login({ phone: values.phone, password: values.password });

      const defaultAfterPin = accountKind === 'merchant' ? '/merchant/onboarding' : null;
      const nextAfterPin = nextForSetupPin ?? defaultAfterPin;
      const qs = nextAfterPin ? `?next=${encodeURIComponent(nextAfterPin)}` : '';
      navigate(`/setup-pin${qs}`);
    } catch (e) {
      setError(getApiErrorMessage(e));
    }
  };

  const barWidth = `${(score / 4) * 100}%`;
  const barColor = score <= 1 ? 'bg-danger-500' : score === 2 ? 'bg-warning-500' : score === 3 ? 'bg-accent-500' : 'bg-success-500';

  return (
    <AuthLayout>
      <h1 className="text-2xl font-bold text-text-primary text-center">Create your account</h1>
      <p className="text-sm text-text-secondary text-center mt-2">Join KOINI and pay exact fare</p>

      <div className="mt-6">
        <Tabs
          tabs={[
            { id: 'client', label: 'Client' },
            { id: 'merchant', label: 'Merchant' },
          ]}
          activeId={accountKind}
          onChange={(id) => setAccountKind(id as AccountKind)}
        />
      </div>

      {accountKind === 'merchant' && !allowMerchantSelfRegister && (
        <div className="mt-4">
          <Alert
            variant="warning"
            title="Merchant registration"
            description="Merchant accounts are created by an admin for now. If you already have a merchant account, sign in instead."
          />
        </div>
      )}

      {error && (
        <div className="mt-4">
          <Alert variant="danger" description={error} />
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
        <Input label="Full Name (optional)" leftIcon={<User className="w-4 h-4" />} error={errors.fullName?.message} {...register('fullName')} />
        <Input label="Phone" leftIcon={<Phone className="w-4 h-4" />} error={errors.phone?.message} {...register('phone')} />
        <Input
          label="Password"
          type="password"
          leftIcon={<Lock className="w-4 h-4" />}
          error={errors.password?.message}
          {...register('password', { onChange: (e) => setPassword(e.target.value) })}
        />
        <div className="h-2 bg-surface-borderMd rounded-full overflow-hidden">
          <div className={`h-full ${barColor}`} style={{ width: barWidth }} />
        </div>
        <div className="text-xs text-text-secondary">Use 8+ characters, an uppercase letter, and a number.</div>
        <Input label="Confirm Password" type="password" error={errors.confirmPassword?.message} {...register('confirmPassword')} />
        <Button type="submit" fullWidth isLoading={isSubmitting} disabled={accountKind === 'merchant' && !allowMerchantSelfRegister}>
          Register
        </Button>
      </form>

      <div className="mt-4 text-sm text-text-secondary text-center">
        <button
          type="button"
          onClick={() => {
            const params = new URLSearchParams();
            const loginNext = nextForSetupPin ?? (accountKind === 'merchant' ? '/merchant/onboarding' : null);
            if (loginNext) params.set('next', loginNext);
            const qs = params.toString();
            navigate(qs ? `/login?${qs}` : '/login');
          }}
          className="text-primary-400"
        >
          Already have an account? Sign in
        </button>
      </div>
    </AuthLayout>
  );
}
