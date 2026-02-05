-- Добавление колонки active в таблицу clients
ALTER TABLE bookings.clients 
ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- Создание индекса для активных клиентов
CREATE INDEX IF NOT EXISTS idx_clients_active ON bookings.clients(active);
