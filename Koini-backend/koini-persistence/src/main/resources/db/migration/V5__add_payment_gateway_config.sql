CREATE TABLE IF NOT EXISTS payment_gateway_config (
  provider            VARCHAR(50) PRIMARY KEY,
  integration_key_enc VARCHAR(1000) NOT NULL,
  encryption_key_enc  VARCHAR(1000) NOT NULL,
  created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

