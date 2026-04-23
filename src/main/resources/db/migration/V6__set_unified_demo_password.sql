-- Set one known bcrypt hash for all demo accounts (plain password: Demo123!).
-- Spring Security loads credentials from app_accounts; domain tables are kept in sync for consistency.
-- Hash generated with bcrypt cost 10 (compatible with Spring BCryptPasswordEncoder).

UPDATE app_accounts
SET password_hash = '$2b$10$sqklXIbggRcGQNbeOVqxne1YAyAuyz.iV6TLamlSjsF09SgXqZasW',
    updated_at    = CURRENT_TIMESTAMP
WHERE email IN (
    'user@test.local',
    'admin@test.local',
    'courier@test.local',
    'restaurant@test.local',
    'bistro@test.local',
    'mario.seed@local',
    'tokyo.seed@local',
    'burger.seed@local',
    'coffee.seed@local'
);

UPDATE users
SET password = '$2b$10$sqklXIbggRcGQNbeOVqxne1YAyAuyz.iV6TLamlSjsF09SgXqZasW'
WHERE email = 'user@test.local';

UPDATE admin_users
SET password_hash = '$2b$10$sqklXIbggRcGQNbeOVqxne1YAyAuyz.iV6TLamlSjsF09SgXqZasW'
WHERE email = 'admin@test.local';

UPDATE courier_users
SET password = '$2b$10$sqklXIbggRcGQNbeOVqxne1YAyAuyz.iV6TLamlSjsF09SgXqZasW'
WHERE email = 'courier@test.local';

UPDATE restaurants
SET password = '$2b$10$sqklXIbggRcGQNbeOVqxne1YAyAuyz.iV6TLamlSjsF09SgXqZasW'
WHERE email IN (
    'restaurant@test.local',
    'bistro@test.local',
    'mario.seed@local',
    'tokyo.seed@local',
    'burger.seed@local',
    'coffee.seed@local'
);
