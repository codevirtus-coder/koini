import { type Variants } from 'framer-motion';

export const pageVariants: Variants = {
  initial: { opacity: 0, y: 12 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.25, ease: 'easeOut' } },
  exit: { opacity: 0, y: -8, transition: { duration: 0.15 } },
};

export const cardVariants: Variants = {
  initial: { opacity: 0, y: 16, scale: 0.98 },
  animate: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: { duration: 0.3, ease: [0.34, 1.56, 0.64, 1] },
  },
};

export const staggerContainer: Variants = {
  animate: { transition: { staggerChildren: 0.07 } },
};

export const checkVariants: Variants = {
  initial: { pathLength: 0, opacity: 0 },
  animate: { pathLength: 1, opacity: 1, transition: { duration: 0.5, ease: 'easeOut' } },
};

export const shakeVariants: Variants = {
  shake: {
    x: [0, -8, 8, -8, 8, -4, 4, 0],
    transition: { duration: 0.4 },
  },
};

export const numberVariants: Variants = {
  initial: { opacity: 0, scale: 0.8 },
  animate: { opacity: 1, scale: 1, transition: { type: 'spring', stiffness: 300, damping: 20 } },
};

export const glowVariants: Variants = {
  animate: {
    boxShadow: [
      '0 0 0px rgba(99,102,241,0)',
      '0 0 30px rgba(99,102,241,0.4)',
      '0 0 0px rgba(99,102,241,0)',
    ],
    transition: { duration: 2, repeat: Infinity },
  },
};
