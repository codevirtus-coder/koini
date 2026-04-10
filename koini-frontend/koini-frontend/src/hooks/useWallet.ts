import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type { ApiResponse, PaginatedResponse, Transaction, TransactionHistoryFilter, WalletBalance, UserRole } from '../api/types';
import { useAuth } from './useAuth';

function getBalanceEndpoint(role: UserRole | undefined): string | null {
  switch (role) {
    case 'PASSENGER':
      return ENDPOINTS.client.balance;
    case 'CLIENT':
      return ENDPOINTS.client.balance;
    case 'CONDUCTOR':
    case 'MERCHANT':
      return ENDPOINTS.conductor.balance;
    case 'AGENT':
      return ENDPOINTS.agent.floatBalance;
    default:
      return null;
  }
}

function getTransactionsEndpoint(role: UserRole | undefined): string | null {
  switch (role) {
    case 'PASSENGER':
      return ENDPOINTS.client.transactions;
    case 'CLIENT':
      return ENDPOINTS.client.transactions;
    case 'CONDUCTOR':
    case 'MERCHANT':
      return ENDPOINTS.conductor.transactions;
    case 'AGENT':
      return ENDPOINTS.agent.transactions;
    case 'ADMIN':
      return ENDPOINTS.admin.transactions;
    default:
      return null;
  }
}

export function useWalletBalance() {
  const { state } = useAuth();
  const endpoint = getBalanceEndpoint(state.user?.role);

  return useQuery({
    queryKey: ['wallet', 'balance', state.user?.userId],
    queryFn: () =>
      apiClient.get<ApiResponse<WalletBalance>>(endpoint ?? '').then((r) => r.data.data),
    refetchInterval: 30000,
    enabled: !!state.user && !!endpoint,
    staleTime: 15000,
  });
}

export function useTransactionHistory(filter: TransactionHistoryFilter) {
  const { state } = useAuth();
  const endpoint = getTransactionsEndpoint(state.user?.role);

  return useQuery({
    queryKey: ['transactions', state.user?.userId, filter],
    queryFn: () =>
      apiClient
        .get<ApiResponse<PaginatedResponse<Transaction>>>(endpoint ?? '', { params: filter })
        .then((r) => r.data.data),
    enabled: !!state.user && !!endpoint,
    placeholderData: keepPreviousData,
  });
}
