import { z } from 'zod';

export const phoneSchema = z
  .string()
  .min(10, 'Phone number too short')
  .max(15, 'Phone number too long')
  .regex(/^[\d\s+()-]+$/, 'Invalid phone number format');

export const pinSchema = z
  .string()
  .length(4, 'PIN must be exactly 4 digits')
  .regex(/^\d{4}$/, 'PIN must be 4 digits only');

export const passwordSchema = z
  .string()
  .min(8, 'At least 8 characters required')
  .regex(/[A-Z]/, 'Must contain an uppercase letter')
  .regex(/[0-9]/, 'Must contain a number');

const phoneRegex = /^[0-9]{10,15}$/;

export const loginSchema = z.object({
  phone: z
    .string()
    .min(1, 'Phone or username is required')
    .refine((value) => phoneRegex.test(value) || value.trim().length > 0, {
      message: 'Enter a valid phone number or username',
    }),
  password: z.string().min(1, 'Password is required'),
});

export const registerSchema = z.object({
  phone: phoneSchema,
  password: passwordSchema,
  fullName: z.string().max(150).optional(),
});

export const generateCodeSchema = z.object({
  amountKc: z.number().int().min(1, 'Amount must be at least 1'),
  pin: pinSchema,
});

export const transferSchema = z.object({
  toPhone: phoneSchema,
  amountKc: z.number().int().min(1),
  pin: pinSchema,
});

export const topupSchema = z.object({
  holderPhone: phoneSchema,
  amountKc: z.number().int().min(100).max(500000),
});

export const pesepayTopupInitiateSchema = z.object({
  amountKc: z.number().int().min(1).max(500000),
  currencyCode: z.string().trim().length(3).optional(),
});

export const pesepayKeysSchema = z.object({
  integrationKey: z.string().trim().min(10, 'Integration key is required'),
  encryptionKey: z.string().trim().min(10, 'Encryption key is required'),
});

export const createRouteSchema = z.object({
  name: z.string().min(1).max(200),
  origin: z.string().min(1).max(150),
  destination: z.string().min(1).max(150),
  fareKc: z.number().int().min(1),
});
