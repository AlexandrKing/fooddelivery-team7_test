CREATE TABLE IF NOT EXISTS dishes (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    is_available BOOLEAN DEFAULT TRUE,
    menu_category_id BIGINT REFERENCES menu_categories(id) ON DELETE SET NULL,
    available_quantity INTEGER,
    preparation_time_min INTEGER DEFAULT 15,
    calories INTEGER,
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_spicy BOOLEAN DEFAULT FALSE,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT positive_price CHECK (price > 0),
    CONSTRAINT positive_prep_time CHECK (preparation_time_min > 0),
    CONSTRAINT non_negative_quantity CHECK (available_quantity IS NULL OR available_quantity >= 0),
    CONSTRAINT unique_dish_name UNIQUE(restaurant_id, name)
);