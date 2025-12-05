-- Вставка тестовых администраторов (пароль: admin123)
INSERT INTO admin_users (username, email, password_hash, full_name, role, is_active) VALUES
('admin', 'admin@system.com', '$2a$10$X8zQr7Pv6jK3L2V1M9Yw.uWQJ6zN8T4B2C5D7E9F1G3H5J7L9N1P3R5T7V9X0Z', 'Главный Администратор', 'super_admin', TRUE),
('manager', 'manager@system.com', '$2a$10$X8zQr7Pv6jK3L2V1M9Yw.uWQJ6zN8T4B2C5D7E9F1G3H5J7L9N1P3R5T7V9X0Z', 'Менеджер Системы', 'manager', TRUE),
('support', 'support@system.com', '$2a$10$X8zQr7Pv6jK3L2V1M9Yw.uWQJ6zN8T4B2C5D7E9F1G3H5J7L9N1P3R5T7V9X0Z', 'Служба Поддержки', 'support', TRUE);