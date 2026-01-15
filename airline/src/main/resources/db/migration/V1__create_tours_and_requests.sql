-- Установка схемы bookings
SET search_path TO bookings;

-- Таблица туров (с явным указанием схемы)
CREATE TABLE IF NOT EXISTS bookings.tours (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_days INTEGER NOT NULL,
    image_url VARCHAR(500),
    destination_city VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Junction таблица для связи туров и рейсов
CREATE TABLE IF NOT EXISTS bookings.tour_flights (
    tour_id BIGINT NOT NULL REFERENCES bookings.tours(id) ON DELETE CASCADE,
    flight_id INTEGER NOT NULL REFERENCES bookings.flights(flight_id) ON DELETE CASCADE,
    PRIMARY KEY (tour_id, flight_id)
);

-- Таблица заявок клиентов
CREATE TABLE IF NOT EXISTS bookings.client_requests (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL REFERENCES bookings.tours(id) ON DELETE CASCADE,
    user_name VARCHAR(100) NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    user_phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_tours_destination ON bookings.tours(destination_city);
CREATE INDEX IF NOT EXISTS idx_tours_active ON bookings.tours(active);
CREATE INDEX IF NOT EXISTS idx_client_requests_tour_id ON bookings.client_requests(tour_id);
CREATE INDEX IF NOT EXISTS idx_client_requests_status ON bookings.client_requests(status);
CREATE INDEX IF NOT EXISTS idx_client_requests_created_at ON bookings.client_requests(created_at DESC);

