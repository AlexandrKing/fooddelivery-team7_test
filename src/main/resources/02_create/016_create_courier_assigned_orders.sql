CREATE TABLE IF NOT EXISTS courier_assigned_orders (
    id SERIAL PRIMARY KEY,
    courier_id INTEGER REFERENCES courier_users(id) ON DELETE CASCADE,
    order_id INTEGER REFERENCES client_orders(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'assigned',
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    travel_distance_km DECIMAL(5,2),
    delivery_notes TEXT,
    CONSTRAINT unique_courier_order UNIQUE (order_id)
);