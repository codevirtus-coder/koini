import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { KoiniLogo } from '../../components/shared/KoiniLogo';
import { useAuth } from '../../hooks/useAuth';
import { notify } from '../../utils/notify';

export default function PesepayReturnPage(): JSX.Element {
  const { state } = useAuth();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const txId = searchParams.get('txId');
  const referenceNumber = searchParams.get('referenceNumber');

  useEffect(() => {
    if (!txId) {
      notify.error('Missing txId from Pesepay return.');
      navigate('/client/topup', { replace: true });
      return;
    }

    const nextParams = new URLSearchParams();
    nextParams.set('txId', txId);
    if (referenceNumber) nextParams.set('referenceNumber', referenceNumber);

    const nextPath = `/client/topup?${nextParams.toString()}`;

    if (!state.isAuthenticated) {
      notify.info('Please sign in to confirm your top up.');
      navigate(`/login?next=${encodeURIComponent(nextPath)}`, { replace: true });
      return;
    }

    navigate(nextPath, { replace: true });
  }, [navigate, referenceNumber, state.isAuthenticated, txId]);

  return (
    <div className="min-h-screen bg-surface-bg flex items-center justify-center p-6">
      <div className="flex flex-col items-center gap-4">
        <KoiniLogo size={48} />
        <div className="w-8 h-8 border-2 border-surface-border border-t-primary-500 rounded-full animate-spin" />
        <div className="text-sm text-text-secondary">Returning to Koini...</div>
      </div>
    </div>
  );
}
