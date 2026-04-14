import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type { ApiResponse, PaginatedResponse, Transaction, TransactionHistoryFilter, TransactionHistoryResponse, WalletBalance, UserRole } from '../api/types';
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

export function useTransactionHistory(
  filter: TransactionHistoryFilter,
  options?: { enabled?: boolean; refetchInterval?: number | false }
) {
  const { state } = useAuth();
  const endpoint = getTransactionsEndpoint(state.user?.role);
  const role = state.user?.role;

  return useQuery({
    queryKey: ['transactions', state.user?.userId, filter],
    queryFn: () =>
      apiClient
        .get<ApiResponse<TransactionHistoryResponse | PaginatedResponse<Transaction>>>(endpoint ?? '', { params: filter })
        .then((r) => {
          const data = r.data.data as TransactionHistoryResponse | PaginatedResponse<Transaction>;
          if (data && typeof data === 'object' && 'transactions' in data) {
            const th = data as TransactionHistoryResponse;
            const size = th.size || filter.size || 20;
            const totalPages = size > 0 ? Math.ceil(th.total / size) : 0;

            const mapped: Transaction[] = (th.transactions || []).map((tx) => {
              const rawType = (tx.type || '').toString().toUpperCase();

              // Direction depends on the user's perspective (client pays fares, merchant receives fares).
              const isMerchant = role === 'CONDUCTOR' || role === 'MERCHANT';
              const isClient = role === 'PASSENGER' || role === 'CLIENT';

              let direction: Transaction['direction'] = 'debit';
              if (rawType === 'TOPUP' || rawType === 'REFUND') direction = 'credit';
              if (rawType === 'WITHDRAWAL' || rawType === 'FEE') direction = 'debit';
              if (rawType === 'FARE_PAYMENT') direction = isMerchant ? 'credit' : 'debit';
              if (rawType === 'TRANSFER') direction = isClient ? 'debit' : 'debit';

              return {
                txId: tx.transactionId,
                txType: rawType as Transaction['txType'],
                amountKc: tx.amountKc,
                amountUsd: tx.amountUsd,
                feeKc: tx.feeKc,
                status: tx.status as Transaction['status'],
                reference: tx.reference,
                description: null,
                createdAt: tx.createdAt,
                direction,
              };
            });

            const normalized: PaginatedResponse<Transaction> = {
              content: mapped,
              totalElements: th.total,
              totalPages,
              size,
              number: th.page,
              first: th.page <= 0,
              last: totalPages > 0 ? th.page >= totalPages - 1 : true,
            };
            return normalized;
          }

          return data as PaginatedResponse<Transaction>;
        }),
    enabled: (options?.enabled ?? true) && !!state.user && !!endpoint,
    placeholderData: keepPreviousData,
    refetchInterval: options?.refetchInterval,
  });
}
