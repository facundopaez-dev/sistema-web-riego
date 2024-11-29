-- Nombre de usuario: admin, contraseña: admin
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, ACTIVE, SUPERUSER, SUPERUSER_PERMISSION_MODIFIER, USER_DELETION_PERMISSION)
VALUES ('admin', 'Admin name', 'Admin last name', 1, 1, 1, 1);

-- Nombre de usuario: jane, contraseña: jane
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, ACTIVE, SUPERUSER)
VALUES ('jane', 'Jane', 'Doe', 1, 1);

-- Nombre de usuario: john, contraseña: john
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, ACTIVE, SUPERUSER)
VALUES ('john', 'John', 'Doe', 1, 0);

-- Nombre de usuario: anya, contraseña: anya
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, ACTIVE, SUPERUSER)
VALUES ('anya', 'Anya', 'Doe', 1, 0);

-- Nombre de usuario: taylor, contraseña: taylor
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, ACTIVE, SUPERUSER)
VALUES ('taylor', 'Taylor', 'Doe', 1, 0);

-- LEER: Este usuario es necesario para la prueba de la implementacion correspondiente a la tarea 85 de la pila del producto
-- Nombre de usuario: jack, contraseña: jack
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, ACTIVE, SUPERUSER)
VALUES ('jack', 'Jack', 'Doe', 1, 0);