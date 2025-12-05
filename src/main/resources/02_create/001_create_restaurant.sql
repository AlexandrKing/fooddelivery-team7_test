-- Создание таблицы ресторанов
CREATE TABLE IF NOT EXISTS restaurant (
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