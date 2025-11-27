DROP TABLE IF EXISTS dish CASCADE;
DROP TABLE IF EXISTS menu_category CASCADE;
DROP TABLE IF EXISTS restaurant CASCADE;

-- Таблица ресторанов
CREATE TABLE restaurant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(50) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    cuisine_type VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_date TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE
);

-- Таблица категорий меню
CREATE TABLE menu_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    restaurant_id BIGINT NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
);

-- Таблица блюд
CREATE TABLE dish (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    available BOOLEAN DEFAULT TRUE,
    restaurant_id BIGINT NOT NULL,
    menu_category_id BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_category_id) REFERENCES menu_category(id) ON DELETE SET NULL
);

-- Создание индексов для улучшения производительности
CREATE INDEX idx_restaurant_email ON restaurant(email);
CREATE INDEX idx_restaurant_phone ON restaurant(phone);
CREATE INDEX idx_restaurant_status ON restaurant(status);
CREATE INDEX idx_menu_category_restaurant_id ON menu_category(restaurant_id);
CREATE INDEX idx_dish_restaurant_id ON dish(restaurant_id);
CREATE INDEX idx_dish_menu_category_id ON dish(menu_category_id);
CREATE INDEX idx_dish_available ON dish(available);
