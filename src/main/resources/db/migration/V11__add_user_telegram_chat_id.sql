ALTER TABLE users
ADD COLUMN IF NOT EXISTS telegram_chat_id VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_users_telegram_chat_id
ON users(telegram_chat_id)
WHERE telegram_chat_id IS NOT NULL;