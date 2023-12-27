-- Nombre de usuario: admin, contraseña: admin
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('admin', 'Admin name', 'Admin last name', 'admin@eservice.com', 1, 1);

INSERT INTO PASSWORD (VALUE, FK_USER) VALUES ('8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 1);

-- Nombre de usuario: liam, contraseña: liam
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('liam', 'Liam', 'Doe', 'liam@eservice.com', 1, 0);

INSERT INTO PASSWORD (VALUE, FK_USER) VALUES ('f73137d930c31d188d901d57d78c13c88458d61600d1355d28c8841d590a6d69', 2);