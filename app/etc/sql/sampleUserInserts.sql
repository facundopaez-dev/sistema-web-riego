-- Nombre de usuario: admin, contraseña: admin
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('admin', 'Admin name', 'Admin last name', 'admin@eservice.com', 1, 1);

INSERT INTO PASSWORD (VALUE, FK_USER) VALUES ('8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 1);

-- Nombre de usuario: liam, contraseña: liam
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('liam', 'Liam', 'Doe', 'liam@eservice.com', 1, 0);

INSERT INTO PASSWORD (VALUE, FK_USER) VALUES ('f73137d930c31d188d901d57d78c13c88458d61600d1355d28c8841d590a6d69', 2);

-- Nombre de usuario: tbosco, contraseña: tbosco
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('tbosco', 'Tomás', 'Bosco', 'tbosco@eservice.com', 1, 1);

INSERT INTO PASSWORD (VALUE, FK_USER) VALUES ('dd421f4484494907eb3f88be027070e18b4ad2f8d9c2cc1e1ff6c16fc38f592c', 3);