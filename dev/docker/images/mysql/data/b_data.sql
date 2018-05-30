USE project;

INSERT INTO users(firstName, lastName, email, username, password)
VALUES ('Toto', 'Tata', 'toto@toto.com', 'toto', 'motdepassesecret');

INSERT INTO themes(name) VALUES ('Musique: rock');
INSERT INTO themes(name) VALUES ('Musique: pop');
INSERT INTO themes(name) VALUES ('Musique: punk');
INSERT INTO themes(name) VALUES ('Musique: metal');
INSERT INTO themes(name) VALUES ('Musique: hip-Hop');
INSERT INTO themes(name) VALUES ('Musique: RnB');
INSERT INTO themes(name) VALUES ('Musique: hardcore');
INSERT INTO themes(name) VALUES ('Musique: techno');
INSERT INTO themes(name) VALUES ('Musique: disco');
INSERT INTO themes(name) VALUES ('Musique: trance');
INSERT INTO themes(name) VALUES ('Musique: classique');
INSERT INTO themes(name) VALUES ('Musique: blues');
INSERT INTO themes(name) VALUES ('Musique: jazz');
INSERT INTO themes(name) VALUES ('Musique: country');
INSERT INTO themes(name) VALUES ('Musique: variété française');
INSERT INTO themes(name) VALUES ('Musique: autres');
INSERT INTO themes(name) VALUES ('Carnaval');
INSERT INTO themes(name) VALUES ('Brandon');
INSERT INTO themes(name) VALUES ('Foire');
INSERT INTO themes(name) VALUES ('Exposition');
INSERT INTO themes(name) VALUES ('Vernissage');
INSERT INTO themes(name) VALUES ('Théatre');
INSERT INTO themes(name) VALUES ('Spectacle');
INSERT INTO themes(name) VALUES ('Festival');
INSERT INTO themes(name) VALUES ('Culinaire');
INSERT INTO themes(name) VALUES ('Sport : match');
INSERT INTO themes(name) VALUES ('Sport : compétition');
INSERT INTO themes(name) VALUES ('Sport : course');
INSERT INTO themes(name) VALUES ('Sport : randonnée');
INSERT INTO themes(name) VALUES ('Sport : vélo');
INSERT INTO themes(name) VALUES ('Sport : pétanque');
INSERT INTO themes(name) VALUES ('Sport : badminton');
INSERT INTO themes(name) VALUES ('Sport : beach-volley');
INSERT INTO themes(name) VALUES ('Sport : footnet');
INSERT INTO themes(name) VALUES ('Sport : hockey');
INSERT INTO themes(name) VALUES ('Sport : foot');
INSERT INTO themes(name) VALUES ('Sport : tennis');
INSERT INTO themes(name) VALUES ('Sport : autres');
INSERT INTO themes(name) VALUES ('Autres');

INSERT INTO organizations(name, type, address) VALUES ('Whaaa! prod', 'Association', 'Ch. Ecublens n°9 1009 Lausanne');
INSERT INTO organizations(name, type, address) VALUES ('UBS', 'Banque', 'Ch. de Lausanne 43 1000 Lausanne');
INSERT INTO organizations(name, type, address) VALUES ('Ergopix', 'SàrL', 'Ch. de Vevey 34 1000 Vevey');
INSERT INTO organizations(name, type, address) VALUES ('Ville de Lausanne', 'Commune', 'Ch. des Alouettes 67 Lausanne');