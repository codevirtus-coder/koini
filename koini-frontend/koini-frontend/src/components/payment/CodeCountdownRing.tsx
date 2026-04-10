import React from 'react';
import { motion } from 'framer-motion';

interface CodeCountdownRingProps {
  percentage: number;
  secondsLeft: number;
  size?: number;
  strokeWidth?: number;
}

export function CodeCountdownRing({ percentage, secondsLeft, size = 180, strokeWidth = 6 }: CodeCountdownRingProps): JSX.Element {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (percentage / 100) * circumference;

  let color = '#6366f1';
  if (percentage < 40) color = '#f59e0b';
  if (percentage < 20) color = '#ef4444';

  return (
    <motion.div initial={{ scale: 0.8 }} animate={{ scale: 1 }}>
      <svg width={size} height={size} className="block">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="#2a2a3d"
          strokeWidth={strokeWidth}
          fill="none"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke={color}
          strokeWidth={strokeWidth}
          fill="none"
          strokeDasharray={`${circumference} ${circumference}`}
          strokeDashoffset={offset}
          strokeLinecap="round"
          style={{ transition: 'stroke-dashoffset 200ms ease' }}
        />
        <text x="50%" y="50%" textAnchor="middle" dy="-2" fill="#f1f5f9" fontSize="28" fontWeight="700">
          {secondsLeft}
        </text>
        <text x="50%" y="50%" textAnchor="middle" dy="20" fill="#94a3b8" fontSize="12">
          seconds
        </text>
      </svg>
    </motion.div>
  );
}
