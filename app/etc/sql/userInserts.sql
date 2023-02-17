-- Nombre de usuario: admin, contraseña: admin
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, PASSWORD, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Admin name', 'Admin last name', 'admin@eservice.com', 1, 1);

-- Nombre de usuario: jane, contraseña: jane
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, PASSWORD, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('jane', '81f8f6dde88365f3928796ec7aa53f72820b06db8664f5fe76a7eb13e24546a2', 'Jane', 'Doe', 'jane@eservice.com', 1, 1);

-- Nombre de usuario: john, contraseña: john
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, PASSWORD, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('john', '96d9632f363564cc3032521409cf22a852f2032eec099ed5967c0d000cec607a', 'John', 'Doe', 'john@eservice.com', 1, 0);

-- Nombre de usuario: anya, contraseña: anya
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, PASSWORD, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('anya', '33939d07e25f54e6432ad3b382e8d3d9e68522b6c3ef868f5c00410308fb6805', 'Anya', 'Doe', 'anya@eservice.com', 1, 0);

-- Nombre de usuario: taylor, contraseña: taylor
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, PASSWORD, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('taylor', '8e924025a26c584ad4ac6365116e09b852ae6b7016da4c0851e269348d93c228', 'Taylor', 'Doe', 'taylor@eservice.com', 1, 0);

-- LEER: Este usuario es necesario para la prueba de la implementacion correspondiente a la tarea 85 de la pila del producto
-- Nombre de usuario: jack, contraseña: jack
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, PASSWORD, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('jack', '31611159e7e6ff7843ea4627745e89225fc866621cfcfdbd40871af4413747cc', 'Jack', 'Doe', 'jack@eservice.com', 1, 0);