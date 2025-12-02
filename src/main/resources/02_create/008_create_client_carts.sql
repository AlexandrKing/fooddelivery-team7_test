-- Таблица корзин
CREATE TABLE IF NOT EXISTS client_carts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES client_users(id),
    restaurant_id INTEGER NOT NULL REFERENCES client_restaurants(id),
    total_amount DECIMAL(10,2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);