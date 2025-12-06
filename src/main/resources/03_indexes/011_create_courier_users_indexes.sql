CREATE INDEX IF NOT EXISTS idx_courier_users_email ON courier_users(email);
CREATE INDEX IF NOT EXISTS idx_courier_users_phone ON courier_users(phone);
CREATE INDEX IF NOT EXISTS idx_courier_users_status ON courier_users(status);
CREATE INDEX IF NOT EXISTS idx_courier_users_active ON courier_users(is_active) WHERE is_active = TRUE;