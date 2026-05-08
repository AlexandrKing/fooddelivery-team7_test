WITH seed_coordinates(email, name, latitude, longitude) AS (
    VALUES
        ('restaurant@test.local', 'Demo Restaurant', 55.751574, 37.573856),
        ('bistro@test.local', 'Demo Bistro', 55.755800, 37.617300),
        ('mario.seed@local', 'Mario Pizza', 55.759900, 37.606600),
        ('tokyo.seed@local', 'Tokyo Sushi', 55.768000, 37.595000),
        ('burger.seed@local', 'Burger Corner', 55.700000, 37.580000),
        ('coffee.seed@local', 'Coffee Lab', 55.710000, 37.570000)
)
UPDATE restaurants r
SET latitude = s.latitude,
    longitude = s.longitude,
    updated_at = CURRENT_TIMESTAMP
FROM seed_coordinates s
WHERE r.email = s.email
  AND r.name = s.name
  AND (r.latitude IS NULL OR r.longitude IS NULL);
