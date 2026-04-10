import type { UserSummary } from '../api/types';

export interface StoredTokens {
  accessToken: string;
  refreshToken: string;
}

const TOKEN_KEY = 'koini_tokens';
const USER_KEY = 'koini_user';
const PENDING_PESEPAY_TOPUP_PREFIX = 'koini_pending_pesepay_topup_';

export interface PendingPesepayTopup {
  txId: string;
  reference?: string;
  amountKc?: number;
  redirectUrl?: string;
  createdAt: string;
}

export function getStoredTokens(): StoredTokens | null {
  try {
    const raw = localStorage.getItem(TOKEN_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as StoredTokens;
  } catch {
    return null;
  }
}

export function setStoredTokens(tokens: StoredTokens): void {
  localStorage.setItem(TOKEN_KEY, JSON.stringify(tokens));
}

export function clearStoredTokens(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function getStoredUser(): UserSummary | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as UserSummary;
  } catch {
    return null;
  }
}

export function setStoredUser(user: UserSummary): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearStoredUser(): void {
  localStorage.removeItem(USER_KEY);
}

function pendingPesepayKey(userId: string): string {
  return `${PENDING_PESEPAY_TOPUP_PREFIX}${userId}`;
}

export function getPendingPesepayTopup(userId: string): PendingPesepayTopup | null {
  try {
    const raw = localStorage.getItem(pendingPesepayKey(userId));
    if (!raw) return null;
    return JSON.parse(raw) as PendingPesepayTopup;
  } catch {
    return null;
  }
}

export function setPendingPesepayTopup(userId: string, pending: PendingPesepayTopup): void {
  localStorage.setItem(pendingPesepayKey(userId), JSON.stringify(pending));
}

export function clearPendingPesepayTopup(userId: string): void {
  localStorage.removeItem(pendingPesepayKey(userId));
}
