import React, { useState } from "react";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import { Select } from "../../components/ui/Select";
import { useConductorRoutes } from "../../hooks/useRoutes";
import {
  useCreatePaymentRequest,
  usePaymentRequestStatus,
} from "../../hooks/usePayment";
import { PaymentStatusBanner } from "../../components/payment/PaymentStatusBanner";
import { formatKc, formatUsd } from "../../utils/money";
import { useAuth } from "../../hooks/useAuth";
import { EmptyState } from "../../components/ui/EmptyState";
import { ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function RequestPaymentPage(): JSX.Element {
  const { state: authState } = useAuth();
  const navigate = useNavigate();
  const isPending = authState.user?.status === "PENDING_VERIFICATION";
  const [phone, setPhone] = useState("");
  const [routeId, setRouteId] = useState("");
  const [amount, setAmount] = useState(50);
  const [requestId, setRequestId] = useState<string | null>(null);
  const { data: routes } = useConductorRoutes({ enabled: !isPending });
  const createRequest = useCreatePaymentRequest();

  const statusQuery = usePaymentRequestStatus(
    requestId,
    !!requestId && !isPending,
  );

  const selectedRoute = routes?.find((r) => r.routeId === routeId);

  const handleSend = async () => {
    if (isPending) return;
    const res = await createRequest.mutateAsync({
      passengerPhone: phone,
      amountKc: amount,
      routeId: routeId || undefined,
    });
    setRequestId(res.requestId);
  };

  if (isPending) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-text-primary">
          Request Payment
        </h1>
        <EmptyState
          icon={<ShieldCheck className="w-7 h-7" />}
          title="Locked until approval"
          description="Payment requests are disabled until an admin approves your merchant account."
          actionLabel="Submit Documents"
          onAction={() => navigate("/merchant/onboarding")}
        />
      </div>
    );
  }
  if (statusQuery.data?.status === "APPROVED") {
    return (
      <PaymentStatusBanner
        variant="success"
        title="Payment Approved"
        amountKc={amount}
        actionLabel="New Request"
        onAction={() => setRequestId(null)}
      />
    );
  }
  if (
    statusQuery.data?.status === "DECLINED" ||
    statusQuery.data?.status === "EXPIRED"
  ) {
    return (
      <PaymentStatusBanner
        variant="failure"
        title="Request Not Approved"
        description={`Status: ${statusQuery.data?.status}`}
        actionLabel="Try Again"
        onAction={() => setRequestId(null)}
      />
    );
  }
  if (requestId) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center text-center gap-3">
        <div className="w-16 h-16 rounded-full bg-primary-500/10 border border-primary-500/30 animate-pulse" />
        <h2 className="text-xl font-bold text-text-primary">
          Waiting for client to approve...
        </h2>
        <div className="text-sm text-text-secondary">{phone}</div>
        <div className="text-lg font-semibold text-text-primary">
          {formatKc(amount)} ({formatUsd(amount)})
        </div>
        <Button variant="outline" onClick={() => setRequestId(null)}>
          Cancel Request
        </Button>
      </div>
    );
  }
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Request Payment</h1>
      <Input
        label="Client Phone"
        value={phone}
        onChange={(e) => setPhone(e.target.value)}
      />
      <Select
        label="Route"
        value={routeId}
        onChange={(e) => {
          const next = e.target.value;
          setRouteId(next);
          const route = routes?.find((r) => r.routeId === next);
          if (route) setAmount(route.fareKc);
        }}
        options={[
          { label: "Select route", value: "" },
          ...(routes || []).map((r) => ({
            label: `${r.name} (${formatKc(r.fareKc)})`,
            value: r.routeId,
          })),
        ]}
      />
      {selectedRoute && (
        <div className="text-xs text-text-secondary">
          {selectedRoute.origin} {"->"} {selectedRoute.destination}
        </div>
      )}
      <Input
        label="Amount"
        type="number"
        value={amount}
        onChange={(e) => setAmount(Number(e.target.value))}
      />
      <Button
        fullWidth
        isLoading={createRequest.isPending}
        onClick={handleSend}
      >
        Send Request
      </Button>
    </div>
  );
}
