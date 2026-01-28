-- ============================================================
-- ДОПОЛНИТЕЛЬНЫЕ ДАННЫЕ ДЛЯ ML-ФУНКЦИЙ
-- ============================================================
-- Добавляет туры и заявки для работы:
-- - Кластеризации (нужно минимум 5-6 туров с разными характеристиками)
-- - Метрик моделей (нужно минимум 4 заявки на направление)
-- - Аномалий (нужны туры с необычными паттернами)
-- ============================================================

SET search_path TO bookings;

-- ============================================================
-- 1. ДОПОЛНИТЕЛЬНЫЕ ТУРЫ (для кластеризации и аномалий)
-- ============================================================

DO $$
DECLARE
    flight_sochi_id BIGINT;
    flight_turkey_id BIGINT;
    flight_egypt_id BIGINT;
    flight_dubai_id BIGINT;
    flight_phuket_id BIGINT;
    flight_paris_id BIGINT;
    flight_rome_id BIGINT;
    flight_barcelona_id BIGINT;
    flight_vienna_id BIGINT;
    flight_innsbruck_id BIGINT;
    flight_spb_id BIGINT;
    flight_moscow_id BIGINT;
    flight_krasnodar_id BIGINT;
    flight_anapa_id BIGINT;
    flight_gelendzhik_id BIGINT;
    flight_nalchik_id BIGINT;
    new_tour_id BIGINT;
BEGIN
    -- Получаем ID рейсов (используем flight_id из таблицы flights)
    SELECT f.flight_id INTO flight_sochi_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Сочи%' OR a.airport_name LIKE '%Сочи%' LIMIT 1;
    SELECT f.flight_id INTO flight_turkey_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Анталия%' OR a.city LIKE '%Стамбул%' LIMIT 1;
    SELECT f.flight_id INTO flight_egypt_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Хургада%' OR a.city LIKE '%Шарм%' LIMIT 1;
    SELECT f.flight_id INTO flight_dubai_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Дубай%' LIMIT 1;
    SELECT f.flight_id INTO flight_phuket_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Пхукет%' LIMIT 1;
    SELECT f.flight_id INTO flight_paris_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Париж%' LIMIT 1;
    SELECT f.flight_id INTO flight_rome_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Рим%' LIMIT 1;
    SELECT f.flight_id INTO flight_barcelona_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Барселона%' LIMIT 1;
    SELECT f.flight_id INTO flight_vienna_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Вена%' LIMIT 1;
    SELECT f.flight_id INTO flight_innsbruck_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Инсбрук%' LIMIT 1;
    SELECT f.flight_id INTO flight_spb_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Санкт-Петербург%' OR a.city LIKE '%Петербург%' LIMIT 1;
    SELECT f.flight_id INTO flight_moscow_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Москва%' LIMIT 1;
    SELECT f.flight_id INTO flight_krasnodar_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Краснодар%' LIMIT 1;
    SELECT f.flight_id INTO flight_anapa_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Анапа%' LIMIT 1;
    SELECT f.flight_id INTO flight_gelendzhik_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Геленджик%' LIMIT 1;
    SELECT f.flight_id INTO flight_nalchik_id FROM bookings.flights f 
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code 
        WHERE a.city LIKE '%Нальчик%' LIMIT 1;

    -- Бюджетные туры (для кластера "бюджетные")
    IF flight_sochi_id IS NOT NULL THEN
        INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
        VALUES ('Эконом тур в Сочи', 'Бюджетный отдых на море', 35000, 5, 'Сочи', true, NOW() - INTERVAL '6 months')
        ON CONFLICT DO NOTHING
        RETURNING id INTO new_tour_id;
    END IF;

    IF flight_anapa_id IS NOT NULL THEN
        INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
        VALUES ('Отдых в Анапе', 'Пляжный отдых', 40000, 7, 'Анапа', true, NOW() - INTERVAL '5 months')
        ON CONFLICT DO NOTHING
        RETURNING id INTO new_tour_id;
    END IF;

    IF flight_gelendzhik_id IS NOT NULL THEN
        INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
        VALUES ('Тур в Геленджик', 'Черноморское побережье', 38000, 6, 'Геленджик', true, NOW() - INTERVAL '4 months')
        ON CONFLICT DO NOTHING
        RETURNING id INTO new_tour_id;
    END IF;

    -- Премиум туры (для кластера "премиум")
    IF flight_dubai_id IS NOT NULL THEN
        INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
        VALUES ('Роскошный Дубай', '5* отель, все включено', 180000, 7, 'Дубай', true, NOW() - INTERVAL '3 months')
        ON CONFLICT DO NOTHING
        RETURNING id INTO new_tour_id;
    END IF;

    IF flight_phuket_id IS NOT NULL THEN
        INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
        VALUES ('Премиум Пхукет', 'Вила на берегу', 200000, 10, 'Пхукет', true, NOW() - INTERVAL '2 months')
        ON CONFLICT DO NOTHING
        RETURNING id INTO new_tour_id;
    END IF;

    -- Средние туры
    IF flight_turkey_id IS NOT NULL THEN
        INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
        VALUES ('Турция стандарт', '4* отель, все включено', 75000, 7, 'Анталия', true, NOW() - INTERVAL '1 month')
        ON CONFLICT DO NOTHING
        RETURNING id INTO new_tour_id;
    END IF;

    IF flight_egypt_id IS NOT NULL THEN
        INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
        VALUES ('Египет комфорт', '5* отель, все включено', 85000, 10, 'Хургада', true, NOW() - INTERVAL '3 weeks')
        ON CONFLICT DO NOTHING
        RETURNING id INTO new_tour_id;
    END IF;

END $$;

-- ============================================================
-- 2. ДОПОЛНИТЕЛЬНЫЕ ЗАЯВКИ (для метрик и аномалий)
-- ============================================================

DO $$
DECLARE
    client_ids BIGINT[];
    employee_ids BIGINT[];
    tour_ids BIGINT[];
    tour_id BIGINT;
    client_idx INT;
    employee_idx INT;
    tour_idx INT;
    i INT;
BEGIN
    -- Получаем ID клиентов
    SELECT ARRAY_AGG(id) INTO client_ids FROM bookings.clients LIMIT 30;
    
    -- Получаем ID сотрудников
    SELECT ARRAY_AGG(id) INTO employee_ids FROM bookings.employees LIMIT 2;
    
    -- Получаем ID туров
    SELECT ARRAY_AGG(id) INTO tour_ids FROM bookings.tours WHERE active = true LIMIT 20;
    
    IF array_length(client_ids, 1) IS NULL OR array_length(employee_ids, 1) IS NULL OR array_length(tour_ids, 1) IS NULL THEN
        RAISE NOTICE 'Недостаточно данных для создания заявок';
        RETURN;
    END IF;

    -- Создаем заявки для разных туров (минимум 4 на направление для метрик)
    -- Сочи - много заявок (для аномалии "высокий спрос")
    FOR i IN 1..10 LOOP
        tour_idx := (i % array_length(tour_ids, 1)) + 1;
        client_idx := (i % array_length(client_ids, 1)) + 1;
        employee_idx := (i % array_length(employee_ids, 1)) + 1;
        
        SELECT id INTO tour_id FROM bookings.tours 
        WHERE destination_city = 'Сочи' AND active = true 
        ORDER BY id LIMIT 1 OFFSET (i % 2);
        
        IF tour_id IS NOT NULL THEN
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, client_id, employee_id, created_at)
            SELECT 
                tour_id,
                'Клиент Сочи ' || i,
                'client_sochi_ml_' || i || '@test.ru',
                '+7 (999) ' || LPAD((i + 100)::text, 3, '0') || '-00-00',
                CASE WHEN i <= 6 THEN 'COMPLETED' WHEN i <= 8 THEN 'IN_PROGRESS' ELSE 'NEW' END,
                CASE WHEN i <= 2 THEN 'HIGH' ELSE 'NORMAL' END,
                client_ids[client_idx],
                employee_ids[employee_idx],
                NOW() - INTERVAL '1 month' + (i || ' days')::INTERVAL
            WHERE NOT EXISTS (
                SELECT 1 FROM bookings.client_requests 
                WHERE user_email = 'client_sochi_ml_' || i || '@test.ru'
            );
        END IF;
    END LOOP;

    -- Создаем заявки для разных туров (минимум 4 на направление для метрик)
    -- Сочи - много заявок (для аномалии "высокий спрос")
    FOR i IN 1..10 LOOP
        client_idx := (i % array_length(client_ids, 1)) + 1;
        employee_idx := (i % array_length(employee_ids, 1)) + 1;
        
        SELECT id INTO tour_id FROM bookings.tours 
        WHERE destination_city = 'Сочи' AND active = true 
        ORDER BY id LIMIT 1 OFFSET (i % 2);
        
        IF tour_id IS NOT NULL THEN
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, client_id, employee_id, created_at)
            SELECT 
                tour_id,
                'Клиент Сочи ML ' || i,
                'client_sochi_ml_' || i || '@test.ru',
                '+7 (999) ' || LPAD((i + 100)::text, 3, '0') || '-00-00',
                CASE WHEN i <= 6 THEN 'COMPLETED' WHEN i <= 8 THEN 'IN_PROGRESS' ELSE 'NEW' END,
                CASE WHEN i <= 2 THEN 'HIGH' ELSE 'NORMAL' END,
                client_ids[client_idx],
                employee_ids[employee_idx],
                NOW() - INTERVAL '1 month' + (i || ' days')::INTERVAL
            WHERE NOT EXISTS (
                SELECT 1 FROM bookings.client_requests 
                WHERE user_email = 'client_sochi_ml_' || i || '@test.ru'
            );
        END IF;
    END LOOP;

    -- Анталия - среднее количество заявок
    FOR i IN 1..6 LOOP
        client_idx := ((i + 10) % array_length(client_ids, 1)) + 1;
        employee_idx := ((i + 10) % array_length(employee_ids, 1)) + 1;
        
        SELECT id INTO tour_id FROM bookings.tours 
        WHERE destination_city = 'Анталия' AND active = true 
        LIMIT 1;
        
        IF tour_id IS NOT NULL THEN
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, client_id, employee_id, created_at)
            SELECT 
                tour_id,
                'Клиент Анталия ML ' || i,
                'client_turkey_ml_' || i || '@test.ru',
                '+7 (999) ' || LPAD((i + 110)::text, 3, '0') || '-00-00',
                CASE WHEN i <= 4 THEN 'COMPLETED' WHEN i = 5 THEN 'IN_PROGRESS' ELSE 'NEW' END,
                'NORMAL',
                client_ids[client_idx],
                employee_ids[employee_idx],
                NOW() - INTERVAL '3 weeks' + (i || ' days')::INTERVAL
            WHERE NOT EXISTS (
                SELECT 1 FROM bookings.client_requests 
                WHERE user_email = 'client_turkey_ml_' || i || '@test.ru'
            );
        END IF;
    END LOOP;

    -- Хургада - среднее количество заявок
    FOR i IN 1..6 LOOP
        client_idx := ((i + 16) % array_length(client_ids, 1)) + 1;
        employee_idx := ((i + 16) % array_length(employee_ids, 1)) + 1;
        
        SELECT id INTO tour_id FROM bookings.tours 
        WHERE destination_city = 'Хургада' AND active = true 
        LIMIT 1;
        
        IF tour_id IS NOT NULL THEN
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, client_id, employee_id, created_at)
            SELECT 
                tour_id,
                'Клиент Хургада ML ' || i,
                'client_egypt_ml_' || i || '@test.ru',
                '+7 (999) ' || LPAD((i + 120)::text, 3, '0') || '-00-00',
                CASE WHEN i <= 4 THEN 'COMPLETED' WHEN i = 5 THEN 'CANCELLED' ELSE 'NEW' END,
                'NORMAL',
                client_ids[client_idx],
                employee_ids[employee_idx],
                NOW() - INTERVAL '2 weeks' + (i || ' days')::INTERVAL
            WHERE NOT EXISTS (
                SELECT 1 FROM bookings.client_requests 
                WHERE user_email = 'client_egypt_ml_' || i || '@test.ru'
            );
        END IF;
    END LOOP;

    -- Дубай - мало заявок, высокая цена (для аномалии "низкий спрос, высокая цена")
    FOR i IN 1..2 LOOP
        client_idx := ((i + 22) % array_length(client_ids, 1)) + 1;
        employee_idx := ((i + 22) % array_length(employee_ids, 1)) + 1;
        
        SELECT id INTO tour_id FROM bookings.tours 
        WHERE destination_city = 'Дубай' AND active = true 
        ORDER BY price DESC LIMIT 1;
        
        IF tour_id IS NOT NULL THEN
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, client_id, employee_id, created_at)
            SELECT 
                tour_id,
                'Клиент Дубай ML ' || i,
                'client_dubai_ml_' || i || '@test.ru',
                '+7 (999) ' || LPAD((i + 130)::text, 3, '0') || '-00-00',
                CASE WHEN i = 1 THEN 'COMPLETED' ELSE 'NEW' END,
                'HIGH',
                client_ids[client_idx],
                employee_ids[employee_idx],
                NOW() - INTERVAL '1 week' + (i || ' days')::INTERVAL
            WHERE NOT EXISTS (
                SELECT 1 FROM bookings.client_requests 
                WHERE user_email = 'client_dubai_ml_' || i || '@test.ru'
            );
        END IF;
    END LOOP;

    -- Пхукет - среднее количество заявок
    FOR i IN 1..5 LOOP
        client_idx := ((i + 24) % array_length(client_ids, 1)) + 1;
        employee_idx := ((i + 24) % array_length(employee_ids, 1)) + 1;
        
        SELECT id INTO tour_id FROM bookings.tours 
        WHERE destination_city = 'Пхукет' AND active = true 
        LIMIT 1;
        
        IF tour_id IS NOT NULL THEN
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, client_id, employee_id, created_at)
            SELECT 
                tour_id,
                'Клиент Пхукет ML ' || i,
                'client_phuket_ml_' || i || '@test.ru',
                '+7 (999) ' || LPAD((i + 140)::text, 3, '0') || '-00-00',
                CASE WHEN i <= 3 THEN 'COMPLETED' WHEN i = 4 THEN 'IN_PROGRESS' ELSE 'NEW' END,
                'NORMAL',
                client_ids[client_idx],
                employee_ids[employee_idx],
                NOW() - INTERVAL '5 days' + (i || ' days')::INTERVAL
            WHERE NOT EXISTS (
                SELECT 1 FROM bookings.client_requests 
                WHERE user_email = 'client_phuket_ml_' || i || '@test.ru'
            );
        END IF;
    END LOOP;

    -- Санкт-Петербург - для метрик
    FOR i IN 1..5 LOOP
        client_idx := ((i + 29) % array_length(client_ids, 1)) + 1;
        employee_idx := ((i + 29) % array_length(employee_ids, 1)) + 1;
        
        SELECT id INTO tour_id FROM bookings.tours 
        WHERE destination_city = 'Санкт-Петербург' AND active = true 
        LIMIT 1;
        
        IF tour_id IS NOT NULL THEN
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, client_id, employee_id, created_at)
            SELECT 
                tour_id,
                'Клиент СПб ML ' || i,
                'client_spb_ml_' || i || '@test.ru',
                '+7 (999) ' || LPAD((i + 150)::text, 3, '0') || '-00-00',
                CASE WHEN i <= 3 THEN 'COMPLETED' WHEN i = 4 THEN 'IN_PROGRESS' ELSE 'NEW' END,
                'NORMAL',
                client_ids[client_idx],
                employee_ids[employee_idx],
                NOW() - INTERVAL '10 days' + (i || ' days')::INTERVAL
            WHERE NOT EXISTS (
                SELECT 1 FROM bookings.client_requests 
                WHERE user_email = 'client_spb_ml_' || i || '@test.ru'
            );
        END IF;
    END LOOP;

END $$;

-- ============================================================
-- 3. ПРИВЯЗКА ТУРОВ К РЕЙСАМ (если нужно)
-- ============================================================

DO $$
DECLARE
    tour_rec RECORD;
    flight_id BIGINT;
BEGIN
    FOR tour_rec IN SELECT id, destination_city FROM bookings.tours WHERE id NOT IN (SELECT DISTINCT tour_id FROM bookings.tour_flights) LOOP
        SELECT f.flight_id INTO flight_id FROM bookings.flights f
        JOIN bookings.airports a ON f.arrival_airport = a.airport_code
        WHERE a.city LIKE '%' || tour_rec.destination_city || '%' 
        LIMIT 1;
        
        IF flight_id IS NOT NULL THEN
            INSERT INTO bookings.tour_flights (tour_id, flight_id)
            VALUES (tour_rec.id, flight_id)
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
END $$;
