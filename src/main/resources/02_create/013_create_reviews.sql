CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    courier_id BIGINT REFERENCES courier_users(id) ON DELETE SET NULL,
    restaurant_rating INTEGER CHECK (restaurant_rating >= 1 AND restaurant_rating <= 5),
    courier_rating INTEGER CHECK (courier_rating >= 1 AND courier_rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);