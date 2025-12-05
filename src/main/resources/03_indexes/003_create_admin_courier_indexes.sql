-- Индексы для администраторов
CREATE INDEX IF NOT EXISTS idx_admin_users_username ON admin_users(username);
CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users(email);
CREATE INDEX IF NOT EXISTS idx_admin_users_is_active ON admin_users(is_active);

-- Индексы для курьеров
CREATE INDEX IF NOT EXISTS idx_courier_users_username ON courier_users(username);
CREATE INDEX IF NOT EXISTS idx_courier_users_email ON courier_users(email);
CREATE INDEX IF NOT EXISTS idx_courier_users_status ON courier_users(status);
CREATE INDEX IF NOT EXISTS idx_courier_users_is_active ON courier_users(is_active);
CREATE INDEX IF NOT EXISTS idx_courier_users_rating ON courier_users(rating DESC);

-- Индексы для назначенных заказов
CREATE INDEX IF NOT EXISTS idx_courier_assigned_orders_courier_id ON courier_assigned_orders(courier_id);
CREATE INDEX IF NOT EXISTS idx_courier_assigned_orders_order_id ON courier_assigned_orders(order_id);
CREATE INDEX IF NOT EXISTS idx_courier_assigned_orders_status ON courier_assigned_orders(status);
CREATE INDEX IF NOT EXISTS idx_courier_assigned_orders_assigned_at ON courier_assigned_orders(assigned_at DESC);