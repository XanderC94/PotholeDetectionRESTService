create table Markers (
    MID BigSerial PRIMARY KEY,
	Coordinates geography(Point) UNIQUE NOT NULL,
    N_Detections Bigserial CHECK (N_Detections > 0),
    Location_Detection VarChar(50)
);
