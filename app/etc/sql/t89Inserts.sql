-- Este archivo corresponde a la tarea 89 de la pila del producto.
-- LEER: Este conjunto de datos es para la prueba del modulo automatico ExpiredAccountManager, el cual, se encuentra en la ruta app/src/accountsAdministration.
-- Este modulo automatico elimina los enlaces de activacion de cuenta expirados y NO consumidos, y las cuentas registradas asociadas a los mismos.

INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('gdelf0', 'Goddart', 'Delf', 'ktaunton0@hexun.com', 1, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('emarc1', 'Edik', 'Marc', 'jrichardes2@nsw.gov.au', 1, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('belderkin2', 'Bobbye', 'Elderkin', 'jcurbishley1@hc360.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('pporte3', 'Pippo', 'Porte', 'kdesaur3@accuweather.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('csummerill4', 'Corilla', 'Summerill', 'bhuc4@yolasite.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('cbranford5', 'Cthrine', 'Branford', 'jwallbrook5@altervista.org', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('ogrishkov6', 'Ole', 'Grishkov', 'cwestell6@prweb.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('cbaumber7', 'Cyrille', 'Baumber', 'biddens7@hhs.gov', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('gfrapwell8', 'Gaven', 'Frapwell', 'hcoger8@alexa.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('hcrummie9', 'Harriott', 'Crummie', 'hpeasgood9@4shared.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('bbaumforda', 'Bernhard', 'Baumford', 'afreshwatera@biblegateway.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('nwithamsb', 'Nanine', 'Withams', 'aharmesb@t-online.de', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('cstellic', 'Cathy', 'Stelli', 'edebrettc@nationalgeographic.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('lwallingd', 'Lou', 'Walling', 'cmcardled@newyorker.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('hmcquiltye', 'Hewie', 'McQuilty', 'pstennese@digg.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('adightf', 'Aeriela', 'Dight', 'rstihlf@irs.gov', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('kmcgraffing', 'Katlin', 'McGraffin', 'poverg@dedecms.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('bbeddowsh', 'Booth', 'Beddows', 'tseymarkh@loc.gov', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('cgorettii', 'Carlyn', 'Goretti', 'fouthwaitei@kickstarter.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('jpauelj', 'Jennine', 'Pauel', 'rmolnarj@wsj.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('ccolebeckk', 'Crystie', 'Colebeck', 'cvarnek@ca.gov', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('pcalderbankl', 'Paul', 'Calderbank', 'tberryl@behance.net', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('jtulipm', 'Johann', 'Tulip', 'hfredam@examiner.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('dmatselln', 'Derek', 'Matsell', 'cbullern@jimdo.com', 0, 0);
INSERT INTO IRRIGATION_SYSTEM_USER (USERNAME, NAME, LAST_NAME, EMAIL, ACTIVE, SUPERUSER)
VALUES ('nclemmeyo', 'Nilson', 'Clemmey', 'frizzardoo@jalbum.net', 0, 0);

INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-12-09 00:00:00', '2022-10-14 00:00:00', 1, 1);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-03-24 00:00:00', '2022-11-27 00:00:00', 1, 2);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-11-23 00:00:00', '2022-03-20 00:00:00', 0, 3);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-03-20 00:00:00', '2022-06-21 00:00:00', 0, 4);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-02-22 00:00:00', '2022-12-01 00:00:00', 0, 5);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-03-10 00:00:00', '2022-08-06 00:00:00', 0, 6);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-09-14 00:00:00', '2022-01-26 00:00:00', 0, 7);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-09-05 00:00:00', '2022-04-29 00:00:00', 0, 8);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-08-31 00:00:00', '2022-08-18 00:00:00', 0, 9);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-04-14 00:00:00', '2022-01-30 00:00:00', 0, 10);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-06-01 00:00:00', '2022-07-28 00:00:00', 0, 11);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-03-13 00:00:00', '2022-01-15 00:00:00', 0, 12);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-12-09 00:00:00', '2022-10-04 00:00:00', 0, 13);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-06-13 00:00:00', '2022-01-22 00:00:00', 0, 14);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-08-29 00:00:00', '2022-02-01 00:00:00', 0, 15);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-02-06 00:00:00', '2022-02-25 00:00:00', 0, 16);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-02-13 00:00:00', '2022-09-11 00:00:00', 0, 17);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-01-13 00:00:00', '2022-06-10 00:00:00', 0, 18);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-04-30 00:00:00', '2022-12-02 00:00:00', 0, 19);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-04-16 00:00:00', '2022-07-11 00:00:00', 0, 20);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-11-25 00:00:00', '2022-09-30 00:00:00', 0, 21);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-11-20 00:00:00', '2022-03-23 00:00:00', 0, 22);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-01-11 00:00:00', '2022-08-14 00:00:00', 0, 23);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-04-24 00:00:00', '2022-05-19 00:00:00', 0, 24);
INSERT INTO ACCOUNT_ACTIVATION_LINK (DATE_ISSUE, EXPIRATION_DATE, CONSUMED, FK_USER) VALUES ('2022-06-05 00:00:00', '2022-12-31 00:00:00', 0, 25);