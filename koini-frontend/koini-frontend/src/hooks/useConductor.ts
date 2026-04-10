import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type { ApiResponse } from '../api/types';

export interface ShiftReport {
  fares: number;
  revenueKc: number;
  activeSince: string;
  recentFares: Array<{ reference: string; time: string; passenger: string; amount: string }>;
}

export function useConductorShiftReport(options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: ['conductor-shift'],
    queryFn: () => apiClient.get<ApiResponse<ShiftReport>>(ENDPOINTS.conductor.shiftReport).then((r) => r.data.data),
    enabled: options?.enabled ?? true,
  });
}
