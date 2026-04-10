CREATE TABLE merchant_onboarding (
  onboarding_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id               UUID NOT NULL UNIQUE REFERENCES users(user_id),
  business_name         VARCHAR(200) NOT NULL,
  trading_name          VARCHAR(200) NOT NULL,
  address_line1         VARCHAR(300) NOT NULL,
  city                  VARCHAR(150) NOT NULL,
  country               VARCHAR(80) NOT NULL,
  id_number             VARCHAR(80) NOT NULL,
  id_document_path      VARCHAR(500) NOT NULL,
  proof_of_address_path VARCHAR(500) NOT NULL,
  created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchant_onboarding_user ON merchant_onboarding(user_id);

