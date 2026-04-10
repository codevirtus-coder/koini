import React from 'react';
import { Badge } from '../ui/Badge';
import type { TransactionType } from '../../api/types';

interface TransactionTypeBadgeProps {
  type: TransactionType | string;
}

export function TransactionTypeBadge({ type }: TransactionTypeBadgeProps): JSX.Element {
  const normalized = type.toString().toUpperCase();
  if (normalized === 'TOPUP') return <Badge variant="success">Top Up</Badge>;
  if (normalized === 'FARE_PAYMENT') return <Badge variant="info">Fare</Badge>;
  if (normalized === 'WITHDRAWAL') return <Badge variant="warning">Withdrawal</Badge>;
  if (normalized === 'TRANSFER') return <Badge variant="default">Transfer</Badge>;
  if (normalized === 'FEE') return <Badge variant="danger">Fee</Badge>;
  return <Badge variant="default">{normalized}</Badge>;
}
