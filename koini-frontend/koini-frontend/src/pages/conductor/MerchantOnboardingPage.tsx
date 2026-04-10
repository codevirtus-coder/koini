import React, { useMemo, useState } from 'react';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Building2, FileText, MapPin, User } from 'lucide-react';
import { PageHeader } from '../../components/layout/PageHeader';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { apiClient } from '../../api/client';
import { ENDPOINTS } from '../../api/endpoints';
import { getApiErrorMessage } from '../../utils/apiError';
import { notify } from '../../utils/notify';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';

const onboardingSchema = z.object({
  businessName: z.string().min(2, 'Business name is required').max(150),
  tradingName: z.string().min(2, 'Trading name is required').max(150),
  addressLine1: z.string().min(3, 'Address is required').max(200),
  city: z.string().min(2, 'City is required').max(120),
  country: z.string().min(2, 'Country is required').max(120),
  idNumber: z.string().min(3, 'ID number is required').max(80),
  idDocument: z
    .custom<FileList>()
    .refine((files) => files && files.length === 1, 'ID document is required'),
  proofOfAddress: z
    .custom<FileList>()
    .refine((files) => files && files.length === 1, 'Proof of address is required'),
});

type FormValues = z.infer<typeof onboardingSchema>;

function fileInputClasses(): string {
  return [
    'block w-full rounded-xl border border-surface-border bg-surface-card px-4 py-3 text-sm text-text-primary',
    'file:mr-3 file:rounded-lg file:border-0 file:bg-surface-cardHover file:px-3 file:py-2 file:text-xs file:font-semibold file:text-text-primary',
    'focus:outline-none focus:ring-2 focus:ring-primary-500/40',
  ].join(' ');
}

export default function MerchantOnboardingPage(): JSX.Element {
  const { state } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState(false);

  const isApproved = state.user?.status === 'ACTIVE';
  const isPending = state.user?.status === 'PENDING_VERIFICATION';

  const defaultValues = useMemo<FormValues>(
    () => ({
      businessName: '',
      tradingName: '',
      addressLine1: '',
      city: '',
      country: 'Zimbabwe',
      idNumber: '',
      idDocument: undefined as unknown as FileList,
      proofOfAddress: undefined as unknown as FileList,
    }),
    []
  );

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(onboardingSchema), defaultValues });

  const onSubmit = async (values: FormValues) => {
    setError(null);
    setSubmitted(false);
    try {
      const form = new FormData();
      form.append('businessName', values.businessName);
      form.append('tradingName', values.tradingName);
      form.append('addressLine1', values.addressLine1);
      form.append('city', values.city);
      form.append('country', values.country);
      form.append('idNumber', values.idNumber);
      form.append('idDocument', values.idDocument[0]);
      form.append('proofOfAddress', values.proofOfAddress[0]);

      await apiClient.post(import.meta.env.VITE_MERCHANT_ONBOARDING_PATH || ENDPOINTS.conductor.onboarding, form);
      setSubmitted(true);
      notify.success('Documents submitted. Awaiting admin approval.');
    } catch (e) {
      const msg = getApiErrorMessage(e);
      setError(msg);
      notify.error(msg);
    }
  };

  if (isApproved) {
    return (
      <div className="space-y-6">
        <PageHeader title="Merchant Verification" subtitle="Your merchant account is already approved." />
        <Alert variant="success" title="Approved" description="You have full access to merchant features." />
        <Button onClick={() => navigate('/merchant/dashboard')}>Go to Dashboard</Button>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader
        title="Merchant Verification"
        subtitle="Submit your documents. You can browse the dashboard, but actions stay locked until approval."
      />

      {isPending && (
        <Alert
          variant="warning"
          title="Pending approval"
          description="Your account is pending verification. An admin must approve your documents before you can redeem or request payments."
        />
      )}

      {submitted && (
        <Alert
          variant="success"
          title="Submitted"
          description="Thanks — your documents were submitted. We’ll notify you once an admin approves your account."
        />
      )}

      {error && <Alert variant="danger" title="Could not submit" description={error} />}

      <form onSubmit={handleSubmit(onSubmit)} className="bg-surface-card border border-surface-border rounded-2xl p-6 space-y-4">
        <div className="grid sm:grid-cols-2 gap-4">
          <Input
            label="Business Name"
            leftIcon={<Building2 className="w-4 h-4" />}
            error={errors.businessName?.message}
            {...register('businessName')}
          />
          <Input
            label="Trading Name"
            leftIcon={<User className="w-4 h-4" />}
            error={errors.tradingName?.message}
            {...register('tradingName')}
          />
        </div>

        <Input
          label="Address"
          leftIcon={<MapPin className="w-4 h-4" />}
          error={errors.addressLine1?.message}
          {...register('addressLine1')}
        />

        <div className="grid sm:grid-cols-2 gap-4">
          <Input label="City" error={errors.city?.message} {...register('city')} />
          <Input label="Country" error={errors.country?.message} {...register('country')} />
        </div>

        <Input
          label="ID Number"
          leftIcon={<FileText className="w-4 h-4" />}
          error={errors.idNumber?.message}
          {...register('idNumber')}
        />

        <div className="space-y-2">
          <div className="text-xs uppercase tracking-wider text-text-muted">ID Document</div>
          <input
            type="file"
            className={fileInputClasses()}
            accept="image/*,.pdf"
            {...register('idDocument')}
          />
          {errors.idDocument?.message && <div className="text-xs text-danger-500">{errors.idDocument.message as string}</div>}
        </div>

        <div className="space-y-2">
          <div className="text-xs uppercase tracking-wider text-text-muted">Proof of Address</div>
          <input
            type="file"
            className={fileInputClasses()}
            accept="image/*,.pdf"
            {...register('proofOfAddress')}
          />
          {errors.proofOfAddress?.message && (
            <div className="text-xs text-danger-500">{errors.proofOfAddress.message as string}</div>
          )}
        </div>

        <Button type="submit" fullWidth size="lg" isLoading={isSubmitting}>
          Submit for Approval
        </Button>
        <div className="text-xs text-text-muted">
          Supported formats: images or PDF. After submission, wait for an admin to approve your merchant account.
        </div>
      </form>
    </div>
  );
}

