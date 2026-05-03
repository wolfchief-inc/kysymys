-- Add user_id column to unread_whats_news so per-user lookup is efficient
-- (legacy schema lacked this; queries had to join through whats_news).
ALTER TABLE unread_whats_news ADD COLUMN user_id VARCHAR(21);

ALTER TABLE unread_whats_news ADD CONSTRAINT FK_UNREAD_WHATS_NEWS_ON_USER
  FOREIGN KEY (user_id) REFERENCES users (id);
