-- Удаление клиентских таблиц в правильном порядке
DROP TABLE IF EXISTS client_order_status_history CASCADE;
DROP TABLE IF EXISTS client_reviews CASCADE;
DROP TABLE IF EXISTS client_order_items CASCADE;
DROP TABLE IF EXISTS client_orders CASCADE;
DROP TABLE IF EXISTS client_cart_items CASCADE;
DROP TABLE IF EXISTS client_carts CASCADE;
DROP TABLE IF EXISTS client_menu CASCADE;
DROP TABLE IF EXISTS client_addresses CASCADE;
DROP TABLE IF EXISTS client_restaurants CASCADE;
DROP TABLE IF EXISTS client_users CASCADE;