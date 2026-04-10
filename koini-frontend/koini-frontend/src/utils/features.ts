export const FEATURES = {
  p2pTransfers: (import.meta.env.VITE_ENABLE_P2P ?? 'false') === 'true',
} as const;

