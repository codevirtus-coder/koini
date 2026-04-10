ALTER TABLE users
  ADD CONSTRAINT chk_users_role CHECK (role IN ('PASSENGER','CONDUCTOR','AGENT','ADMIN','FLEET_OWNER')),
  ADD CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','SUSPENDED','LOCKED','PENDING_VERIFICATION')),
  ADD CONSTRAINT chk_users_kyc CHECK (kyc_level >= 0);

ALTER TABLE wallets
  ADD CONSTRAINT chk_wallets_status CHECK (status IN ('ACTIVE','FROZEN','CLOSED'));

ALTER TABLE transactions
  ADD CONSTRAINT chk_tx_type CHECK (tx_type IN ('TOPUP','FARE_PAYMENT','WITHDRAWAL','TRANSFER','FEE','REFUND')),
  ADD CONSTRAINT chk_tx_status CHECK (status IN ('PENDING','COMPLETED','FAILED','REVERSED')),
  ADD CONSTRAINT chk_tx_fee_nonneg CHECK (fee_kc >= 0);

ALTER TABLE payment_codes
  ADD CONSTRAINT chk_codes_status CHECK (status IN ('PENDING','REDEEMED','EXPIRED','CANCELLED')),
  ADD CONSTRAINT chk_codes_amount CHECK (amount_kc > 0),
  ADD CONSTRAINT chk_codes_fee CHECK (fee_kc >= 0);

ALTER TABLE payment_requests
  ADD CONSTRAINT chk_preq_status CHECK (status IN ('PENDING','APPROVED','DECLINED','EXPIRED','CANCELLED')),
  ADD CONSTRAINT chk_preq_amount CHECK (amount_kc > 0);

ALTER TABLE agents
  ADD CONSTRAINT chk_agents_status CHECK (status IN ('ACTIVE','SUSPENDED')),
  ADD CONSTRAINT chk_agents_float CHECK (float_limit_kc >= 0 AND float_balance_kc >= 0);

ALTER TABLE audit_log
  ADD CONSTRAINT chk_audit_outcome CHECK (outcome IS NULL OR outcome IN ('SUCCESS','FAILURE'));
