-- 10 пользователей
INSERT INTO client_users (name, email, phone, password, role) VALUES
('Иван Петров', 'ivan@mail.ru', '+79161234567', 'password123', 'CLIENT'),
('Мария Сидорова', 'maria@mail.ru', '+79161234568', 'password125', 'CLIENT'),
('Сергей Иванов', 'sergei@mail.ru', '+79161234569', 'password126', 'CLIENT'),
('Анна Козлова', 'anna@mail.ru', '+79161234570', 'password127', 'CLIENT'),
('Дмитрий Смирнов', 'dmitry@mail.ru', '+79161234571', 'password128', 'CLIENT'),
('Ольга Новикова', 'olga@mail.ru', '+79161234572', 'password129', 'CLIENT'),
('Алексей Курьеров', 'courier1@mail.ru', '+79161234573', 'password130', 'COURIER'),
('Петр Доставкин', 'courier2@mail.ru', '+79161234574', 'password131', 'COURIER'),
('Суши Мастер', 'sushi@rest.com', '+78001002030', 'rest132', 'RESTAURANT'),
('Пицца Темпо', 'pizza@rest.com', '+78003004050', 'rest133', 'RESTAURANT');