CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACCEPTED', 'COOKING', 'DELIVERING', 'DELIVERED', 'CANCELLED')),
    delivery_address TEXT NOT NULL,
    delivery_type VARCHAR(20) NOT NULL CHECK (delivery_type IN ('DELIVERY', 'PICKUP')),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CARD', 'CASH', 'ONLINE')),
    preferred_delivery_time TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);