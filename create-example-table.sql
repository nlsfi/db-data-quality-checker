CREATE TABLE IF NOT EXISTS "example_table" (
"id" UUID NOT NULL,
"country_of_location_id" SMALLINT NULL DEFAULT '0',
"geom" GEOMETRY NOT NULL,
);

INSERT INTO "example_table" ("id", "country_of_location_id", "geom") VALUES
('01d24c09-de46-4e93-93fa-5037e64edd34',null, '01060000A03F0F000001000000010300008001000000050000003CDF4F8DD6F50B4119045606B19A59416891ED7C3F35FC3F40355EBA76F60B4123DBF9FEB99A5941355EBA490C020240EE7C3F35C2F70B412DB29D1FB49A59415EBA490C022B0240DBF97E6A28F70B4185EB5130AB9A594196438B6CE7FBFF3F3CDF4F8DD6F50B4119045606B19A59416891ED7C3F35FC3F');