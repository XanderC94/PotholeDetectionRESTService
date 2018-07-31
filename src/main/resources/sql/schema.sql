create table Markers (
    MID BigSerial PRIMARY KEY,
	Coordinates geography(Point) UNIQUE NOT NULL,
    N_Detections Bigserial CHECK (N_Detections > 0),
    Location_Detection VarChar(50)
);

create table Markers2 (
    MID BigSerial PRIMARY KEY,
	Coordinates geography(Point) UNIQUE NOT NULL,
    N_Detections Bigserial CHECK (N_Detections > 0),
    Country VarChar(30) NOT NULL,
    Country_Code VarCha(5) NOT NULL
    State VarChar(50) NOT NULL,
    County VarChar(50) NOT NULL,
    City VarChar(50) NOT NULL,
    Place VarChar(50)
    Postcode VarChar(20) NOT NULL,
    Neighbourhood VarChar(50),
    Road VarChar(50) NOT NULL,
    HouseNumber INT CHECK (Road_Number > -1)
);