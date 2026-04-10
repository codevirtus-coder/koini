import React, { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { cn } from '../../design/cn';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  description?: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  closable?: boolean;
  footer?: React.ReactNode;
}

const sizeStyles: Record<NonNullable<ModalProps['size']>, string> = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-lg',
  xl: 'max-w-xl',
};

export function Modal({
  isOpen,
  onClose,
  title,
  description,
  children,
  size = 'md',
  closable = true,
  footer,
}: ModalProps): JSX.Element | null {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && closable) onClose();
    };
    if (isOpen) window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [isOpen, onClose, closable]);

  if (!isOpen) return null;

  return createPortal(
    <AnimatePresence>
      <motion.div
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm px-4"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={closable ? onClose : undefined}
      >
        <motion.div
          className={cn(
            'w-full bg-surface-card border border-surface-border rounded-2xl p-6',
            sizeStyles[size]
          )}
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          exit={{ y: 10, opacity: 0 }}
          onClick={(e) => e.stopPropagation()}
        >
          {(title || description) && (
            <div className="mb-4">
              {title && <h3 className="text-lg font-semibold text-text-primary">{title}</h3>}
              {description && <p className="text-sm text-text-secondary mt-1">{description}</p>}
            </div>
          )}
          <div>{children}</div>
          {footer && <div className="mt-6">{footer}</div>}
        </motion.div>
      </motion.div>
    </AnimatePresence>,
    document.body
  );
}
