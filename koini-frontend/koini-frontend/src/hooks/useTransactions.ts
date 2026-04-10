import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type { ApiResponse, PaginatedResponse, Transaction, TransactionHistoryFilter } from '../api/types';

export function useAdminTransactions(filter: TransactionHistoryFilter) {
  return useQuery({
    queryKey: ['admin-transactions', filter],
    queryFn: () =>
      apiClient
        .get<ApiResponse<PaginatedResponse<Transaction>>>(ENDPOINTS.admin.transactions, { params: filter })
        .then((r) => r.data.data),
  });
}
