
# --- !Ups


INSERT INTO users(provider, identifier, email, first_name, last_name) VALUES ('credentials', 'admin@admin.pl', 'admin@admin.pl', 'admin', 'admin');
INSERT INTO password_info(provider, identifier, hasher, password) VALUES ('credentials', 'admin@admin.pl', 'bcrypt-sha256', '$2a$10$hjEzpgtZxzS2sPfAiI884OSiVe2tIhmSwOTr30g2Ko2wtT8iiY0l.');

INSERT INTO category(name) VALUES('Pens');
INSERT INTO category(name) VALUES('Notebooks');
INSERT INTO category(name) VALUES('Pencils');
INSERT INTO product(name, description, category, price) VALUES ('Blue Pen', 'Normal Blue Pen', 1, 90);
INSERT INTO product(name, description, category, price) VALUES ('Black Pen', 'Normal Black Pen', 1, 90);
INSERT INTO product(name, description, category, price) VALUES ('Red Pen', 'Normal Red Pen', 1, 90);
INSERT INTO product(name, description, category, price) VALUES ('Green Pen', 'Normal Green Pen', 1, 90);
INSERT INTO product(name, description, category, price) VALUES ('Yellow Pen', 'Normal Yellow Pen', 1, 90);


INSERT INTO product(name, description, category, price) VALUES ('Blue Notebook - A4', 'Normal Blue Notebook, Size A4 ', 2, 230);
INSERT INTO product(name, description, category, price) VALUES ('Black Notebook - A4', 'Normal Black Notebook, Size A4', 2, 230);
INSERT INTO product(name, description, category, price) VALUES ('Red Notebook - A4', 'Normal Red Notebook, Size A4', 2, 230);
INSERT INTO product(name, description, category, price) VALUES ('Green Notebook - A4', 'Normal Green Notebook, Size A4', 2, 230);
INSERT INTO product(name, description, category, price) VALUES ('Yellow Notebook - A4', 'Normal Yellow Notebook, Size A4', 2, 230);

INSERT INTO product(name, description, category, price) VALUES ('Blue Notebook - A5', 'Normal Blue Notebook, Size A5', 2, 200);
INSERT INTO product(name, description, category, price) VALUES ('Black Notebook - A5', 'Normal Black Notebook, Size A5', 2, 200);
INSERT INTO product(name, description, category, price) VALUES ('Red Notebook - A5', 'Normal Red Notebook, Size A5', 2, 200);
INSERT INTO product(name, description, category, price) VALUES ('Green Notebook - A5', 'Normal Green Notebook, Size A5', 2, 200);
INSERT INTO product(name, description, category, price) VALUES ('Yellow Notebook - A5', 'Normal Yellow Notebook, Size A5', 2, 200);

INSERT INTO product(name, description, category, price) VALUES ('Pencil HB', 'Standard HB Pencil', 3, 120);
INSERT INTO product(name, description, category, price) VALUES ('Pencil H2', 'Standard H2 Pencil', 3, 120);
INSERT INTO product(name, description, category, price) VALUES ('Pencil H3', 'Standard H3 Pencil', 3, 120);
INSERT INTO product(name, description, category, price) VALUES ('Pencil B2', 'Standard B2 Pencil', 3, 120);
INSERT INTO product(name, description, category, price) VALUES ('Pencil B3', 'Standard B3 Pencil', 3, 120);


INSERT INTO discounts(id, discount) SELECT id, 30 as discount from product where category = 3;

INSERT INTO opinions(product, rating, comment, timestamp, user) VALUES (1, 3, 'spoko', strftime('%s','now'), 1);
INSERT INTO opinions(product, rating, comment, timestamp, user) VALUES (5, 1, 'słaby', strftime('%s','now'), 1);
INSERT INTO opinions(product, rating, comment, timestamp, user) VALUES (8, 5, 'super produkt, polecam', strftime('%s','now'), 1);
INSERT INTO opinions(product, rating, comment, timestamp, user) VALUES (9, 2, '2/10, nie polecam', strftime('%s','now'), 1);
INSERT INTO opinions(product, rating, comment, timestamp, user) VALUES (3, 5, 'fantastyczny produck', strftime('%s','now'), 1);
INSERT INTO opinions(product, rating, comment, timestamp, user) VALUES (17, 3, 'całkiem spoko', strftime('%s','now'), 1);

INSERT INTO advertisements(text, link) VALUES ('Click here for memes !!!', 'https://www.reddit.com/r/ProgrammerHumor/');
INSERT INTO advertisements(text, link) VALUES ('Click for more memes', 'https://www.reddit.com/r/memes/');
INSERT INTO advertisements(text, link) VALUES ('Surprise !!', 'https://www.youtube.com/watch?v=ub82Xb1C8os');
INSERT INTO advertisements(text, link) VALUES ('See this real shop	', 'https://www.x-kom.pl/');
INSERT INTO advertisements(text, link) VALUES ('Different Shop	', 'https://papierowo.pl/');

# --- !Downs

DELETE FROM category WHERE id is not null ;
DELETE FROM product WHERE id is not null ;
DELETE FROM opinions WHERE id is not null ;
DELETE FROM advertisements WHERE id is not null ;
DELETE FROM users;
DELETE FROM password_info;