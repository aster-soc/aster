CREATE TABLE IF NOT EXISTS taken_username (id VARCHAR(150) PRIMARY KEY, username VARCHAR(300) NOT NULL);
ALTER TABLE taken_username ADD CONSTRAINT taken_username_id_unique UNIQUE (id);
ALTER TABLE taken_username ADD CONSTRAINT taken_username_username_unique UNIQUE (username);
