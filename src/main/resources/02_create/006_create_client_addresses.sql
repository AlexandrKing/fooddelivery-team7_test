-- Таблица адресов доставки
CREATE TABLE IF NOT EXISTS client_addresses (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES client_users(id),
    label VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    apartment VARCHAR(20),
    entrance VARCHAR(10),
    floor VARCHAR(10),
    comment TEXT,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);