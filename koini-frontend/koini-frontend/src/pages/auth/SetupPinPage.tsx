import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Check } from 'lucide-react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../../components/layout/AuthLayout';
import { PinInput } from '../../components/ui/PinInput';
import { Button } from '../../components/ui/Button';
import { apiClient } from '../../api/client';
import { ENDPOINTS } from '../../api/endpoints';
import { useAuth } from '../../hooks/useAuth';
import { getApiErrorMessage } from '../../utils/apiError';

export default function SetupPinPage(): JSX.Element {
  const { portalPath } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [pin, setPin] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState<string | undefined>(undefined);
  const next = new URLSearchParams(location.search).get('next');

  const handleContinue = async () => {
    if (step === 1) {
      if (pin.length < 4) {
        setError('PIN must be 4 digits');
        return;
      }
      setError(undefined);
      setStep(2);
      return;
    }
    if (step === 2) {
      if (pin !== confirm) {
        setError('PINs do not match');
        return;
      }
      try {
        setError(undefined);
        await apiClient.post(ENDPOINTS.auth.setupPin, { pin, confirmPin: confirm });
        setStep(3);
      } catch (e) {
        setError(getApiErrorMessage(e));
      }
    }
  };

  return (
    <AuthLayout>
      <div className="flex flex-col items-center text-center gap-4">
        <div className="flex gap-2">
          {[1, 2, 3].map((i) => (
            <div key={i} className={`w-3 h-3 rounded-full ${i <= step ? 'bg-primary-500' : 'bg-surface-borderMd'}`} />
          ))}
        </div>
        {step === 1 && (
          <>
            <h2 className="text-xl font-bold text-text-primary">Create your 4-digit PIN</h2>
            <PinInput value={pin} onChange={setPin} error={error} />
            <Button onClick={handleContinue} fullWidth>Continue</Button>
          </>
        )}
        {step === 2 && (
          <>
            <h2 className="text-xl font-bold text-text-primary">Confirm your PIN</h2>
            <PinInput value={confirm} onChange={setConfirm} error={error} />
            <Button onClick={handleContinue} fullWidth>Confirm</Button>
          </>
        )}
        {step === 3 && (
          <>
            <motion.div initial={{ scale: 0 }} animate={{ scale: 1 }} className="w-16 h-16 rounded-full border-2 border-success-500 flex items-center justify-center text-success-500">
              <Check className="w-8 h-8" />
            </motion.div>
            <h2 className="text-xl font-bold text-text-primary">Your wallet is ready!</h2>
            <Button onClick={() => navigate(next && next.startsWith('/') ? next : portalPath)} fullWidth>
              Go to Dashboard
            </Button>
          </>
        )}
      </div>
    </AuthLayout>
  );
}
