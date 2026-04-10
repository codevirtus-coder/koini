import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { formatUsd } from '../../utils/money';

interface VolumePoint {
  day: string;
  fares: number;
  topups: number;
  withdrawals: number;
}

interface TransactionVolumeChartProps {
  data: VolumePoint[];
}

export function TransactionVolumeChart({ data }: TransactionVolumeChartProps): JSX.Element {
  return (
    <div className="w-full h-60">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data}>
          <XAxis dataKey="day" tick={{ fill: '#94a3b8', fontSize: 12 }} />
          <YAxis tickFormatter={(v) => formatUsd(v)} tick={{ fill: '#94a3b8', fontSize: 12 }} />
          <Tooltip
            contentStyle={{ background: '#12121a', border: '1px solid #1e1e2e', borderRadius: 8 }}
            formatter={(value: number) => formatUsd(value)}
          />
          <Legend />
          <Bar dataKey="fares" fill="#6366f1" radius={[4, 4, 0, 0]} />
          <Bar dataKey="topups" fill="#f59e0b" radius={[4, 4, 0, 0]} />
          <Bar dataKey="withdrawals" fill="#22c55e" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
