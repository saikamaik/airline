-- Установка схемы bookings
SET search_path TO bookings;

-- Таблица истории изменений заявок
CREATE TABLE IF NOT EXISTS bookings.request_history (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES bookings.client_requests(id) ON DELETE CASCADE,
    changed_by_employee_id BIGINT REFERENCES bookings.employees(id) ON DELETE SET NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    description TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица комментариев к заявкам
CREATE TABLE IF NOT EXISTS bookings.request_comments (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES bookings.client_requests(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES bookings.employees(id) ON DELETE CASCADE,
    comment TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Добавление колонки client_id в таблицу client_requests (если еще нет)
ALTER TABLE bookings.client_requests 
ADD COLUMN IF NOT EXISTS client_id BIGINT REFERENCES bookings.clients(id) ON DELETE SET NULL;

-- Создание индексов для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_request_history_request_id ON bookings.request_history(request_id);
CREATE INDEX IF NOT EXISTS idx_request_history_changed_at ON bookings.request_history(changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_request_history_changed_by ON bookings.request_history(changed_by_employee_id);

CREATE INDEX IF NOT EXISTS idx_request_comments_request_id ON bookings.request_comments(request_id);
CREATE INDEX IF NOT EXISTS idx_request_comments_employee_id ON bookings.request_comments(employee_id);
CREATE INDEX IF NOT EXISTS idx_request_comments_created_at ON bookings.request_comments(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_request_comments_is_internal ON bookings.request_comments(is_internal);

CREATE INDEX IF NOT EXISTS idx_client_requests_client_id ON bookings.client_requests(client_id);

