const CLIENT_API_PREFIX = import.meta.env.VITE_CLIENT_API_PREFIX || '/passenger';

const CLIENT_ENDPOINTS = {
  balance: `${CLIENT_API_PREFIX}/wallet/balance`,
  transactions: `${CLIENT_API_PREFIX}/wallet/transactions`,
  transfer: `${CLIENT_API_PREFIX}/wallet/transfer`,
  generateCode: `${CLIENT_API_PREFIX}/payments/generate-code`,
  pesepayTopupInitiate: `${CLIENT_API_PREFIX}/topups/pesepay/initiate`,
  pesepayTopupConfirm: (txId: string) => `${CLIENT_API_PREFIX}/topups/pesepay/${txId}/confirm`,
  approveRequest: (id: string) => `${CLIENT_API_PREFIX}/payments/request/${id}/approve`,
  declineRequest: (id: string) => `${CLIENT_API_PREFIX}/payments/request/${id}/decline`,
  profile: `${CLIENT_API_PREFIX}/profile`,
} as const;

export const ENDPOINTS = {
  auth: {
    register: '/auth/register',
    login: '/auth/login',
    refresh: '/auth/refresh',
    logout: '/auth/logout',
    changePin: '/auth/pin/change',
    setupPin: '/auth/pin/setup',
  },
  client: CLIENT_ENDPOINTS,
  /**
   * Legacy alias for backwards compatibility.
   * Prefer `ENDPOINTS.client`.
   */
  passenger: CLIENT_ENDPOINTS,
  /**
   * Merchant portal endpoints.
   * Keep `conductor` key for backward compatibility with existing hooks.
   * Set `VITE_MERCHANT_API_PREFIX=/merchant` when the backend path changes.
   */
  conductor: {
    redeemCode: `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/payments/redeem`,
    createRequest: `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/payments/request`,
    requestStatus: (id: string) => `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/payments/request/${id}/status`,
    balance: `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/wallet/balance`,
    transactions: `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/wallet/transactions`,
    shiftReport: `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/shift/report`,
    routes: `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/routes`,
    onboarding: `${import.meta.env.VITE_MERCHANT_API_PREFIX || '/conductor'}/onboarding`,
  },
  agent: {
    topup: '/agent/topup',
    floatBalance: '/agent/float/balance',
    initiateWithdrawal: '/agent/withdrawal/initiate',
    confirmWithdrawal: (id: string) => `/agent/withdrawal/${id}/confirm`,
    reverseWithdrawal: (id: string) => `/agent/withdrawal/${id}/reverse`,
    transactions: '/agent/transactions',
    dailySummary: '/agent/daily-summary',
  },
  admin: {
    dashboard: '/admin/dashboard',
    users: '/admin/users',
    userDetail: (id: string) => `/admin/users/${id}`,
    createConductor: '/admin/users/conductor',
    createAgent: '/admin/users/agent',
    suspendUser: (id: string) => `/admin/users/${id}/suspend`,
    activateUser: (id: string) => `/admin/users/${id}/activate`,
    transactions: '/admin/transactions',
    auditLogs: '/admin/audit-logs',
    routes: '/admin/routes',
    routeDetail: (id: string) => `/admin/routes/${id}`,
    deactivateRoute: (id: string) => `/admin/routes/${id}/deactivate`,
    reconciliation: '/admin/reconciliation',
    agents: '/admin/agents',
    pesepayIntegration: '/admin/integrations/pesepay',
    pesepaySaveKeys: '/admin/integrations/pesepay/keys',
    merchantKycApplication: (userId: string) => `/admin/merchants/${userId}/application`,
    merchantKycDocument: (userId: string, type: 'idDocument' | 'proofOfAddress') => `/admin/merchants/${userId}/documents/${type}`,
  },
} as const;
