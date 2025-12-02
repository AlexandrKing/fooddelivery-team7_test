-- Таблица заказов
CREATE TABLE IF NOT EXISTS client_orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES client_users(id),
    restaurant_id INTEGER NOT NULL REFERENCES client_restaurants(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACCEPTED', 'COOKING', 'DELIVERING', 'DELIVERED', 'CANCELLED')),
    delivery_address TEXT NOT NULL,
    delivery_type VARCHAR(20) NOT NULL CHECK (delivery_type IN ('DELIVERY', 'PICKUP')),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CARD', 'CASH')),
    preferred_delivery_time TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);