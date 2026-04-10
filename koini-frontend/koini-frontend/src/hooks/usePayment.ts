import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type {
  ApiResponse,
  CreatePaymentRequestRequest,
  CreatePaymentRequestResponse,
  GenerateCodeRequest,
  GenerateCodeResponse,
  PaymentRequestStatus,
  RedeemCodeRequest,
  RedeemCodeResponse,
} from '../api/types';

export function useGeneratePaymentCode() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: GenerateCodeRequest) =>
      apiClient.post<ApiResponse<GenerateCodeResponse>>(ENDPOINTS.client.generateCode, req).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['wallet', 'balance'] });
    },
  });
}

export function useRedeemCode() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: RedeemCodeRequest) =>
      apiClient.post<ApiResponse<RedeemCodeResponse>>(ENDPOINTS.conductor.redeemCode, req).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['wallet'] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
    },
  });
}

export function useCreatePaymentRequest() {
  return useMutation({
    mutationFn: (req: CreatePaymentRequestRequest) =>
      apiClient
        .post<ApiResponse<CreatePaymentRequestResponse>>(ENDPOINTS.conductor.createRequest, req)
        .then((r) => r.data.data),
  });
}

export function usePaymentRequestStatus(requestId: string | null, enabled: boolean) {
  return useQuery({
    queryKey: ['payment-request', requestId],
    queryFn: () =>
      apiClient
        .get<ApiResponse<PaymentRequestStatus>>(ENDPOINTS.conductor.requestStatus(requestId ?? ''))
        .then((r) => r.data.data),
    enabled: !!requestId && enabled,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      if (status === 'APPROVED' || status === 'DECLINED' || status === 'EXPIRED') return false;
      return 2000;
    },
  });
}
