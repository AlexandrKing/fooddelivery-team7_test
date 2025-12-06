INSERT INTO admin_users (username, email, password_hash, full_name, role, created_at, last_login_at, is_active, permissions)
VALUES
('admin', 'admin@team7.com', 'admin123', 'Иван Петров', 'SUPER_ADMIN', '2024-01-15 09:00:00', '2024-03-20 14:30:00', TRUE, 'ALL_PERMISSIONS'),
('manager', 'manager@team7.com', 'manager123', 'Анна Сидорова', 'MANAGER', '2024-01-16 10:15:00', '2024-03-20 13:45:00', TRUE, 'USER_MANAGE,CONTENT_MANAGE'),
('support', 'support@team7.com', 'support123', 'Сергей Иванов', 'SUPPORT', '2024-01-18 14:00:00', '2024-03-20 10:10:00', TRUE, 'VIEW_REPORTS,ANSWER_TICKETS');