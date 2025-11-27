CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rating DECIMAL(3,2) DEFAULT 0.0,
    delivery_time INTEGER,
    min_order_amount DECIMAL(10,2) DEFAULT 0.0,
    is_active BOOLEAN DEFAULT true
);