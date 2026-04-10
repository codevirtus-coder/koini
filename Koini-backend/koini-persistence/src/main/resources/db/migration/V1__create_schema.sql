CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
  user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phone           VARCHAR(20) NOT NULL UNIQUE,
  full_name       VARCHAR(150),
  password_hash   VARCHAR(255) NOT NULL,
  pin_hash        VARCHAR(255),
  role            VARCHAR(30) NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  pin_attempts    SMALLINT NOT NULL DEFAULT 0,
  pin_locked_until TIMESTAMP,
  national_id     VARCHAR(50),
  kyc_level       SMALLINT NOT NULL DEFAULT 0,
  last_login      TIMESTAMP,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

CREATE TABLE wallets (
  wallet_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES users(user_id),
  balance_kc      BIGINT NOT NULL DEFAULT 0 CHECK (balance_kc >= 0),
  status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  org_id          UUID,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT unique_user_wallet UNIQUE (user_id)
);

CREATE INDEX idx_wallets_user ON wallets(user_id);

CREATE TABLE transactions (
  tx_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tx_type         VARCHAR(30) NOT NULL,
  from_wallet_id  UUID REFERENCES wallets(wallet_id),
  to_wallet_id    UUID REFERENCES wallets(wallet_id),
  amount_kc       BIGINT NOT NULL CHECK (amount_kc > 0),
  fee_kc          BIGINT NOT NULL DEFAULT 0,
  status          VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
  reference       VARCHAR(100) NOT NULL UNIQUE,
  description     VARCHAR(255),
  initiated_by    UUID REFERENCES users(user_id),
  route_id        UUID,
  metadata        JSONB,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tx_from_wallet ON transactions(from_wallet_id, created_at DESC);
CREATE INDEX idx_tx_to_wallet   ON transactions(to_wallet_id, created_at DESC);
CREATE INDEX idx_tx_reference   ON transactions(reference);
CREATE INDEX idx_tx_type        ON transactions(tx_type, created_at DESC);
CREATE INDEX idx_tx_created     ON transactions(created_at DESC);

CREATE TABLE payment_codes (
  code_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code_hash       VARCHAR(255) NOT NULL UNIQUE,
  holder_id       UUID NOT NULL REFERENCES users(user_id),
  amount_kc       BIGINT NOT NULL,
  fee_kc          BIGINT NOT NULL DEFAULT 0,
  status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  redeemed_by     UUID REFERENCES users(user_id),
  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at      TIMESTAMP NOT NULL,
  redeemed_at     TIMESTAMP
);

CREATE INDEX idx_codes_holder  ON payment_codes(holder_id, created_at DESC);
CREATE INDEX idx_codes_status  ON payment_codes(status, expires_at);

CREATE TABLE payment_requests (
  request_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conductor_id    UUID NOT NULL REFERENCES users(user_id),
  passenger_id    UUID REFERENCES users(user_id),
  amount_kc       BIGINT NOT NULL,
  route_id        UUID,
  status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at      TIMESTAMP NOT NULL,
  responded_at    TIMESTAMP
);

CREATE INDEX idx_preq_conductor ON payment_requests(conductor_id, created_at DESC);
CREATE INDEX idx_preq_passenger ON payment_requests(passenger_id, created_at DESC);
CREATE INDEX idx_preq_status    ON payment_requests(status, expires_at);

CREATE TABLE agents (
  agent_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL UNIQUE REFERENCES users(user_id),
  business_name   VARCHAR(200),
  location        VARCHAR(300),
  float_limit_kc  BIGINT NOT NULL DEFAULT 100000,
  float_balance_kc BIGINT NOT NULL DEFAULT 0,
  cash_held_usd   NUMERIC(10,2) NOT NULL DEFAULT 0,
  status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE routes (
  route_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name            VARCHAR(200) NOT NULL,
  origin          VARCHAR(150) NOT NULL,
  destination     VARCHAR(150) NOT NULL,
  fare_kc         BIGINT NOT NULL,
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_by      UUID REFERENCES users(user_id),
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_log (
  log_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_id        UUID,
  actor_type      VARCHAR(30),
  action          VARCHAR(100) NOT NULL,
  entity_type     VARCHAR(50),
  entity_id       VARCHAR(100),
  ip_address      VARCHAR(45),
  user_agent      VARCHAR(500),
  old_value       JSONB,
  new_value       JSONB,
  outcome         VARCHAR(20),
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor   ON audit_log(actor_id, created_at DESC);
CREATE INDEX idx_audit_action  ON audit_log(action, created_at DESC);
CREATE INDEX idx_audit_created ON audit_log(created_at DESC);

CREATE TABLE refresh_tokens (
  token_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES users(user_id),
  token_hash      VARCHAR(255) NOT NULL UNIQUE,
  device_id       VARCHAR(255),
  ip_address      VARCHAR(45),
  expires_at      TIMESTAMP NOT NULL,
  revoked         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_user   ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token  ON refresh_tokens(token_hash);
