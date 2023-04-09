-- Conjunto de datos para la condicion de aceptacion 12 de la tarea 106 de la pila del producto
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2023-06-24', '2023-01-31', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2023-12-09', '2023-07-18', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2024-05-15', '2023-12-23', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2024-10-28', '2024-06-06', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2025-04-12', '2024-11-19', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2025-09-05', '2025-04-14', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2026-02-24', '2025-10-03', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2026-08-02', '2026-03-11', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2027-01-22', '2026-08-31', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2027-07-12', '2027-02-18', 13, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2027-11-07', '2027-08-05', 8, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2028-02-22', '2027-11-20', 8, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2028-09-26', '2028-03-01', 7, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2029-05-21', '2028-10-24', 7, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-01-10', '2029-06-15', 7, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-03-15', '2030-02-09', 10, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-05-07', '2030-04-03', 10, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-06-25', '2030-05-22', 10, 1, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-08-27', '2030-07-24', 10, 1, 1);

INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2023-01-31', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2024-02-12', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2025-03-13', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2026-04-14', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2027-05-15', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2028-06-16', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2029-07-17', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2029-08-18', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2029-09-19', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);
INSERT INTO CLIMATE_RECORD (ATMOSPHERIC_PRESSURE, CLOUD_COVER, "DATE", DEW_POINT, ETC, ETO, MAX_TEMP, MIN_TEMP, PRECIP, PRECIP_PROBABILITY, EXCESS_WATER, WIND_SPEED, FK_PARCEL)
VALUES (101.43, 0.31, '2029-10-20', -4.4, 15.0, 3.613157757002548, 18.3, 9.01, 3.0, 0.0, 0.0, 6.83, 1);

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

-- Conjunto de datos para la prueba de la condicion de aceptacion 13 de la tarea 106 de la pila del producto
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2023-06-09', '2023-01-31', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2023-10-24', '2023-06-17', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2024-03-09', '2023-11-01', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2024-07-24', '2024-03-17', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2024-12-18', '2024-08-11', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2025-05-11', '2025-01-02', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2025-09-19', '2025-05-13', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2026-02-08', '2025-10-02', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2026-06-29', '2026-02-20', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2026-11-12', '2026-07-06', 14, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2027-03-28', '2026-11-19', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2027-08-30', '2027-04-23', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2028-02-06', '2027-09-30', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2028-07-08', '2028-03-01', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2028-11-21', '2028-07-15', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2029-04-15', '2028-12-07', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2029-09-18', '2029-05-12', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-01-31', '2029-09-24', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-07-02', '2030-02-23', 17, 3, 1);
INSERT INTO PLANTING_RECORD (HARVEST_DATE, SEED_DATE, FK_CROP, FK_PARCEL, FK_STATUS) VALUES ('2030-11-13', '2030-07-07', 17, 3, 1);