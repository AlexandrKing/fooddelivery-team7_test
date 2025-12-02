-- Таблица элементов заказа
CREATE TABLE IF NOT EXISTS client_order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES client_orders(id) ON DELETE CASCADE,
    menu_item_id INTEGER NOT NULL REFERENCES client_menu(id),
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);