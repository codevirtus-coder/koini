import React from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Area } from 'recharts';
import { formatUsd } from '../../utils/money';

interface RevenuePoint {
  day: string;
  fares: number;
  topups: number;
}

interface RevenueChartProps {
  data: RevenuePoint[];
}

export function RevenueChart({ data }: RevenueChartProps): JSX.Element {
  return (
    <div className="w-full h-60">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data}>
          <CartesianGrid stroke="#1e1e2e" strokeDasharray="3 3" vertical={false} />
          <XAxis dataKey="day" tick={{ fill: '#94a3b8', fontSize: 12 }} />
          <YAxis tickFormatter={(v) => formatUsd(v)} tick={{ fill: '#94a3b8', fontSize: 12 }} />
          <Tooltip
            contentStyle={{ background: '#12121a', border: '1px solid #1e1e2e', borderRadius: 8 }}
            labelStyle={{ color: '#e2e8f0' }}
            formatter={(value: number) => formatUsd(value)}
          />
          <Line type="monotone" dataKey="fares" stroke="#6366f1" strokeWidth={2} dot={false} />
          <Line type="monotone" dataKey="topups" stroke="#f59e0b" strokeWidth={2} dot={false} />
          <Area type="monotone" dataKey="fares" stroke="none" fill="#6366f1" fillOpacity={0.1} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
