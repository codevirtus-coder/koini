import React from 'react';
import { Badge } from '../ui/Badge';
import type { TransactionStatus } from '../../api/types';

interface StatusBadgeProps {
  status: TransactionStatus | string;
}

export function StatusBadge({ status }: StatusBadgeProps): JSX.Element {
  const normalized = status.toString().toUpperCase();
  if (normalized === 'COMPLETED') return <Badge variant="success">Completed</Badge>;
  if (normalized === 'PENDING') return <Badge variant="warning">Pending</Badge>;
  if (normalized === 'FAILED') return <Badge variant="danger">Failed</Badge>;
  if (normalized === 'REVERSED') return <Badge variant="info">Reversed</Badge>;
  return <Badge variant="default">{normalized}</Badge>;
}
