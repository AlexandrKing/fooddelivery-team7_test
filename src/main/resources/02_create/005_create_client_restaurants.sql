-- Таблица ресторанов для клиентского модуля
CREATE TABLE IF NOT EXISTS client_restaurants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    cuisine_type VARCHAR(50) NOT NULL,
    rating DECIMAL(3,2) DEFAULT 0.0,
    delivery_time INTEGER DEFAULT 30,
    min_order_amount DECIMAL(10,2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);