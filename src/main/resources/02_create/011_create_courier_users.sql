-- Создание таблицы курьеров с колонкой 'password' вместо 'password_hash'
CREATE TABLE IF NOT EXISTS courier_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Изменено с password_hash на password
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'offline',
    current_location VARCHAR(200),
    rating DECIMAL(3,2) DEFAULT 0.0,
    completed_orders INTEGER DEFAULT 0,
    balance DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    last_activity_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);