INSERT INTO dispatcher_company(name) VALUES('Ålesund Turvogn Service AS');
INSERT INTO dispatcher_company(name) VALUES('Oslo Taxi');

INSERT INTO location(name, longitude, latitude) VALUES ('Ålesund',6.1551755,62.4711412);
INSERT INTO region(name,dispatcher_company_id,region_location_id) VALUES('Ålesund', 1,1);

INSERT INTO location(name, longitude, latitude) VALUES ('Oslo',10.7389701,59.9133301);
INSERT INTO region(name,dispatcher_company_id,region_location_id) VALUES('Oslo', 2,2);

INSERT INTO organizer_company(name) VALUES('GAC - cruise');
INSERT INTO organizer_company(name) VALUES('ECS');
INSERT INTO organizer_company(name) VALUES('GAC - offshore');

INSERT INTO ship(name,imo,organizer_company_id) VALUES('MSC Grandiosa',9803613,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Symphony of the Seas', 9743115,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Harmony of the Seas', 9682875,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Oasis of the Seas', 9383936,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Allure of the Seas', 9383948,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Majesty of the Seas', 8819512,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Navigator of the Seas', 9227508,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Adventure of the Seas', 9151094,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Mariner of the Seas', 9227510,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Anthem of the Seas', 9681452,1);
INSERT INTO ship (name, imo,organizer_company_id) VALUES ('Freedom of the Seas', 9304033,1);


INSERT INTO location(name, longitude, latitude) VALUES ('Storneskaia ålesund',6.1513809, 62.4695037);

INSERT INTO users(id,email,first_name,roles,surname) VALUES('8ae87abd-efc2-4056-8652-36c6322355f1','fylling8@gmail.com','Aleksander', '{ADMIN}','Røder');
INSERT INTO users(id,email,first_name,roles,surname,organizer_company_id) VALUES('ac0cf45a-e296-4724-8f5e-7c5bacd6dc3a','aroderr00@gmail.com','Aleksander', '{MANAGER}','Røder',1);
INSERT INTO users(id,email,first_name,roles,surname,dispatcher_company_id) VALUES('97a37f8e-9f6f-47b9-b59a-3027ac9e54b3','alek-fr@hotmail.com','Aleksander', '{DISPATCHER}','Røder',1);

INSERT INTO location(name, longitude, latitude) VALUES ('OSL airport', 11.100361, 60.194067);
INSERT INTO location(name, longitude, latitude) VALUES ('SVG airport', 5.629167, 58.882778);
INSERT INTO location(name, longitude, latitude) VALUES ('BGO airport', 5.224850, 60.289960);
INSERT INTO location(name, longitude, latitude) VALUES ('TRD airport', 10.923228, 63.455078);
INSERT INTO location(name, longitude, latitude) VALUES ('BVG airport', 6.071111, 67.269167);
INSERT INTO location(name, longitude, latitude) VALUES ('AES airport', 6.108450, 62.561391);
INSERT INTO location(name, longitude, latitude) VALUES ('KRS airport', 8.078652, 58.204214);
INSERT INTO location(name, longitude, latitude) VALUES ('TOS airport', 18.918919, 69.682585);
INSERT INTO location(name, longitude, latitude) VALUES ('HAU airport', 5.216111, 60.293611);
INSERT INTO location(name, longitude, latitude) VALUES ('LYR airport', 15.465556, 78.246111);
INSERT INTO location(name, longitude, latitude) VALUES ('MOL airport', 7.262500, 62.744722);
INSERT INTO location(name, longitude, latitude) VALUES ('ALF airport', 23.355834, 69.976111);
INSERT INTO location(name, longitude, latitude) VALUES ('OSY airport', 11.566944, 64.472222);
INSERT INTO location(name, longitude, latitude) VALUES ('VDS airport', 29.845000, 70.065000);
INSERT INTO location(name, longitude, latitude) VALUES ('FRO airport', 5.024722, 61.584722);
INSERT INTO location(name, longitude, latitude) VALUES ('Quality Waterfront', 6.1460985,62.4699296);
INSERT INTO location(name, longitude, latitude) VALUES ('oslo cruies terminal', 10.734926610901336, 59.906845242762735);