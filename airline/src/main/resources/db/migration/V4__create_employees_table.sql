-- Создание таблицы клиентов
CREATE TABLE IF NOT EXISTS bookings.clients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    vip_status BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы сотрудников
CREATE TABLE IF NOT EXISTS bookings.employees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES bookings.users(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(50),
    hire_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Добавление колонки employee_id в таблицу client_requests
ALTER TABLE bookings.client_requests 
ADD COLUMN IF NOT EXISTS employee_id BIGINT REFERENCES bookings.employees(id) ON DELETE SET NULL;

-- Создание индексов для employees
CREATE INDEX IF NOT EXISTS idx_employees_user_id ON bookings.employees(user_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON bookings.employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_active ON bookings.employees(active);
CREATE INDEX IF NOT EXISTS idx_client_requests_employee_id ON bookings.client_requests(employee_id);

-- Создание индексов для clients
CREATE INDEX IF NOT EXISTS idx_clients_email ON bookings.clients(email);
CREATE INDEX IF NOT EXISTS idx_clients_vip_status ON bookings.clients(vip_status);

-- Добавление роли ROLE_EMPLOYEE в таблицу roles (если еще нет)
INSERT INTO bookings.roles (name) 
SELECT 'ROLE_EMPLOYEE' 
WHERE NOT EXISTS (SELECT 1 FROM bookings.roles WHERE name = 'ROLE_EMPLOYEE');

