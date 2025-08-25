CREATE TABLE IF NOT EXISTS coupon (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) DEFAULT NULL,
  discount_type varchar(20) NOT NULL,
  discount_amount bigint NOT NULL,
  issuable tinyint(1) NOT NULL DEFAULT '1',
  issue_limit bigint NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS order_item (
  id bigint NOT NULL AUTO_INCREMENT,
  order_id bigint DEFAULT NULL,
  product_id bigint NOT NULL,
  product_name varchar(255) NOT NULL,
  price bigint NOT NULL,
  quantity int NOT NULL,
  status varchar(20) NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS orders (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint DEFAULT NULL,
  status varchar(20) NOT NULL,
  total_price bigint NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS payment (
  id bigint NOT NULL AUTO_INCREMENT,
  order_id bigint DEFAULT NULL,
  paid_amount bigint NOT NULL,
  discount_amount bigint NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS product (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  price bigint NOT NULL,
  stock int NOT NULL,
  deleted tinyint(1) NOT NULL DEFAULT '0',
  version bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  point bigint NOT NULL DEFAULT '0',
  version bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_coupon (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  coupon_id bigint NOT NULL,
  discount_type varchar(20) NOT NULL,
  discount_amount bigint NOT NULL,
  status varchar(20) NOT NULL,
  payment_id bigint DEFAULT NULL,
  used_at datetime DEFAULT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_point_history (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  type varchar(20) NOT NULL,
  amount bigint NOT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);