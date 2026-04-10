import type { Config } from 'tailwindcss';
import { tokens } from './src/design/tokens';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: tokens.color.primary,
        accent: tokens.color.accent,
        success: tokens.color.success,
        warning: tokens.color.warning,
        danger: tokens.color.danger,
        surface: tokens.color.surface,
        text: tokens.color.text,
        credit: tokens.color.credit,
        debit: tokens.color.debit,
      },
      fontFamily: {
        sans: ['Inter', 'SF Pro Display', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      borderRadius: {
        sm: tokens.radius.sm,
        md: tokens.radius.md,
        lg: tokens.radius.lg,
        xl: tokens.radius.xl,
        '2xl': tokens.radius['2xl'],
      },
      boxShadow: {
        sm: tokens.shadow.sm,
        md: tokens.shadow.md,
        lg: tokens.shadow.lg,
        glow: tokens.shadow.glow,
        amber: tokens.shadow.amber,
      },
      animation: {
        'fade-in': 'fadeIn 200ms ease forwards',
        'slide-up': 'slideUp 250ms ease forwards',
        'slide-down': 'slideDown 250ms ease forwards',
        'pulse-slow': 'pulse 3s ease-in-out infinite',
        'spin-slow': 'spin 3s linear infinite',
        'bounce-sm': 'bounceSm 1s ease infinite',
        'count-up': 'countUp 800ms ease forwards',
      },
      keyframes: {
        fadeIn: { from: { opacity: '0' }, to: { opacity: '1' } },
        slideUp: { from: { opacity: '0', transform: 'translateY(12px)' }, to: { opacity: '1', transform: 'translateY(0)' } },
        slideDown: { from: { opacity: '0', transform: 'translateY(-12px)' }, to: { opacity: '1', transform: 'translateY(0)' } },
        bounceSm: { '0%,100%': { transform: 'translateY(-3px)' }, '50%': { transform: 'translateY(0)' } },
      },
    },
  },
  plugins: [],
} satisfies Config;
