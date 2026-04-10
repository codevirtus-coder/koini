export const normalizePhone = (value: string): string => {
  const digits = value.replace(/\D/g, '');
  if (!digits) return '';
  if (digits.startsWith('0')) return `+263${digits.slice(1)}`;
  if (digits.startsWith('263')) return `+${digits}`;
  if (digits.startsWith('7')) return `+263${digits}`;
  return value.startsWith('+') ? `+${digits}` : `+${digits}`;
};

export const maskPhone = (value: string): string => {
  const digits = value.replace(/\D/g, '');
  if (digits.length < 6) return value;
  const head = digits.slice(0, 4);
  const tail = digits.slice(-3);
  return `${head}****${tail}`;
};

export const validatePhone = (value: string): boolean =>
  /^[\d\s+()-]{10,15}$/.test(value);
