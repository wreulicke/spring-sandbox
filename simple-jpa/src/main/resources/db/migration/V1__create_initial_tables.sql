CREATE TABLE users (
    id BIGINT not null AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(128) NOT NULL,
	password VARCHAR(128) NOT NULL,
	UNIQUE INDEX `username_UNIQUE` (`username`)
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;


CREATE TABLE user_authorities (
	username VARCHAR(128) PRIMARY KEY,
	authorities VARCHAR(128) NOT NULL
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
