INSERT INTO courier_users (id, username, email, password_hash, full_name, phone, vehicle_type, status, current_location,
                           rating, completed_orders, balance, created_at, last_login_at, last_activity_at, is_active) VALUES
(1, 'courier1', 'ivan@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Иван Курьеров', '+79161234573',
 'bicycle', 'available', '55.7558,37.6176', 4.8, 124, 12500.50, '2024-01-01 09:00:00', '2024-01-19 10:00:00', '2024-01-19 10:30:00', true),
(2, 'courier2', 'petr@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Петр Доставкин', '+79161234574',
 'car', 'busy', '55.7517,37.6178', 4.5, 89, 8900.00, '2024-01-02 10:00:00', '2024-01-19 11:00:00', '2024-01-19 11:30:00', true),
(3, 'courier3', 'semen@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Семен Быстров', '+79163456789',
 'scooter', 'available', '55.7490,37.6200', 4.9, 156, 18200.75, '2024-01-03 11:00:00', '2024-01-19 12:00:00', '2024-01-19 12:30:00', true),
(4, 'courier4', 'alex@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Алексей Надежный', '+79164567890',
 'bicycle', 'offline', NULL, 4.3, 67, 5400.25, '2024-01-04 12:00:00', '2024-01-18 13:00:00', '2024-01-18 18:00:00', true),
(5, 'courier5', 'anna@delivery.com', '$2a$10$Y9zQr8Pv7kK4L3W2N0Xx.vX9K7zO9U5C3D6E8F2G4H6J8L0N2P4R6T8U0X2Z', 'Анна Скорова', '+79165678901',
 'car', 'available', '55.7460,37.6250', 4.7, 112, 13400.00, '2024-01-05 13:00:00', '2024-01-19 14:00:00', '2024-01-19 14:30:00', true);