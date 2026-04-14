export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  requestId: string;
  timestamp: string;
}

export interface ApiError {
  success: false;
  errorCode: string;
  message: string;
  timestamp: string;
  requestId: string | null;
  path: string;
  details?: Record<string, string> | string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface UserSummary {
  userId: string;
  maskedPhone: string;
  fullName: string | null;
  role: UserRole;
  status: UserStatus;
}

export type UserRole = 'PASSENGER' | 'CLIENT' | 'CONDUCTOR' | 'MERCHANT' | 'AGENT' | 'ADMIN' | 'FLEET_OWNER';
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'LOCKED' | 'PENDING_VERIFICATION';

export interface LoginRequest {
  phone: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresIn: number;
  user: UserSummary;
}

export interface RegisterRequest {
  phone: string;
  password: string;
  fullName?: string;
  /**
   * Optional: used when the backend supports role-based self registration.
   * Example: "CONDUCTOR" (merchant) vs "PASSENGER" (client).
   */
  role?: UserRole;
}

export interface RegisterResponse {
  userId: string;
  maskedPhone: string;
  message: string;
}

export interface ChangePinRequest {
  currentPin: string;
  newPin: string;
  confirmNewPin: string;
}

export interface WalletBalance {
  walletId: string;
  balanceKc: number;
  balanceUsd: string;
  points?: number;
  status: 'ACTIVE' | 'FROZEN' | 'CLOSED';
  lastUpdated: string;
}

export interface TopUpRequest {
  holderPhone: string;
  amountKc: number;
}

export interface TopUpResponse {
  transactionId: string;
  reference: string;
  amountKc: number;
  newBalanceKc: number;
  passengerMaskedPhone: string;
}

export interface TransferRequest {
  toPhone: string;
  amountKc: number;
  pin: string;
}

export interface PesepayTopupInitiateRequest {
  amountKc: number;
  currencyCode?: string;
}

export interface PesepayTopupInitiateResponse {
  txId: string;
  reference: string;
  amountKc: number;
  pesepayReferenceNumber: string;
  pollUrl: string;
  redirectUrl: string;
}

export interface PesepayTopupConfirmRequest {
  referenceNumber?: string;
}

export interface PesepayTopupConfirmResponse {
  txId: string;
  reference: string;
  paid: boolean;
  status: string;
  amountKcCredited?: number;
  pointsAdded?: number;
}

export type TransactionType = 'TOPUP' | 'FARE_PAYMENT' | 'WITHDRAWAL' | 'TRANSFER' | 'FEE' | 'REFUND';
export type TransactionStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED';

export interface Transaction {
  txId: string;
  txType: TransactionType;
  amountKc: number;
  amountUsd: string;
  feeKc: number;
  status: TransactionStatus;
  reference: string;
  description: string | null;
  createdAt: string;
  direction: 'credit' | 'debit';
  counterpartyMaskedPhone?: string;
}

export interface TransactionHistoryFilter {
  dateFrom?: string;
  dateTo?: string;
  type?: TransactionType;
  status?: TransactionStatus;
  page?: number;
  size?: number;
}

export interface TransactionHistoryResponse {
  transactions: Array<{
    transactionId: string;
    type: string;
    status: string;
    amountKc: number;
    amountUsd: string;
    feeKc: number;
    feeUsd: string;
    reference: string;
    createdAt: string;
  }>;
  page: number;
  size: number;
  total: number;
}

export interface MerchantDashboardSummary {
  wallet: {
    balanceKc: number;
    balanceUsd: string;
    points: number;
    status: string;
  };
  today: {
    faresCount: number;
    earningsKc: number;
    earningsUsd: string;
    firstFareAt: string | null;
    lastFareAt: string | null;
  };
  requests: {
    pendingCount: number;
    lastRequestAt: string | null;
  };
  recentTransactions: Array<{
    transactionId: string;
    type: string;
    status: string;
    amountKc: number;
    amountUsd: string;
    feeKc: number;
    feeUsd: string;
    reference: string;
    createdAt: string;
  }>;
}

export interface MerchantPaymentRequest {
  requestId: string;
  passengerMaskedPhone: string;
  amountKc: number;
  amountUsd: string;
  status: 'PENDING' | 'APPROVED' | 'DECLINED' | 'EXPIRED' | 'CANCELLED';
  createdAt: string;
  expiresAt: string;
  respondedAt: string | null;
  routeId: string | null;
}

export interface MerchantPaymentRequestsResponse {
  requests: MerchantPaymentRequest[];
  page: number;
  size: number;
  total: number;
}

export interface MerchantReceipt {
  transactionId: string;
  reference: string;
  type: string;
  status: string;
  amountKc: number;
  amountUsd: string;
  feeKc: number;
  feeUsd: string;
  description: string | null;
  createdAt: string;
}

export interface GenerateCodeRequest {
  amountKc: number;
  pin: string;
  routeId?: string;
}

export interface GenerateCodeResponse {
  code: string;
  expiresAt: string;
  amountKc: number;
  amountUsd: string;
  feeKc: number;
  feeUsd: string;
  totalDeductionKc: number;
}

export interface RedeemCodeRequest {
  code: string;
}

export interface RedeemCodeResponse {
  transactionId: string;
  reference: string;
  amountKc: number;
  passengerMaskedPhone: string;
  routeName: string | null;
}

export interface CreatePaymentRequestRequest {
  passengerPhone: string;
  amountKc: number;
  routeId?: string;
}

export interface CreatePaymentRequestResponse {
  requestId: string;
  passengerMaskedPhone: string;
  amountKc: number;
  expiresAt: string;
}

export interface PaymentRequestStatus {
  requestId: string;
  status: 'PENDING' | 'APPROVED' | 'DECLINED' | 'EXPIRED' | 'CANCELLED';
  respondedAt: string | null;
}

export interface ApproveRequestRequest {
  requestId: string;
  pin: string;
}

export interface Route {
  routeId: string;
  name: string;
  origin: string;
  destination: string;
  fareKc: number;
  fareUsd: string;
  isActive: boolean;
  createdAt: string;
}

export interface CreateRouteRequest {
  name: string;
  origin: string;
  destination: string;
  fareKc: number;
}

export interface DashboardStats {
  totalUsers: { passengers: number; conductors: number; agents: number };
  totalWalletBalanceKc: number;
  totalWalletBalanceUsd: string;
  transactionsToday: number;
  transactionVolumeKcToday: number;
  activePaymentCodes: number;
  flaggedAccounts: number;
}

export interface AdminUser {
  userId: string;
  phone: string;
  maskedPhone: string;
  fullName: string | null;
  role: UserRole;
  status: UserStatus;
  kycLevel: number;
  createdAt: string;
  lastLogin: string | null;
}

export interface AdminUserDetail extends AdminUser {
  wallet: WalletBalance;
  recentTransactions: Transaction[];
  agentDetail?: AgentDetail;
}

export interface AgentDetail {
  agentId: string;
  businessName: string | null;
  location: string | null;
  floatLimitKc: number;
  floatBalanceKc: number;
  cashHeldUsd: number;
  status: 'ACTIVE' | 'SUSPENDED';
}

export interface ReconciliationReport {
  date: string;
  totalTopUpsKc: number;
  totalFaresKc: number;
  totalWithdrawalsKc: number;
  totalTransfersKc: number;
  netBalanceKc: number;
  discrepancy: number;
  status: 'BALANCED' | 'DISCREPANCY';
}

export interface AuditLog {
  logId: string;
  actorId: string | null;
  actorType: string;
  action: string;
  entityType: string | null;
  entityId: string | null;
  outcome: 'SUCCESS' | 'FAILURE';
  ipAddress: string | null;
  createdAt: string;
}

export interface AdminPesepayKeysRequest {
  integrationKey: string;
  encryptionKey: string;
}

export interface AdminPesepayIntegrationStatus {
  configured: boolean;
  integrationKeyMasked?: string;
  encryptionKeyMasked?: string;
}

export interface MerchantKycApplication {
  userId: string;
  status: UserStatus;
  merchantDetail: {
    businessName: string | null;
    tradingName: string | null;
    addressLine1: string | null;
    city: string | null;
    country: string | null;
    idNumber: string | null;
    submittedAt: string | null;
  };
  merchantDocuments: {
    idDocumentUrl: string | null;
    proofOfAddressUrl: string | null;
  };
}
