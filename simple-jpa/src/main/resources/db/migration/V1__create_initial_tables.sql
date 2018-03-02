CREATE TABLE users (
    id BIGINT PRIMARY KEY,
	username VARCHAR(128) UNIQUE NOT NULL,
	password VARCHAR(128) NOT NULL,
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;


CREATE TABLE user_authorities (
	username VARCHAR(128) PRIMARY KEY,
	authorities VARCHAR(128) NOT NULL,
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
