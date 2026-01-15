-- Установка схемы bookings
SET search_path TO bookings;

-- Добавление колонки priority в таблицу client_requests
ALTER TABLE bookings.client_requests 
ADD COLUMN IF NOT EXISTS priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

-- Создание индекса для оптимизации запросов по приоритету
CREATE INDEX IF NOT EXISTS idx_client_requests_priority ON bookings.client_requests(priority);

