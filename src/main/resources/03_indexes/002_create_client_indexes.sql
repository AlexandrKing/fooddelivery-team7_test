-- Индексы для всех клиентских таблиц

-- Индексы для client_users
CREATE INDEX idx_client_users_email ON client_users(email);
CREATE INDEX idx_client_users_phone ON client_users(phone);
CREATE INDEX idx_client_users_role ON client_users(role);

-- Индексы для client_restaurants
CREATE INDEX idx_client_restaurants_email ON client_restaurants(email);
CREATE INDEX idx_client_restaurants_phone ON client_restaurants(phone);
CREATE INDEX idx_client_restaurants_cuisine ON client_restaurants(cuisine_type);
CREATE INDEX idx_client_restaurants_rating ON client_restaurants(rating);

-- Индексы для client_addresses
CREATE INDEX idx_client_addresses_user_id ON client_addresses(user_id);
CREATE INDEX idx_client_addresses_default ON client_addresses(user_id, is_default);

-- Индексы для client_menu
CREATE INDEX idx_client_menu_restaurant_id ON client_menu(restaurant_id);
CREATE INDEX idx_client_menu_category ON client_menu(category);
CREATE INDEX idx_client_menu_available ON client_menu(restaurant_id, is_available);

-- Индексы для client_carts
CREATE INDEX idx_client_carts_user_id ON client_carts(user_id);
CREATE INDEX idx_client_carts_restaurant ON client_carts(user_id, restaurant_id);

-- Индексы для client_cart_items
CREATE INDEX idx_client_cart_items_cart_id ON client_cart_items(cart_id);
CREATE INDEX idx_client_cart_items_menu_item ON client_cart_items(menu_item_id);

-- Индексы для client_orders
CREATE INDEX idx_client_orders_user_id ON client_orders(user_id);
CREATE INDEX idx_client_orders_restaurant_id ON client_orders(restaurant_id);
CREATE INDEX idx_client_orders_status ON client_orders(status);
CREATE INDEX idx_client_orders_created_at ON client_orders(created_at);

-- Индексы для client_order_items
CREATE INDEX idx_client_order_items_order_id ON client_order_items(order_id);
CREATE INDEX idx_client_order_items_menu_item ON client_order_items(menu_item_id);

-- Индексы для client_reviews
CREATE INDEX idx_client_reviews_order_id ON client_reviews(order_id);
CREATE INDEX idx_client_reviews_restaurant_id ON client_reviews(restaurant_id);
CREATE INDEX idx_client_reviews_user_id ON client_reviews(user_id);

-- Индексы для client_order_status_history
CREATE INDEX idx_client_order_status_history_order_id ON client_order_status_history(order_id);
CREATE INDEX idx_client_order_status_history_created ON client_order_status_history(order_id, created_at);