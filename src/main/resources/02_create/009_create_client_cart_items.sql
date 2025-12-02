-- Таблица элементов корзины
CREATE TABLE IF NOT EXISTS client_cart_items (
    id SERIAL PRIMARY KEY,
    cart_id INTEGER NOT NULL REFERENCES client_carts(id) ON DELETE CASCADE,
    menu_item_id INTEGER NOT NULL REFERENCES client_menu(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);