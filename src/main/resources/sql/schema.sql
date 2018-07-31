--create table Markers (
--    MID BigSerial PRIMARY KEY,
--	Coordinates geography(Point) UNIQUE NOT NULL,
--    N_Detections Bigserial CHECK (N_Detections > 0),
--    Location_Detection VarChar(50)
--);

create table Markers (
    ID BigSerial PRIMARY KEY,
	Coordinates geography(Point) UNIQUE NOT NULL,
    N_Detections Bigserial CHECK (N_Detections > 0),
    Country VarChar(60) NOT NULL,
    Country_Code VarChar(10) NOT NULL,
    Region VarChar(50) NOT NULL,
    County VarChar(50),
    City VarChar(100) NOT NULL,
    Place VarChar(50),
    Postcode VarChar(10),
    Neighbourhood VarChar(50),
    Road VarChar(50) NOT NULL,
    House_Number INT CHECK (House_Number > -1)
);