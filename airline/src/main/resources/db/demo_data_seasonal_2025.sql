-- ============================================================
-- ДЕМО-ДАННЫЕ С СЕЗОННЫМ РАСПРЕДЕЛЕНИЕМ (01.01.2025 - 29.01.2026)
-- ============================================================
-- Этот скрипт создает реалистичные данные с логичным распределением
-- заявок по месяцам с учетом сезонности и типичного спроса
-- ============================================================

SET search_path TO bookings;
SET client_encoding = 'UTF8';

-- ============================================================
-- 1. УДАЛЕНИЕ СТАРЫХ ДАННЫХ
-- ============================================================

DELETE FROM bookings.request_comments;
DELETE FROM bookings.request_history;
DELETE FROM bookings.client_requests;
DELETE FROM bookings.tour_flights;
DELETE FROM bookings.tours;
DELETE FROM bookings.clients WHERE user_id IS NULL OR user_id NOT IN (SELECT id FROM bookings.users WHERE username IN ('client1', 'client2'));
DELETE FROM bookings.employees;
DELETE FROM bookings.user_roles WHERE user_id NOT IN (SELECT id FROM bookings.users WHERE username IN ('admin', 'employee1', 'employee2', 'client1', 'client2'));
DELETE FROM bookings.users WHERE username NOT IN ('admin', 'employee1', 'employee2', 'client1', 'client2');

-- ============================================================
-- 2. ТУРЫ
-- ============================================================

INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at) VALUES
-- Пляжные туры
('Отдых в Сочи', 'Комфортабельный отель на берегу моря, завтраки включены', 45000.00, 7, 'Сочи', true, '2024-12-01'),
('Сочи Премиум', '5* отель, все включено, СПА', 75000.00, 7, 'Сочи', true, '2024-12-01'),
('Сочи, Красная Поляна', 'Горнолыжный курорт, подъемники включены', 52000.00, 6, 'Сочи', true, '2024-12-01'),
('Турция, Анталия', 'Все включено, 5 звезд, аквапарк на территории', 85000.00, 10, 'Анталия', true, '2024-12-01'),
('Турция Стандарт', '4* отель, все включено', 75000.00, 7, 'Анталия', true, '2024-12-01'),
('Турция, Кемер', 'Пляжный отдых, экскурсии', 78000.00, 8, 'Анталия', true, '2024-12-01'),
('Египет, Хургада', 'Красное море, дайвинг, все включено', 65000.00, 8, 'Хургада', true, '2024-12-01'),
('Египет Эконом', '3* отель, завтраки', 55000.00, 7, 'Хургада', true, '2024-12-01'),
('ОАЭ, Дубай', 'Роскошный отдых, шоппинг, экскурсии', 120000.00, 7, 'Дубай', true, '2024-12-01'),
('Дубай Стандарт', '4* отель, завтраки', 100000.00, 6, 'Дубай', true, '2024-12-01'),
('Тайланд, Пхукет', 'Экзотика, пляжи, экскурсии', 95000.00, 12, 'Пхукет', true, '2024-12-01'),
('Мальдивы', 'Райские острова, все включено', 150000.00, 10, 'Мальдивы', true, '2024-12-01'),
('Бали', 'Экзотический отдых, пляжи, храмы', 110000.00, 14, 'Бали', true, '2024-12-01'),

-- Экскурсионные туры
('Париж и Версаль', 'Экскурсионный тур по столице Франции', 78000.00, 5, 'Париж', true, '2024-12-01'),
('Рим и Ватикан', 'Классический тур по Вечному городу', 72000.00, 6, 'Рим', true, '2024-12-01'),
('Прага - сердце Европы', 'Романтический тур по столице Чехии', 55000.00, 4, 'Прага', true, '2024-12-01'),
('Барселона и Коста-Брава', 'Испания: архитектура и пляжи', 68000.00, 7, 'Барселона', true, '2024-12-01'),
('Вена и Зальцбург', 'Музыкальная столица Европы', 65000.00, 5, 'Вена', true, '2024-12-01'),
('Стамбул - мост между Европой и Азией', 'Экскурсии, базары, мечети', 60000.00, 6, 'Стамбул', true, '2024-12-01'),
('Амстердам', 'Каналы, музеи, велосипеды', 70000.00, 4, 'Амстердам', true, '2024-12-01'),

-- Горнолыжные туры
('Австрия, Инсбрук', 'Альпы, горные лыжи, традиционная кухня', 89000.00, 7, 'Инсбрук', true, '2024-12-01')

ON CONFLICT DO NOTHING;

-- ============================================================
-- 3. КЛИЕНТЫ (расширенный список)
-- ============================================================

DO $$
DECLARE
    client1_user_id BIGINT;
    client2_user_id BIGINT;
BEGIN
    SELECT id INTO client1_user_id FROM bookings.users WHERE username = 'client1';
    SELECT id INTO client2_user_id FROM bookings.users WHERE username = 'client2';
    
    INSERT INTO bookings.clients (first_name, last_name, email, phone, vip_status, user_id, created_at) VALUES
    ('Анна', 'Иванова', 'anna.ivanova@mail.ru', '+7 (999) 123-45-67', false, client1_user_id, '2024-12-01'),
    ('Сергей', 'Смирнов', 'sergey.smirnov@mail.ru', '+7 (999) 234-56-78', true, client2_user_id, '2024-12-01'),
    ('Елена', 'Петрова', 'elena.petrova@yandex.ru', '+7 (999) 345-67-89', false, NULL, '2024-12-01'),
    ('Дмитрий', 'Козлов', 'dmitry.kozlov@gmail.com', '+7 (999) 456-78-90', false, NULL, '2024-12-01'),
    ('Ольга', 'Новикова', 'olga.novikova@mail.ru', '+7 (999) 567-89-01', true, NULL, '2024-12-01'),
    ('Александр', 'Морозов', 'alex.morozov@yandex.ru', '+7 (999) 678-90-12', false, NULL, '2024-12-01'),
    ('Мария', 'Соколова', 'maria.sokolova@mail.ru', '+7 (999) 789-01-23', false, NULL, '2024-12-01'),
    ('Иван', 'Волков', 'ivan.volkov@yandex.ru', '+7 (999) 890-12-34', false, NULL, '2024-12-01'),
    ('Екатерина', 'Лебедева', 'ekaterina.lebedeva@gmail.com', '+7 (999) 901-23-45', true, NULL, '2024-12-01'),
    ('Павел', 'Кузнецов', 'pavel.kuznetsov@mail.ru', '+7 (999) 012-34-56', false, NULL, '2024-12-01'),
    ('Татьяна', 'Орлова', 'tatiana.orlova@yandex.ru', '+7 (999) 111-22-33', false, NULL, '2024-12-01'),
    ('Андрей', 'Семенов', 'andrey.semenov@gmail.com', '+7 (999) 222-33-44', false, NULL, '2024-12-01'),
    ('Наталья', 'Павлова', 'natalia.pavlova@mail.ru', '+7 (999) 333-44-55', true, NULL, '2024-12-01'),
    ('Михаил', 'Федоров', 'mikhail.fedorov@yandex.ru', '+7 (999) 444-55-66', false, NULL, '2024-12-01'),
    ('Юлия', 'Морозова', 'yulia.morozova@gmail.com', '+7 (999) 555-66-77', false, NULL, '2024-12-01'),
    ('Владимир', 'Николаев', 'vladimir.nikolaev@mail.ru', '+7 (999) 666-77-88', true, NULL, '2024-12-01'),
    ('Светлана', 'Васильева', 'svetlana.vasilieva@yandex.ru', '+7 (999) 777-88-99', false, NULL, '2024-12-01'),
    ('Алексей', 'Степанов', 'alexey.stepanov@gmail.com', '+7 (999) 888-99-00', false, NULL, '2024-12-01'),
    ('Ирина', 'Андреева', 'irina.andreeva@mail.ru', '+7 (999) 999-00-11', false, NULL, '2024-12-01'),
    ('Роман', 'Алексеев', 'roman.alekseev@yandex.ru', '+7 (999) 000-11-22', false, NULL, '2024-12-01'),
    ('Оксана', 'Макарова', 'oksana.makarova@gmail.com', '+7 (999) 111-33-44', true, NULL, '2024-12-01'),
    ('Денис', 'Леонов', 'denis.leonov@mail.ru', '+7 (999) 222-44-55', false, NULL, '2024-12-01'),
    ('Виктория', 'Сергеева', 'victoria.sergeeva@yandex.ru', '+7 (999) 333-55-66', false, NULL, '2024-12-01'),
    ('Максим', 'Петров', 'maxim.petrov@gmail.com', '+7 (999) 444-66-77', false, NULL, '2024-12-01'),
    ('Анастасия', 'Соколова', 'anastasia.sokolova@mail.ru', '+7 (999) 555-77-88', false, NULL, '2024-12-01'),
    ('Артем', 'Михайлов', 'artem.mikhailov@yandex.ru', '+7 (999) 666-88-99', true, NULL, '2024-12-01'),
    ('Кристина', 'Новикова', 'kristina.novikova@gmail.com', '+7 (999) 777-99-00', false, NULL, '2024-12-01')
    ON CONFLICT (email) DO NOTHING;
END $$;

-- ============================================================
-- 4. СОТРУДНИКИ
-- ============================================================

DO $$
DECLARE
    employee1_user_id BIGINT;
    employee2_user_id BIGINT;
BEGIN
    SELECT id INTO employee1_user_id FROM bookings.users WHERE username = 'employee1';
    SELECT id INTO employee2_user_id FROM bookings.users WHERE username = 'employee2';
    
    INSERT INTO bookings.employees (first_name, last_name, email, phone, hire_date, user_id, created_at) VALUES
    ('Иван', 'Петров', 'ivan.petrov@airline.com', '+7 (495) 111-22-33', '2024-01-15', employee1_user_id, '2024-12-01'),
    ('Мария', 'Сидорова', 'maria.sidorova@airline.com', '+7 (495) 222-33-44', '2023-06-01', employee2_user_id, '2024-12-01')
    ON CONFLICT (email) DO NOTHING;
END $$;

-- ============================================================
-- 5. ЗАЯВКИ С СЕЗОННЫМ РАСПРЕДЕЛЕНИЕМ (01.01.2025 - 29.01.2026)
-- ============================================================

DO $$
DECLARE
    -- ID туров
    tour_sochi_id BIGINT;
    tour_sochi_premium_id BIGINT;
    tour_sochi_ski_id BIGINT;
    tour_turkey_id BIGINT;
    tour_turkey_std_id BIGINT;
    tour_turkey_kemer_id BIGINT;
    tour_egypt_id BIGINT;
    tour_egypt_econ_id BIGINT;
    tour_dubai_id BIGINT;
    tour_dubai_std_id BIGINT;
    tour_phuket_id BIGINT;
    tour_maldives_id BIGINT;
    tour_bali_id BIGINT;
    tour_paris_id BIGINT;
    tour_rome_id BIGINT;
    tour_prague_id BIGINT;
    tour_barcelona_id BIGINT;
    tour_vienna_id BIGINT;
    tour_stambul_id BIGINT;
    tour_amsterdam_id BIGINT;
    tour_innsbruck_id BIGINT;
    
    -- ID клиентов
    client_ids BIGINT[];
    client_count INTEGER;
    
    -- Переменные для генерации
    i INTEGER;
    j INTEGER;
    request_date DATE;
    month_num INTEGER;
    requests_per_month INTEGER;
    destination_tours BIGINT[];
    client_idx INTEGER;
    statuses TEXT[] := ARRAY['NEW', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
    priorities TEXT[] := ARRAY['LOW', 'NORMAL', 'HIGH'];
    status_weights INTEGER[] := ARRAY[20, 15, 60, 5]; -- NEW: 20%, IN_PROGRESS: 15%, COMPLETED: 60%, CANCELLED: 5%
    priority_weights INTEGER[] := ARRAY[10, 70, 20]; -- LOW: 10%, NORMAL: 70%, HIGH: 20%
    
    -- Переменные для weighted random
    total_weight INTEGER;
    random_val INTEGER;
    cumulative INTEGER;
    selected_status TEXT;
    selected_priority TEXT;
    
BEGIN
    -- Получаем ID туров
    SELECT id INTO tour_sochi_id FROM bookings.tours WHERE destination_city = 'Сочи' AND name = 'Отдых в Сочи' LIMIT 1;
    SELECT id INTO tour_sochi_premium_id FROM bookings.tours WHERE destination_city = 'Сочи' AND name = 'Сочи Премиум' LIMIT 1;
    SELECT id INTO tour_sochi_ski_id FROM bookings.tours WHERE destination_city = 'Сочи' AND name = 'Сочи, Красная Поляна' LIMIT 1;
    SELECT id INTO tour_turkey_id FROM bookings.tours WHERE destination_city = 'Анталия' AND name = 'Турция, Анталия' LIMIT 1;
    SELECT id INTO tour_turkey_std_id FROM bookings.tours WHERE destination_city = 'Анталия' AND name = 'Турция Стандарт' LIMIT 1;
    SELECT id INTO tour_turkey_kemer_id FROM bookings.tours WHERE destination_city = 'Анталия' AND name = 'Турция, Кемер' LIMIT 1;
    SELECT id INTO tour_egypt_id FROM bookings.tours WHERE destination_city = 'Хургада' AND name = 'Египет, Хургада' LIMIT 1;
    SELECT id INTO tour_egypt_econ_id FROM bookings.tours WHERE destination_city = 'Хургада' AND name = 'Египет Эконом' LIMIT 1;
    SELECT id INTO tour_dubai_id FROM bookings.tours WHERE destination_city = 'Дубай' AND name = 'ОАЭ, Дубай' LIMIT 1;
    SELECT id INTO tour_dubai_std_id FROM bookings.tours WHERE destination_city = 'Дубай' AND name = 'Дубай Стандарт' LIMIT 1;
    SELECT id INTO tour_phuket_id FROM bookings.tours WHERE destination_city = 'Пхукет' LIMIT 1;
    SELECT id INTO tour_maldives_id FROM bookings.tours WHERE destination_city = 'Мальдивы' LIMIT 1;
    SELECT id INTO tour_bali_id FROM bookings.tours WHERE destination_city = 'Бали' LIMIT 1;
    SELECT id INTO tour_paris_id FROM bookings.tours WHERE destination_city = 'Париж' LIMIT 1;
    SELECT id INTO tour_rome_id FROM bookings.tours WHERE destination_city = 'Рим' LIMIT 1;
    SELECT id INTO tour_prague_id FROM bookings.tours WHERE destination_city = 'Прага' LIMIT 1;
    SELECT id INTO tour_barcelona_id FROM bookings.tours WHERE destination_city = 'Барселона' LIMIT 1;
    SELECT id INTO tour_vienna_id FROM bookings.tours WHERE destination_city = 'Вена' LIMIT 1;
    SELECT id INTO tour_stambul_id FROM bookings.tours WHERE destination_city = 'Стамбул' LIMIT 1;
    SELECT id INTO tour_amsterdam_id FROM bookings.tours WHERE destination_city = 'Амстердам' LIMIT 1;
    SELECT id INTO tour_innsbruck_id FROM bookings.tours WHERE destination_city = 'Инсбрук' LIMIT 1;
    
    -- Получаем ID всех клиентов
    SELECT ARRAY_AGG(id) INTO client_ids FROM bookings.clients;
    client_count := array_length(client_ids, 1);
    
    -- Генерируем заявки по месяцам
    FOR month_num IN 1..13 LOOP -- 13 месяцев: январь 2025 - январь 2026
        request_date := DATE '2025-01-01' + (month_num - 1) * INTERVAL '1 month';
        
        -- Определяем количество заявок и направления в зависимости от месяца
        CASE month_num
            -- Зима (декабрь, январь, февраль) - месяцы 1, 2, 13
            WHEN 1, 2, 13 THEN -- Январь, Февраль 2025, Январь 2026
                requests_per_month := 50 + floor(random() * 30)::INTEGER; -- 50-80 заявок
                -- Зимние направления: горнолыжные (Сочи, Инсбрук), теплые страны (ОАЭ, Египет, Таиланд, Мальдивы)
                destination_tours := ARRAY[tour_sochi_ski_id, tour_innsbruck_id, tour_dubai_id, tour_dubai_std_id, tour_egypt_id, tour_egypt_econ_id, tour_phuket_id, tour_maldives_id, tour_bali_id];
            
            -- Весна (март, апрель, май) - месяцы 3, 4, 5
            WHEN 3, 4, 5 THEN
                requests_per_month := 60 + floor(random() * 40)::INTEGER; -- 60-100 заявок
                -- Весенние направления: экскурсионные (Европа), пляжные начинают сезон (Турция)
                destination_tours := ARRAY[tour_paris_id, tour_rome_id, tour_prague_id, tour_barcelona_id, tour_vienna_id, tour_stambul_id, tour_amsterdam_id, tour_turkey_id, tour_turkey_std_id, tour_turkey_kemer_id, tour_sochi_id];
            
            -- Лето (июнь, июль, август) - месяцы 6, 7, 8
            WHEN 6, 7, 8 THEN
                requests_per_month := 80 + floor(random() * 50)::INTEGER; -- 80-130 заявок (пик сезона)
                -- Летние направления: пляжные (Турция, Сочи, Барселона), экскурсионные
                destination_tours := ARRAY[tour_turkey_id, tour_turkey_std_id, tour_turkey_kemer_id, tour_sochi_id, tour_sochi_premium_id, tour_barcelona_id, tour_paris_id, tour_rome_id, tour_stambul_id, tour_amsterdam_id, tour_phuket_id, tour_bali_id];
            
            -- Осень (сентябрь, октябрь, ноябрь) - месяцы 9, 10, 11
            WHEN 9, 10, 11 THEN
                requests_per_month := 55 + floor(random() * 35)::INTEGER; -- 55-90 заявок
                -- Осенние направления: экскурсионные, теплые страны (ОАЭ, Египет)
                destination_tours := ARRAY[tour_paris_id, tour_rome_id, tour_prague_id, tour_vienna_id, tour_stambul_id, tour_amsterdam_id, tour_dubai_id, tour_dubai_std_id, tour_egypt_id, tour_egypt_econ_id];
            
            -- Декабрь 2025 - месяц 12
            WHEN 12 THEN
                requests_per_month := 45 + floor(random() * 25)::INTEGER; -- 45-70 заявок
                -- Декабрь: горнолыжные начинают сезон, новогодние туры (ОАЭ, Мальдивы)
                destination_tours := ARRAY[tour_sochi_ski_id, tour_innsbruck_id, tour_dubai_id, tour_dubai_std_id, tour_maldives_id, tour_phuket_id, tour_paris_id, tour_rome_id];
            
            ELSE
                requests_per_month := 50;
                destination_tours := ARRAY[tour_sochi_id, tour_turkey_id, tour_paris_id];
        END CASE;
        
        -- Генерируем заявки для месяца
        FOR i IN 1..requests_per_month LOOP
            -- Случайная дата в пределах месяца
            request_date := DATE '2025-01-01' + (month_num - 1) * INTERVAL '1 month' + floor(random() * 28)::INTEGER * INTERVAL '1 day';
            
            -- Выбираем случайный тур из доступных для сезона
            j := 1 + floor(random() * array_length(destination_tours, 1))::INTEGER;
            
            -- Выбираем случайного клиента
            client_idx := 1 + floor(random() * client_count)::INTEGER;
            
            -- Выбираем статус с учетом весов
            total_weight := 0;
            FOR i IN 1..array_length(status_weights, 1) LOOP
                total_weight := total_weight + status_weights[i];
            END LOOP;
            random_val := floor(random() * total_weight)::INTEGER;
            cumulative := 0;
            selected_status := statuses[1];
            FOR i IN 1..array_length(statuses, 1) LOOP
                cumulative := cumulative + status_weights[i];
                IF random_val < cumulative THEN
                    selected_status := statuses[i];
                    EXIT;
                END IF;
            END LOOP;
            
            -- Выбираем приоритет с учетом весов
            total_weight := 0;
            FOR i IN 1..array_length(priority_weights, 1) LOOP
                total_weight := total_weight + priority_weights[i];
            END LOOP;
            random_val := floor(random() * total_weight)::INTEGER;
            cumulative := 0;
            selected_priority := priorities[1];
            FOR i IN 1..array_length(priorities, 1) LOOP
                cumulative := cumulative + priority_weights[i];
                IF random_val < cumulative THEN
                    selected_priority := priorities[i];
                    EXIT;
                END IF;
            END LOOP;
            
            -- Вставляем заявку
            INSERT INTO bookings.client_requests (
                tour_id,
                user_name,
                user_email,
                user_phone,
                status,
                priority,
                comment,
                client_id,
                employee_id,
                created_at
            ) VALUES (
                destination_tours[j],
                (SELECT first_name || ' ' || last_name FROM bookings.clients WHERE id = client_ids[client_idx]),
                (SELECT email FROM bookings.clients WHERE id = client_ids[client_idx]),
                (SELECT phone FROM bookings.clients WHERE id = client_ids[client_idx]),
                selected_status,
                selected_priority,
                CASE floor(random() * 5)::INTEGER
                    WHEN 0 THEN 'Интересует тур на выходные'
                    WHEN 1 THEN 'Хочу узнать больше деталей'
                    WHEN 2 THEN 'Готов к бронированию'
                    WHEN 3 THEN 'Нужна консультация'
                    ELSE NULL
                END,
                client_ids[client_idx],
                CASE WHEN random() < 0.3 THEN (SELECT id FROM bookings.employees LIMIT 1) ELSE NULL END,
                request_date
            );
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Создано заявок с сезонным распределением';
END $$;

-- ============================================================
-- ИТОГОВАЯ СТАТИСТИКА
-- ============================================================

DO $$
DECLARE
    total_tours INTEGER;
    total_clients INTEGER;
    total_requests INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_tours FROM bookings.tours;
    SELECT COUNT(*) INTO total_clients FROM bookings.clients;
    SELECT COUNT(*) INTO total_requests FROM bookings.client_requests;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'ДЕМО-ДАННЫЕ УСПЕШНО ЗАГРУЖЕНЫ!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Туров: %', total_tours;
    RAISE NOTICE 'Клиентов: %', total_clients;
    RAISE NOTICE 'Заявок: %', total_requests;
    RAISE NOTICE '========================================';
END $$;
