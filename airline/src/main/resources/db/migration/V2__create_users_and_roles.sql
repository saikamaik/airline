-- Установка схемы bookings
SET search_path TO bookings;

-- Таблица ролей
CREATE TABLE IF NOT EXISTS bookings.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS bookings.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Junction таблица для связи пользователей и ролей
CREATE TABLE IF NOT EXISTS bookings.user_roles (
    user_id BIGINT NOT NULL REFERENCES bookings.users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES bookings.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_users_username ON bookings.users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON bookings.users(email);

