CREATE TABLE IF NOT EXISTS courier_transactions (
    id BIGSERIAL PRIMARY KEY,
    courier_id BIGINT NOT NULL REFERENCES courier_users(id) ON DELETE CASCADE,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_courier_transactions_order
    ON courier_transactions(order_id);

CREATE INDEX IF NOT EXISTS idx_courier_transactions_courier
    ON courier_transactions(courier_id);
