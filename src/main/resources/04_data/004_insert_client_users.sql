-- 10 пользователей
INSERT INTO client_users (name, email, phone, password, role) VALUES
('Иван Петров', 'ivan@mail.ru', '+79161234567', 'password123', 'CLIENT'),
('Мария Сидорова', 'maria@mail.ru', '+79161234568', 'password123', 'CLIENT'),
('Сергей Иванов', 'sergei@mail.ru', '+79161234569', 'password123', 'CLIENT'),
('Анна Козлова', 'anna@mail.ru', '+79161234570', 'password123', 'CLIENT'),
('Дмитрий Смирнов', 'dmitry@mail.ru', '+79161234571', 'password123', 'CLIENT'),
('Ольга Новикова', 'olga@mail.ru', '+79161234572', 'password123', 'CLIENT'),
('Алексей Курьеров', 'courier1@mail.ru', '+79161234573', 'password123', 'COURIER'),
('Петр Доставкин', 'courier2@mail.ru', '+79161234574', 'password123', 'COURIER'),
('Суши Мастер', 'sushi@rest.com', '+78001002030', 'rest123', 'RESTAURANT'),
('Пицца Темпо', 'pizza@rest.com', '+78003004050', 'rest123', 'RESTAURANT');