-- Создание индексов для улучшения производительности
CREATE INDEX idx_restaurant_email ON restaurant(email);
CREATE INDEX idx_restaurant_phone ON restaurant(phone);
CREATE INDEX idx_restaurant_status ON restaurant(status);
CREATE INDEX idx_menu_category_restaurant_id ON menu_category(restaurant_id);
CREATE INDEX idx_dish_restaurant_id ON dish(restaurant_id);
CREATE INDEX idx_dish_menu_category_id ON dish(menu_category_id);
CREATE INDEX idx_dish_available ON dish(available);