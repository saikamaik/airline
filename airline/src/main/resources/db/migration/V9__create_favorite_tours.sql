-- Создание таблицы для избранных туров клиентов
CREATE TABLE IF NOT EXISTS bookings.favorite_tours (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    tour_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_favorite_client FOREIGN KEY (client_id) REFERENCES bookings.clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_tour FOREIGN KEY (tour_id) REFERENCES bookings.tours(id) ON DELETE CASCADE,
    
    -- Уникальное ограничение: один клиент может добавить тур в избранное только один раз
    CONSTRAINT uk_client_tour UNIQUE (client_id, tour_id)
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_favorite_tours_client_id ON bookings.favorite_tours(client_id);
CREATE INDEX idx_favorite_tours_tour_id ON bookings.favorite_tours(tour_id);
CREATE INDEX idx_favorite_tours_created_at ON bookings.favorite_tours(created_at);
