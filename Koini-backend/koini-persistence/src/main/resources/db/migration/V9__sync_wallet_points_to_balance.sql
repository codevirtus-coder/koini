-- Ensure points (spendable Bank Koins) always mirror balance_kc.
-- This migration removes any drift caused by earlier points multipliers or partial updates.

UPDATE wallets
SET points = balance_kc
WHERE points <> balance_kc;

