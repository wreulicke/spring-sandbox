CREATE TABLE products (
    id BIGINT not null AUTO_INCREMENT PRIMARY KEY,
	description VARCHAR(128) NOT NULL
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;


CREATE TABLE product_stocks (
	product_id BIGINT not null PRIMARY KEY,
	count BIGINT NOT NULL
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
