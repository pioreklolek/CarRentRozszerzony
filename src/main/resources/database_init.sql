DROP TABLE IF EXISTS rental;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id TEXT PRIMARY KEY,
                       login TEXT NOT NULL UNIQUE,
                       password TEXT NOT NULL,
                       role TEXT NOT NULL
);

CREATE TABLE vehicle (
                         id TEXT PRIMARY KEY,
                         type TEXT NOT NULL,
                         category TEXT,
                         brand TEXT NOT NULL,
                         model TEXT NOT NULL,
                         year INT NOT NULL,
                         plate TEXT NOT NULL UNIQUE,
                         price NUMERIC NOT NULL,
                         rented BOOLEAN DEFAULT FALSE,
                         licence_category TEXT,
                         attributes JSONB
);

CREATE TABLE rental (
                        id TEXT PRIMARY KEY,
                        vehicle_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        rent_date TEXT NOT NULL,
                        return_date TEXT,
                        FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO users (id, login, password, role) VALUES
                                                  ('77070363-7936-40c7-aa54-0ac846e66e09', 'admin', '$2a$10$0N0ygrJTriri2PGm1UfDGuyyEZ92/Hmh3GpZxKBJdTQZhLwAil8aS', 'admin'),
                                                  ('edd027ee-4d8e-4e62-b2d0-8a62321a5eed', 'user', '$2a$10$yPcfpBpe9EvRNR3t/aiyneLtNDneXKzg.fxwEZ8cw/0eWidu5sStW', 'user');

INSERT INTO vehicle (id, type, category, brand, model, year, plate, price, rented, licence_category, attributes) VALUES
                                                                                                                     ('1', 'Car', 'Car', 'Toyota', 'Corolla', 2020, 'LU123', 100, false, NULL, '{}'),
                                                                                                                     ('2', 'Car', 'Car', 'Ford', 'Focus', 2018, 'LU456', 100, false, NULL, '{}'),
                                                                                                                     ('3', 'Car', 'Car', 'Honda', 'Civic', 2019, 'LU789', 10, false, NULL, '{}'),
                                                                                                                     ('4', 'Motorcycle', 'Motorcycle', 'Yamaha', 'YZF-R6', 2021, 'LU101', 700, false, 'A', '{}'),
                                                                                                                     ('5', 'Car', 'Car', 'BMW', 'X5', 2022, 'LU112', 30, false, NULL, '{}'),
                                                                                                                     ('6', 'Motorcycle', 'Motorcycle', 'Kawasaki', 'Ninja 650', 2020, 'LU131', 105, false, 'A1', '{}'),
                                                                                                                     ('7', 'Car', 'Car', 'Mercedes', 'C-Class', 2017, 'LU141', 200, false, NULL, '{}'),
                                                                                                                     ('8', 'Motorcycle', 'Motorcycle', 'Honda', 'GSX-R1000', 2019, 'LU151', 3500, false, 'AM', '{}');