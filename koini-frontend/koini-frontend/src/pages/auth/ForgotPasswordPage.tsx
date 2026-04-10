import React from 'react';
import { AuthLayout } from '../../components/layout/AuthLayout';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export default function ForgotPasswordPage(): JSX.Element {
  return (
    <AuthLayout>
      <h1 className="text-2xl font-bold text-text-primary text-center">Reset Password</h1>
      <p className="text-sm text-text-secondary text-center mt-2">Enter your phone and we will send reset instructions.</p>
      <form className="mt-6 space-y-4">
        <Input label="Phone" placeholder="+263 77 123 4567" />
        <Button type="submit" fullWidth>Send Reset Link</Button>
      </form>
    </AuthLayout>
  );
}
