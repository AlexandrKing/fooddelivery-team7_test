-- Additional demo restaurants, app_accounts (RESTAURANT), and dishes.
-- Does not modify V1–V4. No fixed primary keys; idempotent on email.

INSERT INTO restaurants (name, email, password, phone, address, cuisine_type, description, status, is_active)
VALUES
    (
        'Mario Pizza',
        'mario.seed@local',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '+79990000010',
        'ул. Пушкина, 10',
        'ITALIAN',
        'Пицца и паста (демо-каталог)',
        'ACTIVE',
        TRUE
    ),
    (
        'Tokyo Sushi',
        'tokyo.seed@local',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '+79990000011',
        'ул. Лермонтова, 25',
        'JAPANESE',
        'Суши и роллы (демо-каталог)',
        'ACTIVE',
        TRUE
    ),
    (
        'Burger Corner',
        'burger.seed@local',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '+79990000012',
        'ул. Гагарина, 5',
        'FAST_FOOD',
        'Бургеры и гарниры (демо-каталог)',
        'ACTIVE',
        TRUE
    ),
    (
        'Coffee Lab',
        'coffee.seed@local',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '+79990000013',
        'пр. Ленина, 30',
        'COFFEE',
        'Кофе и десерты (демо-каталог)',
        'ACTIVE',
        TRUE
    )
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_accounts (email, password_hash, role, linked_restaurant_id, is_active, created_at, updated_at)
SELECT r.email, r.password, 'RESTAURANT', r.id, COALESCE(r.is_active, TRUE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM restaurants r
WHERE r.email IN (
    'mario.seed@local',
    'tokyo.seed@local',
    'burger.seed@local',
    'coffee.seed@local'
)
  AND NOT EXISTS (SELECT 1 FROM app_accounts a WHERE a.email = r.email);

-- Mario Pizza — 3 блюда
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Маргарита', 'Томаты и моцарелла', 450.0, TRUE, 'PIZZA', 20, 850, NULL
FROM restaurants r WHERE r.email = 'mario.seed@local';
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Пепперони', 'Пицца с пепперони', 550.0, TRUE, 'PIZZA', 20, 950, NULL
FROM restaurants r WHERE r.email = 'mario.seed@local';
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Карбонара', 'Паста с беконом', 380.0, TRUE, 'PASTA', 15, 700, NULL
FROM restaurants r WHERE r.email = 'mario.seed@local';

-- Tokyo Sushi — 3 блюда
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Филадельфия', 'Лосось и сыр', 520.0, TRUE, 'SUSHI', 12, 320, NULL
FROM restaurants r WHERE r.email = 'tokyo.seed@local';
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Калифорния', 'Краб и авокадо', 480.0, TRUE, 'SUSHI', 12, 280, NULL
FROM restaurants r WHERE r.email = 'tokyo.seed@local';
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Рамен', 'Суп с лапшой', 350.0, TRUE, 'SOUP', 15, 450, NULL
FROM restaurants r WHERE r.email = 'tokyo.seed@local';

-- Burger Corner — 2 блюда
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Чизбургер', 'Говядина и сыр', 290.0, TRUE, 'BURGER', 12, 450, NULL
FROM restaurants r WHERE r.email = 'burger.seed@local';
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Картофель фри', 'Порция фри', 120.0, TRUE, 'SNACK', 8, 350, NULL
FROM restaurants r WHERE r.email = 'burger.seed@local';

-- Coffee Lab — 2 блюда
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Капучино', 'Кофе с молоком', 220.0, TRUE, 'COFFEE', 5, 120, NULL
FROM restaurants r WHERE r.email = 'coffee.seed@local';
INSERT INTO dishes (restaurant_id, name, description, price, is_available, category, preparation_time_min, calories, image_url)
SELECT r.id, 'Чизкейк', 'Десерт', 280.0, TRUE, 'DESSERT', 3, 350, NULL
FROM restaurants r WHERE r.email = 'coffee.seed@local';
