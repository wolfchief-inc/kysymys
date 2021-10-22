CREATE TABLE unread_whats_news (
  id VARCHAR(255) NOT NULL,
  whats_new_id VARCHAR(255) NOT NULL,
  CONSTRAINT pk_unread_whats_news PRIMARY KEY (id)
);

ALTER TABLE unread_whats_news ADD CONSTRAINT FK_UNREAD_WHATS_NEWS_ON_WHATS_NEWS FOREIGN KEY (whats_new_id) REFERENCES whats_news (id);

