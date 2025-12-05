-- Вставка тестовых курьеров (пароль: courier123)
INSERT INTO courier_users (username, email, password_hash, full_name, phone, vehicle_type, status, rating, completed_orders, balance, is_active) VALUES
('courier1', 'ivan@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Иван Курьеров', '+79161234567', 'bicycle', 'available', 4.8, 124, 12500.50, TRUE),
('courier2', 'petr@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Петр Доставкин', '+79162345678', 'car', 'busy', 4.5, 89, 8900.00, TRUE),
('courier3', 'semen@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Семен Быстров', '+79163456789', 'scooter', 'available', 4.9, 156, 18200.75, TRUE),
('courier4', 'alex@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Алексей Надежный', '+79164567890', 'bicycle', 'offline', 4.3, 67, 5400.25, TRUE),
('courier5', 'anna@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Анна Скорова', '+79165678901', 'car', 'available', 4.7, 112, 13400.00, TRUE);
