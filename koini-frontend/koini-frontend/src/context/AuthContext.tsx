import React, { createContext, useEffect, useMemo, useReducer } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { LoginRequest, LoginResponse, UserRole, UserSummary } from '../api/types';
import { apiClient } from '../api/client';
import { ENDPOINTS } from '../api/endpoints';
import {
  clearStoredTokens,
  clearStoredUser,
  getStoredTokens,
  getStoredUser,
  setStoredTokens,
  setStoredUser,
} from '../utils/storage';

interface AuthState {
  user: UserSummary | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

type AuthAction =
  | { type: 'LOGIN_SUCCESS'; payload: LoginResponse }
  | { type: 'LOGOUT' }
  | { type: 'TOKENS_REFRESHED'; payload: { accessToken: string; refreshToken: string } }
  | { type: 'HYDRATION_COMPLETE'; payload: AuthState }
  | { type: 'UPDATE_USER'; payload: Partial<UserSummary> };

interface AuthContextValue {
  state: AuthState;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  isClient: boolean;
  isMerchant: boolean;
  isAgent: boolean;
  isAdmin: boolean;
  portalPath: string;
}

const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false },
  },
});

function getPortalPath(role: UserRole | undefined): string {
  switch (role) {
    case 'PASSENGER':
      return '/client';
    case 'CLIENT':
      return '/client';
    case 'CONDUCTOR':
      return '/merchant';
    case 'MERCHANT':
      return '/merchant';
    case 'AGENT':
      return '/agent';
    case 'ADMIN':
      return '/admin';
    default:
      return '/login';
  }
}

function reducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'LOGIN_SUCCESS':
      return {
        user: action.payload.user,
        accessToken: action.payload.accessToken,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
        isLoading: false,
      };
    case 'TOKENS_REFRESHED':
      return {
        ...state,
        accessToken: action.payload.accessToken,
        refreshToken: action.payload.refreshToken,
      };
    case 'UPDATE_USER':
      return {
        ...state,
        user: state.user ? { ...state.user, ...action.payload } : state.user,
      };
    case 'HYDRATION_COMPLETE':
      return action.payload;
    case 'LOGOUT':
      return { ...initialState, isLoading: false };
    default:
      return state;
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }): JSX.Element {
  const [state, dispatch] = useReducer(reducer, initialState);

  useEffect(() => {
    const tokens = getStoredTokens();
    const user = getStoredUser();
    const hydrated: AuthState = {
      user,
      accessToken: tokens?.accessToken ?? null,
      refreshToken: tokens?.refreshToken ?? null,
      isAuthenticated: !!tokens?.accessToken && !!user,
      isLoading: false,
    };
    dispatch({ type: 'HYDRATION_COMPLETE', payload: hydrated });
  }, []);

  const login = async (credentials: LoginRequest): Promise<void> => {
    const res = await apiClient.post<{ data: LoginResponse }>(ENDPOINTS.auth.login, credentials);
    const payload = res.data.data;
    setStoredTokens({ accessToken: payload.accessToken, refreshToken: payload.refreshToken });
    setStoredUser(payload.user);
    dispatch({ type: 'LOGIN_SUCCESS', payload });
  };

  const logout = async (): Promise<void> => {
    try {
      const refreshToken = state.refreshToken ?? getStoredTokens()?.refreshToken;
      if (refreshToken) {
        await apiClient.post(ENDPOINTS.auth.logout, { refreshToken });
      }
    } finally {
      clearStoredTokens();
      clearStoredUser();
      dispatch({ type: 'LOGOUT' });
      window.location.href = '/login';
    }
  };

  const portalPath = getPortalPath(state.user?.role);

  const value = useMemo<AuthContextValue>(
    () => ({
      state,
      login,
      logout,
      isClient: state.user?.role === 'PASSENGER' || state.user?.role === 'CLIENT',
      isMerchant: state.user?.role === 'CONDUCTOR' || state.user?.role === 'MERCHANT',
      isAgent: state.user?.role === 'AGENT',
      isAdmin: state.user?.role === 'ADMIN',
      portalPath,
    }),
    [state, portalPath]
  );

  return (
    <QueryClientProvider client={queryClient}>
      <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
    </QueryClientProvider>
  );
}

export function useAuthContext(): AuthContextValue {
  const ctx = React.useContext(AuthContext);
  if (!ctx) throw new Error('AuthContext not available');
  return ctx;
}
