-- Создание таблицы блюд
CREATE TABLE dish (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    available BOOLEAN DEFAULT TRUE,
    restaurant_id BIGINT NOT NULL,
    menu_category_id BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_category_id) REFERENCES menu_category(id) ON DELETE SET NULL
);