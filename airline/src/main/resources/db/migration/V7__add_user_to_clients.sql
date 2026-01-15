-- Добавляем связь клиента с аккаунтом пользователя
ALTER TABLE bookings.clients
ADD COLUMN IF NOT EXISTS user_id BIGINT UNIQUE REFERENCES bookings.users(id) ON DELETE SET NULL;

-- Индекс для быстрого поиска клиента по user_id
CREATE INDEX IF NOT EXISTS idx_clients_user_id ON bookings.clients(user_id);

-- Комментарий: user_id может быть NULL для клиентов, 
-- которые были созданы вручную администратором (без регистрации)

