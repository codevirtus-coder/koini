import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type {
  AdminUser,
  AdminUserDetail,
  AdminPesepayIntegrationStatus,
  AdminPesepayKeysRequest,
  ApiResponse,
  AuditLog,
  DashboardStats,
  MerchantKycApplication,
  PaginatedResponse,
  ReconciliationReport,
} from '../api/types';

export function useAdminDashboard() {
  return useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: () => apiClient.get<ApiResponse<DashboardStats>>(ENDPOINTS.admin.dashboard).then((r) => r.data.data),
  });
}

export function useAdminUsers(params?: Record<string, string | number | undefined>) {
  return useQuery({
    queryKey: ['admin-users', params],
    queryFn: () =>
      apiClient
        .get<ApiResponse<PaginatedResponse<AdminUser>>>(ENDPOINTS.admin.users, { params })
        .then((r) => r.data.data),
  });
}

export function useAdminUserDetail(id: string) {
  return useQuery({
    queryKey: ['admin-user', id],
    queryFn: () => apiClient.get<ApiResponse<AdminUserDetail>>(ENDPOINTS.admin.userDetail(id)).then((r) => r.data.data),
    enabled: !!id,
  });
}

export function useAdminMerchantKycApplication(userId: string, enabled: boolean) {
  return useQuery({
    queryKey: ['admin-merchant-kyc', userId],
    queryFn: () =>
      apiClient
        .get<ApiResponse<MerchantKycApplication>>(ENDPOINTS.admin.merchantKycApplication(userId))
        .then((r) => r.data.data),
    enabled: !!userId && enabled,
    retry: 0,
  });
}

export function useAdminAgents() {
  return useQuery({
    queryKey: ['admin-agents'],
    queryFn: () => apiClient.get<ApiResponse<AdminUser[]>>(ENDPOINTS.admin.agents).then((r) => r.data.data),
  });
}

export function useAdminAuditLogs(params?: Record<string, string | number | undefined>) {
  return useQuery({
    queryKey: ['admin-audit', params],
    queryFn: () => apiClient.get<ApiResponse<PaginatedResponse<AuditLog>>>(ENDPOINTS.admin.auditLogs, { params }).then((r) => r.data.data),
  });
}

export function useReconciliation(date: string) {
  return useQuery({
    queryKey: ['admin-reconciliation', date],
    queryFn: () => apiClient.get<ApiResponse<ReconciliationReport>>(ENDPOINTS.admin.reconciliation, { params: { date } }).then((r) => r.data.data),
    enabled: !!date,
  });
}

export function useSuspendUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiClient.post<ApiResponse<AdminUser>>(ENDPOINTS.admin.suspendUser(id)).then((r) => r.data.data),
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      queryClient.invalidateQueries({ queryKey: ['admin-user', id] });
    },
  });
}

export function useActivateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiClient.post<ApiResponse<AdminUser>>(ENDPOINTS.admin.activateUser(id)).then((r) => r.data.data),
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      queryClient.invalidateQueries({ queryKey: ['admin-user', id] });
    },
  });
}

export function useCreateConductor() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: Record<string, unknown>) =>
      apiClient.post<ApiResponse<AdminUser>>(ENDPOINTS.admin.createConductor, payload).then((r) => r.data.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
  });
}

export function useCreateAgent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: Record<string, unknown>) =>
      apiClient.post<ApiResponse<AdminUser>>(ENDPOINTS.admin.createAgent, payload).then((r) => r.data.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-users'] }),
  });
}

export function useAdminPesepayIntegrationStatus() {
  return useQuery({
    queryKey: ['admin-integrations', 'pesepay'],
    queryFn: () =>
      apiClient
        .get<ApiResponse<AdminPesepayIntegrationStatus>>(ENDPOINTS.admin.pesepayIntegration)
        .then((r) => r.data.data),
  });
}

export function useAdminSavePesepayKeys() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: AdminPesepayKeysRequest) =>
      apiClient.post<ApiResponse<AdminPesepayIntegrationStatus>>(ENDPOINTS.admin.pesepaySaveKeys, req).then((r) => r.data.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-integrations', 'pesepay'] }),
  });
}
