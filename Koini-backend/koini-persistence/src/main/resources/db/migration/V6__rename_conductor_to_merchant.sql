-- Rename role value CONDUCTOR -> MERCHANT (with temporary backward compatibility)

ALTER TABLE users
  DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
  ADD CONSTRAINT chk_users_role
    CHECK (role IN ('PASSENGER','MERCHANT','CONDUCTOR','AGENT','ADMIN','FLEET_OWNER'));

UPDATE users
SET role = 'MERCHANT'
WHERE role = 'CONDUCTOR';

