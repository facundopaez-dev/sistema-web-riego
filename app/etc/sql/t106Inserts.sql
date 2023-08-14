-- Conjunto de datos para la condicion de aceptacion 11 de la tarea 106 de la pila del producto
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2013-06-24', '2013-01-31', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2013-12-09', '2013-07-18', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2014-05-15', '2013-12-23', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2014-10-28', '2014-06-06', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2015-04-12', '2014-11-19', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2015-09-05', '2015-04-14', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2016-02-24', '2015-10-03', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2016-08-02', '2016-03-11', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2017-01-22', '2016-08-31', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2017-07-12', '2017-02-18', 13, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2017-11-07', '2017-08-05', 8, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2018-02-22', '2017-11-20', 8, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2018-09-26', '2018-03-01', 7, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2019-05-21', '2018-10-24', 7, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-01-10', '2019-06-15', 7, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-03-15', '2020-02-09', 10, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-05-07', '2020-04-03', 10, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-06-25', '2020-05-22', 10, 1, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-08-27', '2020-07-24', 10, 1, 1, '7');

INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2013-01-31', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2014-02-12', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2015-03-13', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2016-04-14', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2017-05-15', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2018-06-16', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2019-07-17', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2020-08-18', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2020-09-19', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, WIND_SPEED, MODIFIABLE, FK_PARCEL)
VALUES (101.43, 0.31, '2020-09-20', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 6.83, 1, 1);

INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 1);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 2);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 3);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 4);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 5);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 6);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 7);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 8);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 9);
INSERT INTO TYPE_PRECIPITATION (NAME, FK_CLIMATE_RECORD) VALUES ('rain', 10);

-- Conjunto de datos para la prueba de la condicion de aceptacion 12 de la tarea 106 de la pila del producto
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2015-06-09', '2015-01-31', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2015-10-24', '2015-06-17', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2016-03-09', '2015-11-01', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2016-07-24', '2016-03-17', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2016-12-18', '2016-08-11', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2017-05-11', '2017-01-02', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2017-09-19', '2017-05-13', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2018-02-08', '2017-10-02', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2018-06-29', '2018-02-20', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2018-11-12', '2018-07-06', 14, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2019-03-28', '2018-11-19', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2019-08-30', '2019-04-23', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-02-06', '2019-09-30', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-07-08', '2020-03-01', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2020-11-21', '2020-07-15', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2021-04-15', '2020-12-07', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2021-09-18', '2021-05-12', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2022-01-31', '2021-09-24', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2022-07-02', '2022-02-23', 17, 3, 1, '7');
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS, IRRIGATION_WATER_NEED) VALUES ('2022-11-13', '2022-07-07', 17, 3, 1, '7');