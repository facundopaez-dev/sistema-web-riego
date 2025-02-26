DELETE FROM MAXIMUM_INSOLATION;
ALTER TABLE MAXIMUM_INSOLATION ALTER COLUMN ID RESTART WITH 1;

DELETE FROM SOLAR_RADIATION;
ALTER TABLE SOLAR_RADIATION ALTER COLUMN ID RESTART WITH 1;

DELETE FROM LATITUDE;
ALTER TABLE LATITUDE ALTER COLUMN ID RESTART WITH 1;

DELETE FROM PLANTING_RECORD;
ALTER TABLE PLANTING_RECORD ALTER COLUMN ID RESTART WITH 1;

DELETE FROM IRRIGATION_RECORD;
ALTER TABLE IRRIGATION_RECORD ALTER COLUMN ID RESTART WITH 1;

DELETE FROM CLIMATE_RECORD;
ALTER TABLE CLIMATE_RECORD ALTER COLUMN ID RESTART WITH 1;

DELETE FROM TYPE_PRECIPITATION;
ALTER TABLE TYPE_PRECIPITATION ALTER COLUMN ID RESTART WITH 1;

DELETE FROM STATISTICAL_GRAPH;
ALTER TABLE STATISTICAL_GRAPH ALTER COLUMN ID RESTART WITH 1;

DELETE FROM STATISTICAL_DATA;
ALTER TABLE STATISTICAL_DATA ALTER COLUMN ID RESTART WITH 1;

DELETE FROM STATISTICAL_REPORT;
ALTER TABLE STATISTICAL_REPORT ALTER COLUMN ID RESTART WITH 1;

DELETE FROM HARVEST;
ALTER TABLE HARVEST ALTER COLUMN ID RESTART WITH 1;

DELETE FROM SOIL_WATER_BALANCE;
ALTER TABLE SOIL_WATER_BALANCE ALTER COLUMN ID RESTART WITH 1;

DELETE FROM PARCEL;
ALTER TABLE PARCEL ALTER COLUMN ID RESTART WITH 1;

DELETE FROM GEOGRAPHIC_LOCATION;
ALTER TABLE GEOGRAPHIC_LOCATION ALTER COLUMN ID RESTART WITH 1;

DELETE FROM SOIL;
ALTER TABLE SOIL ALTER COLUMN ID RESTART WITH 1;

DELETE FROM CROP;
ALTER TABLE CROP ALTER COLUMN ID RESTART WITH 1;

DELETE FROM MONTH;
ALTER TABLE MONTH ALTER COLUMN ID RESTART WITH 1;

DELETE FROM TYPE_CROP;
ALTER TABLE TYPE_CROP ALTER COLUMN ID RESTART WITH 1;

DELETE FROM REGION;
ALTER TABLE REGION ALTER COLUMN ID RESTART WITH 1;

DELETE FROM PLANTING_RECORD_STATUS;
ALTER TABLE PLANTING_RECORD_STATUS ALTER COLUMN ID RESTART WITH 1;

DELETE FROM USER_SESSION;
ALTER TABLE USER_SESSION ALTER COLUMN ID RESTART WITH 1;

DELETE FROM ACCOUNT_ACTIVATION_LINK;
ALTER TABLE ACCOUNT_ACTIVATION_LINK ALTER COLUMN ID RESTART WITH 1;

DELETE FROM PASSWORD_RESET_LINK;
ALTER TABLE PASSWORD_RESET_LINK ALTER COLUMN ID RESTART WITH 1;

DELETE FROM PASSWORD;
ALTER TABLE PASSWORD ALTER COLUMN ID RESTART WITH 1;

DELETE FROM IRRIGATION_SYSTEM_USER;
ALTER TABLE IRRIGATION_SYSTEM_USER ALTER COLUMN ID RESTART WITH 1;

DELETE FROM EMAIL;
ALTER TABLE EMAIL ALTER COLUMN ID RESTART WITH 1;

DELETE FROM PARCEL_OPTION;
ALTER TABLE PARCEL_OPTION ALTER COLUMN ID RESTART WITH 1;

DELETE FROM SECRET_KEY;
ALTER TABLE SECRET_KEY ALTER COLUMN ID RESTART WITH 1;