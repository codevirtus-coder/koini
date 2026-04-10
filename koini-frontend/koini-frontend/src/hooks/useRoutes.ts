import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import type { ApiResponse, CreateRouteRequest, Route } from '../api/types';

export function useConductorRoutes(options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: ['conductor-routes'],
    queryFn: () => apiClient.get<ApiResponse<Route[]>>(ENDPOINTS.conductor.routes).then((r) => r.data.data),
    enabled: options?.enabled ?? true,
  });
}

export function useAdminRoutes() {
  return useQuery({
    queryKey: ['admin-routes'],
    queryFn: () => apiClient.get<ApiResponse<Route[]>>(ENDPOINTS.admin.routes).then((r) => r.data.data),
  });
}

export function useCreateRoute() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: CreateRouteRequest) =>
      apiClient.post<ApiResponse<Route>>(ENDPOINTS.admin.routes, req).then((r) => r.data.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-routes'] }),
  });
}

export function useDeactivateRoute() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (routeId: string) =>
      apiClient.post<ApiResponse<Route>>(ENDPOINTS.admin.deactivateRoute(routeId)).then((r) => r.data.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-routes'] }),
  });
}
