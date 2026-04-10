import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type { ApiResponse, TopUpRequest, TopUpResponse } from '../api/types';

export function useAgentFloatBalance() {
  return useQuery({
    queryKey: ['agent-float'],
    queryFn: () => apiClient.get<ApiResponse<{ balanceKc: number; balanceUsd: string }>>(ENDPOINTS.agent.floatBalance).then((r) => r.data.data),
  });
}

export function useTopUpPassenger() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: TopUpRequest) => apiClient.post<ApiResponse<TopUpResponse>>(ENDPOINTS.agent.topup, req).then((r) => r.data.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['agent-float'] }),
  });
}

export function useInitiateWithdrawal() {
  return useMutation({
    mutationFn: (req: object) => apiClient.post<ApiResponse<{ withdrawalId: string }>>(ENDPOINTS.agent.initiateWithdrawal, req).then((r) => r.data.data),
  });
}

export function useConfirmWithdrawal() {
  return useMutation({
    mutationFn: (id: string) => apiClient.post<ApiResponse<{ status: string }>>(ENDPOINTS.agent.confirmWithdrawal(id)).then((r) => r.data.data),
  });
}

export function useReverseWithdrawal() {
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      apiClient.post<ApiResponse<{ status: string }>>(ENDPOINTS.agent.reverseWithdrawal(id), { reason }).then((r) => r.data.data),
  });
}

export function useAgentDailySummary() {
  return useQuery({
    queryKey: ['agent-summary'],
    queryFn: () => apiClient.get<ApiResponse<Record<string, unknown>>>(ENDPOINTS.agent.dailySummary).then((r) => r.data.data),
  });
}
