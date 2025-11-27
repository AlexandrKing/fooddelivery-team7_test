CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    apartment VARCHAR(20),
    entrance VARCHAR(10),
    floor VARCHAR(10),
    comment TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);