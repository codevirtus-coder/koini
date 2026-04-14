import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type {
  ApiResponse,
  MerchantDashboardSummary,
  MerchantPaymentRequest,
  MerchantPaymentRequestsResponse,
  MerchantReceipt,
} from '../api/types';

export function useMerchantDashboardSummary(date?: string) {
  return useQuery({
    queryKey: ['merchant-dashboard-summary', date],
    queryFn: () =>
      apiClient
        .get<ApiResponse<MerchantDashboardSummary>>(ENDPOINTS.merchant.dashboardSummary, { params: date ? { date } : undefined })
        .then((r) => r.data.data),
  });
}

export function useMerchantPaymentRequests(params?: Record<string, string | number | undefined>) {
  return useQuery({
    queryKey: ['merchant-payment-requests', params],
    queryFn: () =>
      apiClient
        .get<ApiResponse<MerchantPaymentRequestsResponse>>(ENDPOINTS.merchant.paymentRequests, { params })
        .then((r) => r.data.data),
  });
}

export function useMerchantPaymentRequest(requestId: string | null) {
  return useQuery({
    queryKey: ['merchant-payment-request', requestId],
    queryFn: () =>
      apiClient
        .get<ApiResponse<MerchantPaymentRequest>>(ENDPOINTS.merchant.paymentRequest(requestId ?? ''))
        .then((r) => r.data.data),
    enabled: !!requestId,
  });
}

export function useCancelMerchantRequest() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (requestId: string) =>
      apiClient
        .post<ApiResponse<{ requestId: string; status: string }>>(ENDPOINTS.merchant.cancelPaymentRequest(requestId))
        .then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['merchant-payment-requests'] });
    },
  });
}

export function useMerchantReceipt() {
  return useMutation({
    mutationFn: (transactionId: string) =>
      apiClient
        .get<ApiResponse<MerchantReceipt>>(ENDPOINTS.merchant.receipt(transactionId))
        .then((r) => r.data.data),
  });
}
