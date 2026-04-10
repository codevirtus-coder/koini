import axios, { type AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { getStoredTokens, setStoredTokens, clearStoredTokens, clearStoredUser } from '../utils/storage';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Allow browser/axios to set multipart boundaries when sending FormData.
    const isFormData = typeof FormData !== 'undefined' && config.data instanceof FormData;
    if (isFormData) {
      try {
        delete (config.headers as Record<string, unknown>)['Content-Type'];
        delete (config.headers as Record<string, unknown>)['content-type'];
      } catch {
        // ignore
      }
    }

    const tokens = getStoredTokens();
    if (tokens?.accessToken) {
      config.headers.Authorization = `Bearer ${tokens.accessToken}`;
    }
    const url = config.url ?? '';
    const skipIdempotency =
      url.includes('/auth/') || Boolean(config.headers['X-Skip-Idempotency']);
    if (['post', 'put', 'patch'].includes(config.method ?? '') && !skipIdempotency) {
      const key = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`;
      config.headers['X-Idempotency-Key'] = key;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

function redirectToLogin(): void {
  if (typeof window === 'undefined') return;
  if (window.location.pathname.startsWith('/login')) return;
  const next = `${window.location.pathname}${window.location.search}${window.location.hash}`;
  clearStoredTokens();
  clearStoredUser();
  window.location.href = `/login?next=${encodeURIComponent(next)}`;
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const data = error.response?.data as { errorCode?: string } | undefined;
    const status = error.response?.status;
    const url = originalRequest.url ?? '';
    const isAuthRoute = url.includes('/auth/');
    const isTokenExpired = status === 401 && data?.errorCode === 'AUTH_002';

    // If the backend says the token is expired, force re-login (no silent refresh).
    if (isTokenExpired && !isAuthRoute) {
      redirectToLogin();
      return Promise.reject(error);
    }

    // Any other unauthorized response while authenticated routes are being used: force re-login.
    if (status === 401 && !isAuthRoute) {
      redirectToLogin();
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);
