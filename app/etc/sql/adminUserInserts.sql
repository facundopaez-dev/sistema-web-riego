-- Nombre de usuario: admin, contraseña: admin
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, PASSWORD, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Admin name', 'Admin last name', 'admin@eservice.com', 1, 1);

INSERT INTO PAST_DAYS_REFERENCE ("VALUE", FK_USER) VALUES (1, 1);