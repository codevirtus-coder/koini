import { format, formatDistanceToNow } from 'date-fns';

export const formatDate = (value: string | Date): string =>
  format(new Date(value), 'MMM d, yyyy');

export const formatTime = (value: string | Date): string =>
  format(new Date(value), 'hh:mm a');

export const formatRelative = (value: string | Date): string =>
  formatDistanceToNow(new Date(value), { addSuffix: true });
