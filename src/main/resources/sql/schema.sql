create table Markers (
    ID BigSerial PRIMARY KEY,
	Coordinates geography(Point) UNIQUE NOT NULL,
    N_Detections Bigserial CHECK (N_Detections > 0),
    Country VarChar(60) NOT NULL,
    Country_Code VarChar(10) NOT NULL,
    Region VarChar(50) NOT NULL,
    County VarChar(50),
    Town VarChar(100) NOT NULL,
    Place VarChar(50),
    Postcode VarChar(10),
    Neighbourhood VarChar(50),
    Road VarChar(50) NOT NULL,
    House_Number INT CHECK (House_Number > -1)
);

create table Comments (
    ID BigSerial PRIMARY KEY,
    MID BIGINT REFERENCES Markers(ID),
    comment VarChar(200),
    posting_date DATE NOT NULL DEFAULT CURRENT_DATE
);

SELECT
	json_build_object(
		'country',country,
		'countryCode',country_code,
		'region',region,
		'county',county,
		'town',town,
		'place',place,
		'neighbourhood',neighbourhood,
		'road',road
	) AS addressNode,
	ST_AsGeoJSON(coordinates)::json->'coordinates' AS coordinates
FROM markers
WHERE ST_DistanceSphere(
	ST_SetSRID(ST_MakeLine(ST_MakePoint(latA, lngA), ST_MakePoint(latB, lngB)), 4326),
	markers.coordinates
) < 100;

SELECT
json_build_object(
	'country',country,
	'countryCode',country_code,
	'region',region,
	'county',county,
	'town',town,
	'place',place,
	'neighbourhood',neighbourhood,
	'road',road
) AS addressNode,
ST_AsGeoJSON(coordinates)::json->'coordinates' AS coordinates
FROM markers
WHERE markers.coordinates &&
    ST_Transform(
        ST_MakeEnvelope(43, 11, 45, 13, 4326),
        4326
    );

INSERT INTO Comments(MID, comment) VALUES (0,"commento");