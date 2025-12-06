INSERT INTO reviews (order_id, user_id, restaurant_id, courier_id, restaurant_rating, courier_rating, comment, created_at) VALUES
(1, 1, 1, 1, 5, 5, 'Отличная пицца! Быстрая доставка. Курьер был очень вежлив.', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(2, 2, 2, 2, 4, 5, 'Суши свежие, но доставка немного задержалась. Курьер молодец!', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(3, 3, 3, 3, 5, 4, 'Бургер просто огонь! Курьер привез быстро, но немного помял упаковку.', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(4, 4, 4, 4, 3, 5, 'Еда обычная, но курьер просто супер! Очень вежливый и аккуратный.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(5, 7, 5, 5, 4, 4, 'Хорошая китайская кухня. Доставка в срок. Все понравилось.', CURRENT_TIMESTAMP - INTERVAL '1 day')