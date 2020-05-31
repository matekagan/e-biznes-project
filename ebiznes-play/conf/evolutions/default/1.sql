# --- !Ups

CREATE TABLE category
(
    "id"   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL
);

CREATE TABLE product
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"        VARCHAR NOT NULL,
    "description" TEXT    NOT NULL,
    "category"    INT     NOT NULL,
    "price"       INT     NOT NULL,
    constraint category_fk FOREIGN KEY (category) references category (id) on delete cascade
);

create table opinions
(
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    product   INTEGER   NOT NULL,
    rating    INTEGER   NOT NULL,
    comment   VARCHAR,
    timestamp TIMESTAMP NOT NULL,
    user      INTEGER   NOT NULL,
    CONSTRAINT product_fk FOREIGN KEY (product) references product (id) on delete cascade,
    CONSTRAINT user_fk FOREIGN KEY (user) references users (id) on delete cascade
);

create TABLE discounts
(
    id       INTEGER NOT NULL PRIMARY KEY,
    discount INTEGER NOT NULL,
    constraint product_fk foreign key (id) references product (id) on delete cascade
);

create TABLE orders
(
    id           INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    created_date TIMESTAMP NOT NULL,
    address      VARCHAR   NOT NULL,
    value        INTEGER   NOT NULL,
    status       VARCHAR   NOT NULL,
    phone        VARCHAR   NOT NULL,
    user_id      VARCHAR   NOT NULL,
    constraint user_fk foreign key (user_id) references users (id)
);

create TABLE orders_products
(
    order_id   INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    amount     INTEGER NOT NULL,
    CONSTRAINT product_fk foreign key (product_id) references product (id) on delete cascade,
    CONSTRAINT order_fk foreign key (order_id) references orders (id) on delete cascade,
    CONSTRAINT pk_orders_products primary key (order_id, product_id)
);

create table employees
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR NOT NULL,
    last_name  VARCHAR NOT NULL,
    position   VARCHAR NOT NULL
);

create table payments
(
    id           INTEGER PRIMARY KEY,
    order_id     INTEGER NOT NULL,
    value        INTEGER NOT NULL,
    created_time TIMESTAMP,
    status       VARCHAR,
    constraint order_fk foreign key (order_id) references orders (id) on delete cascade
);

create table returns
(
    id     INTEGER PRIMARY KEY,
    status VARCHAR NOT NULL,
    reason VARCHAR,
    constraint order_fk foreign key (id) references orders (id) on delete cascade
);

create TABLE carts
(
    id        INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    uuid      VARCHAR   NOT NULL,
    timeStamp TIMESTAMP NOT NULL
);

create TABLE cart_products
(
    cart_id    INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    amount     INTEGER NOT NULL,
    CONSTRAINT product_fk foreign key (product_id) references product (id) on delete cascade,
    CONSTRAINT cart_fk foreign key (cart_id) references carts (id) on delete cascade,
    CONSTRAINT pk_cart_products primary key (cart_id, product_id)
);

create TABLE advertisements
(
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    text VARCHAR NOT NULL,
    link VARCHAR NOT NULL
);

create table users
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    provider   VARCHAR NOT NULL,
    identifier VARCHAR NOT NULL,
    email      VARCHAR NOT NULL,
    first_name VARCHAR,
    last_name  VARCHAR
);

create table tokens
(
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    userID INTEGER NOT NULL,
    CONSTRAINT user_fk foreign key (userID) references users (id)
);

create table password_info
(
    provider   VARCHAR NOT NULL,
    identifier VARCHAR NOT NULL,
    hasher     VARCHAR NOT NULL,
    password   VARCHAR NOT NULL,
    salt       VARCHAR,
    CONSTRAINT pk_password_info primary key (provider, identifier)

);

create table oauth2_info
(
    provider      VARCHAR NOT NULL,
    identifier    VARCHAR NOT NULL,
    access_token  VARCHAR NOT NULL,
    token_type    VARCHAR,
    expires_in    INTEGER,
    refresh_token VARCHAR,
    CONSTRAINT pk_password_info primary key (provider, identifier)
);

PRAGMA foreign_keys= ON;

# --- !Downs

DROP TABLE category;
DROP TABLE product;
DROP TABLE opinions;
DROP TABLE discounts;
DROP TABLE orders;
DROP TABLE orders_products;
DROP TABLE employees;
DROP TABLE payments;
DROP TABLE returns;
DROP TABLE carts;
DROP TABLE cart_products;
DROP TABLE advertisements;
DROP TABLE tokens;
DROP TABLE users;
DROP TABLE password_info;
drop table oauth2_info;