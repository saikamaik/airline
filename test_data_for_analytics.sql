-- SQL скрипт для добавления тестовых данных для аналитики
-- Добавляет туры с растущим спросом и туры-аномалии (высокая цена, низкий спрос)

SET search_path TO bookings;

-- 1. ТУРЫ С РАСТУЩИМ СПРОСОМ
-- Добавляем популярные туры по разумной цене

-- Тур в Сочи (популярное направление, средняя цена)
INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
VALUES 
    ('Сочи - Летний отдых 2026', 'Пляжный отдых на Черном море с экскурсиями', 35000.00, 7, 'Сочи', true, NOW() - INTERVAL '3 months'),
    ('Сочи - Горнолыжный тур', 'Активный отдых на склонах Красной Поляны', 42000.00, 5, 'Сочи', true, NOW() - INTERVAL '2 months')
ON CONFLICT DO NOTHING;

-- Тур в Казань (растущая популярность, доступная цена)
INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
VALUES 
    ('Казань - Культурная столица', 'Экскурсионный тур по историческим местам', 28000.00, 4, 'Казань', true, NOW() - INTERVAL '4 months'),
    ('Казань - Гастрономический тур', 'Знакомство с татарской кухней и культурой', 32000.00, 3, 'Казань', true, NOW() - INTERVAL '3 months')
ON CONFLICT DO NOTHING;

-- Тур в Калининград (новое популярное направление)
INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
VALUES 
    ('Калининград - Янтарный край', 'Экскурсии по Калининградской области', 38000.00, 5, 'Калининград', true, NOW() - INTERVAL '3 months'),
    ('Калининград - Европейские выходные', 'Архитектура и история', 33000.00, 3, 'Калининград', true, NOW() - INTERVAL '2 months')
ON CONFLICT DO NOTHING;

-- 2. ДОРОГИЕ ТУРЫ С НИЗКИМ СПРОСОМ (АНОМАЛИИ)

-- Очень дорогой VIP тур
INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
VALUES 
    ('Камчатка VIP - Эксклюзив', 'Премиальный тур с вертолетными экскурсиями и проживанием в 5* отеле', 450000.00, 10, 'Петропавловск-Камчатский', true, NOW() - INTERVAL '4 months'),
    ('Байкал Премиум', 'Роскошный тур на Байкал с частным гидом и трансфером на яхте', 380000.00, 8, 'Иркутск', true, NOW() - INTERVAL '3 months'),
    ('Арктика Люкс', 'Эксклюзивная экспедиция на Северный полюс', 850000.00, 14, 'Мурманск', true, NOW() - INTERVAL '5 months')
ON CONFLICT DO NOTHING;

-- Дорогие туры в популярные направления (необоснованно высокая цена)
INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at)
VALUES 
    ('Сочи Делюкс Overpriced', 'Стандартный пляжный отдых по завышенной цене', 95000.00, 7, 'Сочи', true, NOW() - INTERVAL '3 months'),
    ('Казань Золотая', 'Обычный экскурсионный тур по неоправданно высокой цене', 78000.00, 4, 'Казань', true, NOW() - INTERVAL '2 months')
ON CONFLICT DO NOTHING;

-- 3. СОЗДАЕМ ЗАЯВКИ С РАСТУЩИМ ТРЕНДОМ
-- Для туров в Сочи, Казань и Калининград создаем возрастающее количество заявок

DO $$
DECLARE
    sochi_tour_id BIGINT;
    sochi_tour_id_2 BIGINT;
    kazan_tour_id BIGINT;
    kazan_tour_id_2 BIGINT;
    kaliningrad_tour_id BIGINT;
    kaliningrad_tour_id_2 BIGINT;
    week_offset INTEGER;
    requests_count INTEGER;
    i INTEGER;
    request_date TIMESTAMP;
BEGIN
    -- Получаем ID туров
    SELECT id INTO sochi_tour_id FROM bookings.tours WHERE name = 'Сочи - Летний отдых 2026' LIMIT 1;
    SELECT id INTO sochi_tour_id_2 FROM bookings.tours WHERE name = 'Сочи - Горнолыжный тур' LIMIT 1;
    SELECT id INTO kazan_tour_id FROM bookings.tours WHERE name = 'Казань - Культурная столица' LIMIT 1;
    SELECT id INTO kazan_tour_id_2 FROM bookings.tours WHERE name = 'Казань - Гастрономический тур' LIMIT 1;
    SELECT id INTO kaliningrad_tour_id FROM bookings.tours WHERE name = 'Калининград - Янтарный край' LIMIT 1;
    SELECT id INTO kaliningrad_tour_id_2 FROM bookings.tours WHERE name = 'Калининград - Европейские выходные' LIMIT 1;

    -- Создаем заявки за последние 12 недель с РАСТУЩИМ трендом
    -- Чем ближе к текущей дате, тем больше заявок
    FOR week_offset IN 0..11 LOOP
        -- Количество заявок растет: от 2-3 в начале до 10-15 в конце
        requests_count := 2 + (week_offset * 1.2)::INTEGER;
        
        -- Сочи - летний отдых (растущий спрос)
        IF sochi_tour_id IS NOT NULL THEN
            FOR i IN 1..requests_count LOOP
                request_date := NOW() - INTERVAL '1 week' * (11 - week_offset) + (random() * INTERVAL '6 days');
                INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
                VALUES (
                    sochi_tour_id,
                    'Клиент ' || i || ' неделя ' || week_offset,
                    'client' || i || '_w' || week_offset || '@test.com',
                    '+7900' || lpad((1000000 + i + week_offset * 100)::text, 7, '0'),
                    CASE 
                        WHEN random() < 0.7 THEN 'COMPLETED'
                        WHEN random() < 0.85 THEN 'IN_PROGRESS'
                        ELSE 'NEW'
                    END,
                    request_date
                );
            END LOOP;
        END IF;

        -- Казань (растущий спрос, чуть меньше чем Сочи)
        IF kazan_tour_id IS NOT NULL THEN
            FOR i IN 1..(requests_count - 1) LOOP
                request_date := NOW() - INTERVAL '1 week' * (11 - week_offset) + (random() * INTERVAL '6 days');
                INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
                VALUES (
                    kazan_tour_id,
                    'Клиент Казань ' || i || ' w' || week_offset,
                    'kazan_client' || i || '_w' || week_offset || '@test.com',
                    '+7900' || lpad((2000000 + i + week_offset * 100)::text, 7, '0'),
                    CASE 
                        WHEN random() < 0.75 THEN 'COMPLETED'
                        WHEN random() < 0.9 THEN 'IN_PROGRESS'
                        ELSE 'NEW'
                    END,
                    request_date
                );
            END LOOP;
        END IF;

        -- Калининград (быстро растущий спрос на новое направление)
        IF kaliningrad_tour_id IS NOT NULL AND week_offset >= 4 THEN
            -- Заявки начинаются только с 4 недели, но растут быстро
            FOR i IN 1..(requests_count + 2) LOOP
                request_date := NOW() - INTERVAL '1 week' * (11 - week_offset) + (random() * INTERVAL '6 days');
                INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
                VALUES (
                    kaliningrad_tour_id,
                    'Клиент Калининград ' || i || ' w' || week_offset,
                    'kaliningrad' || i || '_w' || week_offset || '@test.com',
                    '+7900' || lpad((3000000 + i + week_offset * 100)::text, 7, '0'),
                    CASE 
                        WHEN random() < 0.8 THEN 'COMPLETED'
                        WHEN random() < 0.95 THEN 'IN_PROGRESS'
                        ELSE 'NEW'
                    END,
                    request_date
                );
            END LOOP;
        END IF;
    END LOOP;

    RAISE NOTICE 'Создано заявок с растущим трендом для популярных туров';
END $$;

-- 4. СОЗДАЕМ МИНИМАЛЬНОЕ КОЛИЧЕСТВО ЗАЯВОК ДЛЯ ДОРОГИХ ТУРОВ (АНОМАЛИИ)
-- Эти туры имеют высокую цену, но очень низкий спрос

DO $$
DECLARE
    kamchatka_vip_id BIGINT;
    baikal_premium_id BIGINT;
    arctic_lux_id BIGINT;
    sochi_overpriced_id BIGINT;
    kazan_overpriced_id BIGINT;
    i INTEGER;
    request_date TIMESTAMP;
BEGIN
    -- Получаем ID дорогих туров
    SELECT id INTO kamchatka_vip_id FROM bookings.tours WHERE name = 'Камчатка VIP - Эксклюзив' LIMIT 1;
    SELECT id INTO baikal_premium_id FROM bookings.tours WHERE name = 'Байкал Премиум' LIMIT 1;
    SELECT id INTO arctic_lux_id FROM bookings.tours WHERE name = 'Арктика Люкс' LIMIT 1;
    SELECT id INTO sochi_overpriced_id FROM bookings.tours WHERE name = 'Сочи Делюкс Overpriced' LIMIT 1;
    SELECT id INTO kazan_overpriced_id FROM bookings.tours WHERE name = 'Казань Золотая' LIMIT 1;

    -- Камчатка VIP - всего 2 заявки за 3 месяца (очень низкий спрос для любой цены)
    IF kamchatka_vip_id IS NOT NULL THEN
        FOR i IN 1..2 LOOP
            request_date := NOW() - INTERVAL '1 month' * (3 - i) - INTERVAL '5 days' * i;
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
            VALUES (
                kamchatka_vip_id,
                'VIP Клиент Камчатка ' || i,
                'vip_kamchatka' || i || '@test.com',
                '+7900' || lpad((5000000 + i)::text, 7, '0'),
                CASE WHEN i = 1 THEN 'COMPLETED' ELSE 'IN_PROGRESS' END,
                request_date
            );
        END LOOP;
    END IF;

    -- Байкал Премиум - 3 заявки за 3 месяца
    IF baikal_premium_id IS NOT NULL THEN
        FOR i IN 1..3 LOOP
            request_date := NOW() - INTERVAL '1 month' * (3 - i) - INTERVAL '3 days' * i;
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
            VALUES (
                baikal_premium_id,
                'Premium Клиент Байкал ' || i,
                'premium_baikal' || i || '@test.com',
                '+7900' || lpad((5100000 + i)::text, 7, '0'),
                'COMPLETED',
                request_date
            );
        END LOOP;
    END IF;

    -- Арктика Люкс - всего 1 заявка за 5 месяцев (экстремально низкий спрос)
    IF arctic_lux_id IS NOT NULL THEN
        request_date := NOW() - INTERVAL '4 months';
        INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
        VALUES (
            arctic_lux_id,
            'Эксклюзив Клиент Арктика',
            'arctic_exclusive@test.com',
            '+79005200001',
            'CANCELLED',
            request_date
        );
    END IF;

    -- Сочи Overpriced - 4 заявки за 3 месяца (при том что обычные туры в Сочи популярны)
    IF sochi_overpriced_id IS NOT NULL THEN
        FOR i IN 1..4 LOOP
            request_date := NOW() - INTERVAL '2 months' + INTERVAL '2 weeks' * i;
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
            VALUES (
                sochi_overpriced_id,
                'Клиент Сочи Дорогой ' || i,
                'sochi_expensive' || i || '@test.com',
                '+7900' || lpad((5200000 + i)::text, 7, '0'),
                CASE 
                    WHEN i <= 2 THEN 'CANCELLED'
                    ELSE 'COMPLETED'
                END,
                request_date
            );
        END LOOP;
    END IF;

    -- Казань Золотая - 3 заявки за 2 месяца (при том что обычные туры в Казань популярны)
    IF kazan_overpriced_id IS NOT NULL THEN
        FOR i IN 1..3 LOOP
            request_date := NOW() - INTERVAL '6 weeks' + INTERVAL '2 weeks' * i;
            INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, created_at)
            VALUES (
                kazan_overpriced_id,
                'Клиент Казань Дорогая ' || i,
                'kazan_expensive' || i || '@test.com',
                '+7900' || lpad((5300000 + i)::text, 7, '0'),
                CASE 
                    WHEN i = 1 THEN 'CANCELLED'
                    WHEN i = 2 THEN 'IN_PROGRESS'
                    ELSE 'COMPLETED'
                END,
                request_date
            );
        END LOOP;
    END IF;

    RAISE NOTICE 'Создано минимальное количество заявок для дорогих туров (аномалии)';
END $$;

-- 5. СТАТИСТИКА ПО СОЗДАННЫМ ДАННЫМ
SELECT 
    '=== СВОДКА ПО ДОБАВЛЕННЫМ ДАННЫМ ===' as info;

SELECT 
    'Туры с растущим спросом:' as category,
    COUNT(*) as count
FROM bookings.tours 
WHERE name IN (
    'Сочи - Летний отдых 2026',
    'Сочи - Горнолыжный тур',
    'Казань - Культурная столица',
    'Казань - Гастрономический тур',
    'Калининград - Янтарный край',
    'Калининград - Европейские выходные'
)
UNION ALL
SELECT 
    'Дорогие туры (аномалии):' as category,
    COUNT(*) as count
FROM bookings.tours 
WHERE name IN (
    'Камчатка VIP - Эксклюзив',
    'Байкал Премиум',
    'Арктика Люкс',
    'Сочи Делюкс Overpriced',
    'Казань Золотая'
);

SELECT 
    '=== РАСПРЕДЕЛЕНИЕ ЗАЯВОК ПО НАПРАВЛЕНИЯМ ===' as info;

SELECT 
    t.destination_city,
    COUNT(cr.id) as request_count,
    MIN(t.price) as min_price,
    MAX(t.price) as max_price,
    AVG(t.price)::DECIMAL(10,2) as avg_price
FROM bookings.tours t
LEFT JOIN bookings.client_requests cr ON t.id = cr.tour_id
WHERE t.name IN (
    'Сочи - Летний отдых 2026',
    'Сочи - Горнолыжный тур',
    'Казань - Культурная столица',
    'Казань - Гастрономический тур',
    'Калининград - Янтарный край',
    'Калининград - Европейские выходные',
    'Камчатка VIP - Эксклюзив',
    'Байкал Премиум',
    'Арктика Люкс',
    'Сочи Делюкс Overpriced',
    'Казань Золотая'
)
GROUP BY t.destination_city
ORDER BY request_count DESC;

SELECT 
    '=== АНОМАЛЬНЫЕ ТУРЫ (высокая цена, низкий спрос) ===' as info;

SELECT 
    t.name,
    t.destination_city,
    t.price,
    COUNT(cr.id) as request_count
FROM bookings.tours t
LEFT JOIN bookings.client_requests cr ON t.id = cr.tour_id
WHERE t.price > 70000
AND t.name IN (
    'Камчатка VIP - Эксклюзив',
    'Байкал Премиум',
    'Арктика Люкс',
    'Сочи Делюкс Overpriced',
    'Казань Золотая'
)
GROUP BY t.id, t.name, t.destination_city, t.price
ORDER BY t.price DESC;
