-- Таблица меню ресторанов
CREATE TABLE IF NOT EXISTS client_menu (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES client_restaurants(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);