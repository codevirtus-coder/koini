import React from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

interface RolePoint {
  role: string;
  count: number;
}

interface RoleDonutChartProps {
  data: RolePoint[];
}

const COLORS = ['#6366f1', '#22c55e', '#f59e0b'];

export function RoleDonutChart({ data }: RoleDonutChartProps): JSX.Element {
  const total = data.reduce((sum, d) => sum + d.count, 0);
  return (
    <div className="w-full h-60">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie data={data} dataKey="count" nameKey="role" innerRadius={55} outerRadius={80} paddingAngle={2}>
            {data.map((entry, index) => (
              <Cell key={`cell-${entry.role}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{ background: '#12121a', border: '1px solid #1e1e2e', borderRadius: 8 }}
            formatter={(value: number) => [value, 'Users']}
          />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
      <div className="-mt-40 text-center">
        <div className="text-sm text-text-secondary">Total</div>
        <div className="text-xl font-bold text-text-primary">{total}</div>
      </div>
    </div>
  );
}
