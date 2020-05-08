# --- !Ups

CREATE TABLE category (
 "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
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
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    product INTEGER NOT NULL,
    rating  INTEGER NOT NULL,
    comment VARCHAR,
    timestamp TIMESTAMP NOT NULL ,
    CONSTRAINT product_fk FOREIGN KEY (product) references product (id) on delete cascade
);

create TABLE discounts
(
    id   INTEGER NOT NULL PRIMARY KEY,
    discount INTEGER NOT NULL,
    constraint product_fk foreign key (id) references product(id) on delete cascade
);

create TABLE orders
(
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    created_date TIMESTAMP NOT NULL ,
    delivery    BOOLEAN,
    address     VARCHAR NOT NULL ,
    value       INTEGER NOT NULL ,
    status      VARCHAR NOT NULL ,
    first_name  VARCHAR NOT NULL ,
    last_name   VARCHAR NOT NULL ,
    e_mail      VARCHAR NOT NULL ,
    phone       VARCHAR NOT NULL
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
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    position VARCHAR NOT NULL
);

create table payments
(
    id INTEGER PRIMARY KEY,
    order_id INTEGER NOT NULL,
    value INTEGER NOT NULL,
    created_time TIMESTAMP,
    status VARCHAR,
    constraint order_fk foreign key (order_id) references orders(id) on delete cascade
);

create table returns
(
    id INTEGER PRIMARY KEY,
    status VARCHAR NOT NULL,
    reason VARCHAR,
    constraint order_fk foreign key (id) references orders(id) on delete cascade
);

create TABLE carts
(
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    uuid        VARCHAR NOT NULL,
    timeStamp   TIMESTAMP NOT NULL
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
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    text        VARCHAR NOT NULL,
    link        VARCHAR NOT NULL
);

PRAGMA foreign_keys=ON;

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