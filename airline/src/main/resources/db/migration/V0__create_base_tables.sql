-- Создание схемы bookings (если не существует)
CREATE SCHEMA IF NOT EXISTS bookings;

-- Установка схемы bookings
SET search_path TO bookings;

-- Таблица аэропортов
CREATE TABLE IF NOT EXISTS bookings.airports_data (
    airport_code CHAR(3) PRIMARY KEY,
    airport_name JSONB NOT NULL,
    city JSONB NOT NULL,
    timezone TEXT NOT NULL
);

-- Таблица самолетов
CREATE TABLE IF NOT EXISTS bookings.aircrafts_data (
    aircraft_code CHAR(3) PRIMARY KEY,
    model JSONB NOT NULL,
    range INTEGER NOT NULL
);

-- Таблица рейсов
CREATE TABLE IF NOT EXISTS bookings.flights (
    flight_id SERIAL PRIMARY KEY,
    flight_no CHAR(6) NOT NULL,
    scheduled_departure TIMESTAMP NOT NULL,
    scheduled_arrival TIMESTAMP NOT NULL,
    departure_airport CHAR(3) NOT NULL REFERENCES bookings.airports_data(airport_code) ON DELETE RESTRICT,
    arrival_airport CHAR(3) NOT NULL REFERENCES bookings.airports_data(airport_code) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL,
    aircraft_code CHAR(3) NOT NULL REFERENCES bookings.aircrafts_data(aircraft_code) ON DELETE RESTRICT,
    actual_departure TIMESTAMP,
    actual_arrival TIMESTAMP
);

-- Индексы для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_flights_departure_airport ON bookings.flights(departure_airport);
CREATE INDEX IF NOT EXISTS idx_flights_arrival_airport ON bookings.flights(arrival_airport);
CREATE INDEX IF NOT EXISTS idx_flights_aircraft_code ON bookings.flights(aircraft_code);
CREATE INDEX IF NOT EXISTS idx_flights_scheduled_departure ON bookings.flights(scheduled_departure);
CREATE INDEX IF NOT EXISTS idx_flights_status ON bookings.flights(status);
