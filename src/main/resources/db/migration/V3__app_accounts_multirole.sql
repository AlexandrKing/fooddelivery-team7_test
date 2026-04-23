CREATE TABLE IF NOT EXISTS app_accounts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    linked_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    linked_restaurant_id BIGINT REFERENCES restaurants(id) ON DELETE SET NULL,
    linked_courier_id BIGINT REFERENCES courier_users(id) ON DELETE SET NULL,
    linked_admin_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_accounts_role CHECK (role IN ('USER', 'ADMIN', 'COURIER', 'RESTAURANT'))
);

CREATE INDEX IF NOT EXISTS idx_app_accounts_role ON app_accounts(role);
CREATE INDEX IF NOT EXISTS idx_app_accounts_user ON app_accounts(linked_user_id);
CREATE INDEX IF NOT EXISTS idx_app_accounts_restaurant ON app_accounts(linked_restaurant_id);
CREATE INDEX IF NOT EXISTS idx_app_accounts_courier ON app_accounts(linked_courier_id);
CREATE INDEX IF NOT EXISTS idx_app_accounts_admin ON app_accounts(linked_admin_id);

INSERT INTO app_accounts (email, password_hash, role, linked_user_id, is_active, created_at, updated_at)
SELECT u.email, u.password, 'USER', u.id, COALESCE(u.is_active, TRUE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_accounts (email, password_hash, role, linked_admin_id, is_active, created_at, updated_at)
SELECT a.email, a.password_hash, 'ADMIN', a.id, COALESCE(a.is_active, TRUE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM admin_users a
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_accounts (email, password_hash, role, linked_courier_id, is_active, created_at, updated_at)
SELECT c.email, c.password, 'COURIER', c.id, COALESCE(c.is_active, TRUE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM courier_users c
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_accounts (email, password_hash, role, linked_restaurant_id, is_active, created_at, updated_at)
SELECT r.email, r.password, 'RESTAURANT', r.id, COALESCE(r.is_active, TRUE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM restaurants r
ON CONFLICT (email) DO NOTHING;

