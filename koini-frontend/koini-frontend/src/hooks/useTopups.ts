import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type {
  ApiResponse,
  PesepayTopupConfirmRequest,
  PesepayTopupConfirmResponse,
  PesepayTopupInitiateRequest,
  PesepayTopupInitiateResponse,
} from '../api/types';

export function usePesepayTopupInitiate() {
  return useMutation({
    mutationFn: (req: PesepayTopupInitiateRequest) =>
      apiClient
        .post<ApiResponse<PesepayTopupInitiateResponse>>(ENDPOINTS.client.pesepayTopupInitiate, req)
        .then((r) => r.data.data),
  });
}

export function usePesepayTopupConfirm() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ txId, body }: { txId: string; body?: PesepayTopupConfirmRequest }) =>
      apiClient
        .post<ApiResponse<PesepayTopupConfirmResponse>>(ENDPOINTS.client.pesepayTopupConfirm(txId), body ?? {})
        .then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['wallet'] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
    },
  });
}
