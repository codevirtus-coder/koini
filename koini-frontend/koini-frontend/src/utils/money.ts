const DEFAULT_KC_TO_USD = 0.0001;

function resolveKcToUsd(): number {
  const raw = import.meta.env.VITE_KC_TO_USD as string | undefined;
  if (!raw) return DEFAULT_KC_TO_USD;
  const parsed = Number.parseFloat(raw);
  if (!Number.isFinite(parsed) || parsed <= 0) return DEFAULT_KC_TO_USD;
  return parsed;
}

const KC_TO_USD = resolveKcToUsd();

export const formatUsd = (amountKc: number): string =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(amountKc * KC_TO_USD);

export const formatKc = (amountKc: number): string => `${amountKc.toLocaleString()}`;

export const formatAmount = (amountKc: number): { kc: string; usd: string } => ({
  kc: formatKc(amountKc),
  usd: formatUsd(amountKc),
});

export const kcToUsd = (amountKc: number): number => amountKc * KC_TO_USD;

export const usdToKc = (usdAmount: number): number => Math.round(usdAmount / KC_TO_USD);

export { KC_TO_USD };
