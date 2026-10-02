CREATE TABLE User_Theatre (
    id serial PRIMARY KEY,
    first_name text NOT NULL CHECK (length(trim(first_name)) > 0),
    last_name text NOT NULL CHECK (length(trim(last_name)) > 0),
    email text NOT NULL UNIQUE,
    passport_data varchar(11) NOT NULL UNIQUE,
    password_hash text NOT NULL
);

CREATE TABLE Performance (
    id serial PRIMARY KEY,
    title varchar(255) NOT NULL CHECK (length(trim(title)) > 0),
    description text,
    performance_date timestamptz NOT NULL,
    duration int NOT NULL CHECK (duration > 0),
    base_price numeric(10,2) NOT NULL CHECK (base_price >= 0)
);

CREATE TYPE ticket_status AS ENUM ('booked', 'paid', 'canceled');

CREATE TABLE Ticket (
    id serial PRIMARY KEY,
    user_id int REFERENCES User_Theatre(id) ON DELETE RESTRICT,
    performance_id int NOT NULL REFERENCES Performance(id) ON DELETE CASCADE,
    row_number int NOT NULL CHECK (row_number BETWEEN 1 AND 15),
    seat_number int NOT NULL CHECK (seat_number BETWEEN 1 AND 20),
    status ticket_status NOT NULL DEFAULT 'booked',
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX tickets_active_seat_uniq
    ON Ticket (performance_id, row_number, seat_number)
    WHERE status IN ('booked', 'paid');