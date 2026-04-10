-- Rename role value PASSENGER -> CLIENT (with temporary backward compatibility)

ALTER TABLE users
  DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
  ADD CONSTRAINT chk_users_role
    CHECK (role IN ('CLIENT','PASSENGER','MERCHANT','CONDUCTOR','AGENT','ADMIN','FLEET_OWNER'));

UPDATE users
SET role = 'CLIENT'
WHERE role = 'PASSENGER';

