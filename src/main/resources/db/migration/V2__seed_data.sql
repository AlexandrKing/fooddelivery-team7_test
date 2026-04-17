-- Minimal seed data for local/dev startup.
-- Uses ON CONFLICT to stay idempotent.

INSERT INTO users (full_name, email, password, phone)
VALUES
    ('Test User', 'user@test.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+79990000001')
ON CONFLICT (email) DO NOTHING;

INSERT INTO restaurants (name, email, password, phone, address, cuisine_type, status, is_active)
VALUES
    ('Demo Restaurant', 'restaurant@test.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+79990000002', 'Demo street 1', 'Mixed', 'ACTIVE', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO admin_users (username, email, password_hash, full_name, role, permissions, is_active)
VALUES
    ('admin', 'admin@test.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Admin', 'SUPER_ADMIN', 'ALL', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO courier_users (username, email, password, full_name, phone, vehicle_type, status, is_active)
VALUES
    ('courier', 'courier@test.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo Courier', '+79990000003', 'bike', 'online', TRUE)
ON CONFLICT (username) DO NOTHING;

