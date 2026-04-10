import type { AxiosError } from 'axios';
import type { ApiError } from '../api/types';

function isAxiosError(value: unknown): value is AxiosError {
  return typeof value === 'object' && value !== null && 'isAxiosError' in value;
}

export function getApiErrorCode(error: unknown): string | undefined {
  if (!isAxiosError(error)) return undefined;
  const data = error.response?.data as ApiError | undefined;
  return data?.errorCode;
}

export function getApiErrorStatus(error: unknown): number | undefined {
  if (!isAxiosError(error)) return undefined;
  return error.response?.status;
}

export function getApiErrorMessage(error: unknown): string {
  if (isAxiosError(error)) {
    const data = error.response?.data as ApiError | undefined;
    if (data?.details) {
      if (typeof data.details === 'string') return data.details;
      const first = Object.values(data.details)[0];
      if (first) return first;
    }
    if (data?.message) return data.message;
    if (typeof error.message === 'string' && error.message.trim()) return error.message;
    return 'Request failed';
  }

  if (error instanceof Error && error.message.trim()) return error.message;
  return 'Something went wrong';
}
