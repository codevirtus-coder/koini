import React, { useMemo, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Phone, Lock, Eye, EyeOff } from 'lucide-react';
import { AuthLayout } from '../../components/layout/AuthLayout';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Alert } from '../../components/ui/Alert';
import { loginSchema } from '../../utils/validators';
import type { LoginRequest } from '../../api/types';
import { useAuth } from '../../hooks/useAuth';

function mapAuthError(code?: string): string {
  switch (code) {
    case 'AUTH_001':
      return 'Incorrect phone or password';
    case 'AUTH_004':
      return 'Account locked. Try again in 30 minutes.';
    case 'AUTH_005':
      return 'Account suspended. Contact support.';
    default:
      return 'Unable to sign in. Please try again.';
  }
}

function getErrorCode(err: unknown): string | undefined {
  const e = err as { response?: { data?: { errorCode?: string } } };
  return e?.response?.data?.errorCode;
}

export default function LoginPage(): JSX.Element {
  const { state, login, portalPath } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const location = useLocation();
  const navigate = useNavigate();

  const from = useMemo(() => (location.state as { from?: Location })?.from?.pathname, [location.state]);
  const next = useMemo(() => {
    const raw = new URLSearchParams(location.search).get('next');
    if (!raw) return null;
    if (!raw.startsWith('/') || raw.startsWith('//')) return null;
    return raw;
  }, [location.search]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginRequest>({ resolver: zodResolver(loginSchema) });

  if (state.isAuthenticated) {
    return <Navigate to={next || from || portalPath} replace />;
  }

  const onSubmit = async (values: LoginRequest) => {
    setError(null);
    try {
      await login(values);
      navigate(next || from || portalPath, { replace: true });
    } catch (err: unknown) {
      const code = getErrorCode(err);
      setError(mapAuthError(code));
    }
  };

  return (
    <AuthLayout>
      <h1 className="text-2xl font-bold text-text-primary text-center">Welcome back</h1>
      <p className="text-sm text-text-secondary text-center mt-2">Sign in to your KOINI wallet</p>
      {error && <div className="mt-4"><Alert variant="danger" description={error} /></div>}
      <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
        <Input
          label="Phone or username"
          placeholder="Phone number or admin"
          leftIcon={<Phone className="w-4 h-4" />}
          error={errors.phone?.message}
          {...register('phone')}
        />
        <Input
          label="Password"
          type={showPassword ? 'text' : 'password'}
          leftIcon={<Lock className="w-4 h-4" />}
          rightElement={
            <button type="button" onClick={() => setShowPassword((v) => !v)} aria-label="Toggle password">
              {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          }
          error={errors.password?.message}
          {...register('password')}
        />
        <Button type="submit" fullWidth size="xl" isLoading={isSubmitting}>
          {isSubmitting ? 'Signing in...' : 'Sign In'}
        </Button>
      </form>
      <div className="mt-4 text-sm text-text-secondary text-center">
        <button
          type="button"
          onClick={() => {
            const params = new URLSearchParams();
            const inferredRole = next?.startsWith('/merchant') || next?.startsWith('/conductor') ? 'merchant' : 'client';
            params.set('role', inferredRole);
            if (next) params.set('next', next);
            navigate(`/register?${params.toString()}`);
          }}
          className="text-primary-400"
        >
          Don't have an account? Register
        </button>
      </div>
      <div className="mt-2 text-xs text-text-muted text-center">
        <button type="button" onClick={() => navigate('/forgot-password')} className="text-text-secondary">Forgot password?</button>
      </div>
    </AuthLayout>
  );
}
