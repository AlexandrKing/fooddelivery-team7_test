-- Additional reference data for local/dev after V2/V3.
-- Does not modify applied migrations V1–V3.
--
-- 1) Demo dishes for the seed restaurant from V2 (so USER catalog → menu works on empty DB).
-- 2) Optional second restaurant + matching app_accounts row (V3 ran before this file, so new restaurants need an account row).

INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min)
SELECT r.id, 'Demo Burger', 'Демо-позиция для проверки корзины', 450.0, TRUE, 'Main', 15
FROM restaurants r
WHERE r.email = 'restaurant@test.local'
LIMIT 1;

INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min)
SELECT r.id, 'Demo Soup', 'Демо-суп', 320.0, TRUE, 'Soup', 20
FROM restaurants r
WHERE r.email = 'restaurant@test.local'
LIMIT 1;

INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min)
SELECT r.id, 'Demo Dessert', 'Демо-десерт', 280.0, TRUE, 'Dessert', 10
FROM restaurants r
WHERE r.email = 'restaurant@test.local'
LIMIT 1;

INSERT INTO restaurants (name, email, password, phone, address, cuisine_type, status, is_active)
VALUES
    (
        'Demo Bistro',
        'bistro@test.local',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '+79990000004',
        'Demo avenue 2',
        'European',
        'ACTIVE',
        TRUE
    )
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_accounts (email, password_hash, role, linked_restaurant_id, is_active, created_at, updated_at)
SELECT r.email, r.password, 'RESTAURANT', r.id, COALESCE(r.is_active, TRUE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM restaurants r
WHERE r.email = 'bistro@test.local'
  AND NOT EXISTS (SELECT 1 FROM app_accounts a WHERE a.email = r.email);

INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min)
SELECT r.id, 'Bistro Pasta', 'Демо-паста второго ресторана', 520.0, TRUE, 'Main', 25
FROM restaurants r
WHERE r.email = 'bistro@test.local'
LIMIT 1;
