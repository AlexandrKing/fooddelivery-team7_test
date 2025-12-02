-- Таблица отзывов
CREATE TABLE IF NOT EXISTS client_reviews (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES client_orders(id),
    user_id INTEGER NOT NULL REFERENCES client_users(id),
    restaurant_id INTEGER NOT NULL REFERENCES client_restaurants(id),
    courier_id INTEGER REFERENCES client_users(id),
    restaurant_rating INTEGER CHECK (restaurant_rating >= 1 AND restaurant_rating <= 5),
    courier_rating INTEGER CHECK (courier_rating >= 1 AND courier_rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);