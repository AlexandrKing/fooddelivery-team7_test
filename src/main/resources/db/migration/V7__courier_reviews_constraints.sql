-- Courier reviews use existing table `reviews` (V1). Enforce one row per order and rating range.

ALTER TABLE reviews DROP CONSTRAINT IF EXISTS chk_reviews_courier_rating;
ALTER TABLE reviews ADD CONSTRAINT chk_reviews_courier_rating
    CHECK (courier_rating IS NULL OR (courier_rating >= 1 AND courier_rating <= 5));

ALTER TABLE reviews DROP CONSTRAINT IF EXISTS chk_reviews_restaurant_rating;
ALTER TABLE reviews ADD CONSTRAINT chk_reviews_restaurant_rating
    CHECK (restaurant_rating IS NULL OR (restaurant_rating >= 1 AND restaurant_rating <= 5));

CREATE UNIQUE INDEX IF NOT EXISTS uq_reviews_order_id ON reviews (order_id);
