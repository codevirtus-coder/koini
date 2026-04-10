import { toast } from 'sonner';

export const notify = {
  success: (msg: string): void => { toast.success(msg, { duration: 3000 }); },
  error: (msg: string): void => { toast.error(msg, { duration: 5000 }); },
  warning: (msg: string): void => { toast.warning(msg, { duration: 4000 }); },
  info: (msg: string): void => { toast.info(msg, { duration: 3000 }); },
  loading: (msg: string): string | number => toast.loading(msg),
  dismiss: (id: string | number): void => { toast.dismiss(id); },
};
