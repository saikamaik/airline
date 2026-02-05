-- ============================================================
-- МИГРАЦИЯ: ЗАГРУЗКА ДЕМО-ДАННЫХ
-- ============================================================
-- Эта миграция загружает полноценный набор демо-данных:
-- - 22+ тура (пляжные, экскурсионные, горнолыжные)
-- - 27 клиентов (включая VIP)
-- - 2 сотрудника
-- - 800+ заявок с реалистичным распределением
-- - История изменений заявок
-- - Комментарии к заявкам
-- ============================================================

SET search_path TO bookings;
SET client_encoding = 'UTF8';

-- ============================================================
-- 1. РОЛИ И ПОЛЬЗОВАТЕЛИ (если еще не созданы)
-- ============================================================

INSERT INTO bookings.roles (name) VALUES 
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_EMPLOYEE')
ON CONFLICT (name) DO NOTHING;

-- Демо-пользователи (пароли захешированы BCrypt: password123)
INSERT INTO bookings.users (username, password, email, enabled, created_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy7p8.O', 'admin@airline.com', true, NOW() - INTERVAL '90 days'),
('employee1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy7p8.O', 'ivan.petrov@airline.com', true, NOW() - INTERVAL '60 days'),
('employee2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy7p8.O', 'maria.sidorova@airline.com', true, NOW() - INTERVAL '45 days'),
('client1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy7p8.O', 'anna.ivanova@mail.ru', true, NOW() - INTERVAL '30 days'),
('client2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy7p8.O', 'sergey.smirnov@mail.ru', true, NOW() - INTERVAL '25 days')
ON CONFLICT (username) DO NOTHING;

-- Назначаем роли пользователям
INSERT INTO bookings.user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM bookings.users u, bookings.roles r
WHERE (u.username = 'admin' AND r.name = 'ROLE_ADMIN')
   OR (u.username = 'admin' AND r.name = 'ROLE_USER')
   OR (u.username LIKE 'employee%' AND r.name = 'ROLE_EMPLOYEE')
   OR (u.username LIKE 'client%' AND r.name = 'ROLE_USER')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. ТУРЫ (22 тура различных категорий)
-- ============================================================

INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at) VALUES
-- Пляжные туры
('Отдых в Сочи', 'Комфортабельный отель на берегу моря, завтраки включены', 45000.00, 7, 'Сочи', true, NOW() - INTERVAL '30 days'),
('Сочи Премиум', '5* отель, все включено, СПА', 75000.00, 7, 'Сочи', true, NOW() - INTERVAL '25 days'),
('Сочи, Красная Поляна', 'Горнолыжный курорт, подъемники включены', 52000.00, 6, 'Сочи', true, NOW() - INTERVAL '20 days'),
('Турция, Анталия', 'Все включено, 5 звезд, аквапарк на территории', 85000.00, 10, 'Анталия', true, NOW() - INTERVAL '25 days'),
('Турция Стандарт', '4* отель, все включено', 75000.00, 7, 'Анталия', true, NOW() - INTERVAL '20 days'),
('Турция, Кемер', 'Пляжный отдых, экскурсии', 78000.00, 8, 'Анталия', true, NOW() - INTERVAL '15 days'),
('Египет, Хургада', 'Красное море, дайвинг, все включено', 65000.00, 8, 'Хургада', true, NOW() - INTERVAL '20 days'),
('Египет Эконом', '3* отель, завтраки', 55000.00, 7, 'Хургада', true, NOW() - INTERVAL '15 days'),
('ОАЭ, Дубай', 'Роскошный отдых, шоппинг, экскурсии', 120000.00, 7, 'Дубай', true, NOW() - INTERVAL '15 days'),
('Дубай Стандарт', '4* отель, завтраки', 100000.00, 6, 'Дубай', true, NOW() - INTERVAL '10 days'),
('Тайланд, Пхукет', 'Экзотика, пляжи, экскурсии', 95000.00, 12, 'Пхукет', true, NOW() - INTERVAL '10 days'),
('Мальдивы', 'Райские острова, все включено', 150000.00, 10, 'Мальдивы', true, NOW() - INTERVAL '5 days'),
('Бали', 'Экзотический отдых, пляжи, храмы', 110000.00, 14, 'Бали', true, NOW() - INTERVAL '3 days'),

-- Экскурсионные туры
('Париж и Версаль', 'Экскурсионный тур по столице Франции', 78000.00, 5, 'Париж', true, NOW() - INTERVAL '28 days'),
('Рим и Ватикан', 'Классический тур по Вечному городу', 72000.00, 6, 'Рим', true, NOW() - INTERVAL '22 days'),
('Прага - сердце Европы', 'Романтический тур по столице Чехии', 55000.00, 4, 'Прага', true, NOW() - INTERVAL '18 days'),
('Барселона и Коста-Брава', 'Испания: архитектура и пляжи', 68000.00, 7, 'Барселона', true, NOW() - INTERVAL '12 days'),
('Вена и Зальцбург', 'Музыкальная столица Европы', 65000.00, 5, 'Вена', true, NOW() - INTERVAL '8 days'),
('Стамбул - мост между Европой и Азией', 'Экскурсии, базары, мечети', 60000.00, 6, 'Стамбул', true, NOW() - INTERVAL '6 days'),
('Амстердам', 'Каналы, музеи, велосипеды', 70000.00, 4, 'Амстердам', true, NOW() - INTERVAL '4 days'),

-- Горнолыжные туры
('Австрия, Инсбрук', 'Альпы, горные лыжи, традиционная кухня', 89000.00, 7, 'Инсбрук', true, NOW() - INTERVAL '6 days'),

-- Неактивные туры (для демонстрации фильтрации)
('Мальдивы (сезон окончен)', 'Райские острова, сезон закрыт', 150000.00, 10, 'Мальдивы', false, NOW() - INTERVAL '60 days'),
('Бали (недоступен)', 'Экзотический отдых, временно недоступен', 110000.00, 14, 'Бали', false, NOW() - INTERVAL '45 days')

ON CONFLICT DO NOTHING;

-- ============================================================
-- 3. КЛИЕНТЫ (27 клиентов, включая VIP)
-- ============================================================

DO $$
DECLARE
    client1_user_id BIGINT;
    client2_user_id BIGINT;
BEGIN
    SELECT id INTO client1_user_id FROM bookings.users WHERE username = 'client1';
    SELECT id INTO client2_user_id FROM bookings.users WHERE username = 'client2';
    
    INSERT INTO bookings.clients (first_name, last_name, email, phone, vip_status, user_id, created_at) VALUES
    ('Анна', 'Иванова', 'anna.ivanova@mail.ru', '+7 (999) 123-45-67', false, client1_user_id, NOW() - INTERVAL '30 days'),
    ('Сергей', 'Смирнов', 'sergey.smirnov@mail.ru', '+7 (999) 234-56-78', true, client2_user_id, NOW() - INTERVAL '25 days'),
    ('Елена', 'Петрова', 'elena.petrova@yandex.ru', '+7 (999) 345-67-89', false, NULL, NOW() - INTERVAL '20 days'),
    ('Дмитрий', 'Козлов', 'dmitry.kozlov@gmail.com', '+7 (999) 456-78-90', false, NULL, NOW() - INTERVAL '15 days'),
    ('Ольга', 'Новикова', 'olga.novikova@mail.ru', '+7 (999) 567-89-01', true, NULL, NOW() - INTERVAL '10 days'),
    ('Александр', 'Морозов', 'alex.morozov@yandex.ru', '+7 (999) 678-90-12', false, NULL, NOW() - INTERVAL '5 days'),
    ('Мария', 'Соколова', 'maria.sokolova@mail.ru', '+7 (999) 789-01-23', false, NULL, NOW() - INTERVAL '4 days'),
    ('Иван', 'Волков', 'ivan.volkov@yandex.ru', '+7 (999) 890-12-34', false, NULL, NOW() - INTERVAL '3 days'),
    ('Екатерина', 'Лебедева', 'ekaterina.lebedeva@gmail.com', '+7 (999) 901-23-45', true, NULL, NOW() - INTERVAL '2 days'),
    ('Павел', 'Кузнецов', 'pavel.kuznetsov@mail.ru', '+7 (999) 012-34-56', false, NULL, NOW() - INTERVAL '1 day'),
    ('Татьяна', 'Орлова', 'tatiana.orlova@yandex.ru', '+7 (999) 111-22-33', false, NULL, NOW() - INTERVAL '29 days'),
    ('Андрей', 'Семенов', 'andrey.semenov@gmail.com', '+7 (999) 222-33-44', false, NULL, NOW() - INTERVAL '28 days'),
    ('Наталья', 'Павлова', 'natalia.pavlova@mail.ru', '+7 (999) 333-44-55', true, NULL, NOW() - INTERVAL '27 days'),
    ('Михаил', 'Федоров', 'mikhail.fedorov@yandex.ru', '+7 (999) 444-55-66', false, NULL, NOW() - INTERVAL '26 days'),
    ('Юлия', 'Морозова', 'yulia.morozova@gmail.com', '+7 (999) 555-66-77', false, NULL, NOW() - INTERVAL '24 days'),
    ('Владимир', 'Николаев', 'vladimir.nikolaev@mail.ru', '+7 (999) 666-77-88', true, NULL, NOW() - INTERVAL '23 days'),
    ('Светлана', 'Васильева', 'svetlana.vasilieva@yandex.ru', '+7 (999) 777-88-99', false, NULL, NOW() - INTERVAL '22 days'),
    ('Алексей', 'Степанов', 'alexey.stepanov@gmail.com', '+7 (999) 888-99-00', false, NULL, NOW() - INTERVAL '21 days'),
    ('Ирина', 'Андреева', 'irina.andreeva@mail.ru', '+7 (999) 999-00-11', false, NULL, NOW() - INTERVAL '19 days'),
    ('Роман', 'Алексеев', 'roman.alekseev@yandex.ru', '+7 (999) 000-11-22', false, NULL, NOW() - INTERVAL '18 days'),
    ('Оксана', 'Макарова', 'oksana.makarova@gmail.com', '+7 (999) 111-33-44', true, NULL, NOW() - INTERVAL '17 days'),
    ('Денис', 'Леонов', 'denis.leonov@mail.ru', '+7 (999) 222-44-55', false, NULL, NOW() - INTERVAL '16 days'),
    ('Виктория', 'Сергеева', 'victoria.sergeeva@yandex.ru', '+7 (999) 333-55-66', false, NULL, NOW() - INTERVAL '14 days'),
    ('Максим', 'Петров', 'maxim.petrov@gmail.com', '+7 (999) 444-66-77', false, NULL, NOW() - INTERVAL '13 days'),
    ('Анастасия', 'Соколова', 'anastasia.sokolova@mail.ru', '+7 (999) 555-77-88', false, NULL, NOW() - INTERVAL '12 days'),
    ('Артем', 'Михайлов', 'artem.mikhailov@yandex.ru', '+7 (999) 666-88-99', true, NULL, NOW() - INTERVAL '11 days'),
    ('Кристина', 'Новикова', 'kristina.novikova@gmail.com', '+7 (999) 777-99-00', false, NULL, NOW() - INTERVAL '9 days')
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
    
    INSERT INTO bookings.employees (user_id, first_name, last_name, email, phone, hire_date, active, created_at) VALUES
    (employee1_user_id, 'Иван', 'Петров', 'ivan.petrov@airline.com', '+7 (495) 111-22-33', CURRENT_DATE - INTERVAL '2 years', true, NOW() - INTERVAL '60 days'),
    (employee2_user_id, 'Мария', 'Сидорова', 'maria.sidorova@airline.com', '+7 (495) 222-33-44', CURRENT_DATE - INTERVAL '1 year', true, NOW() - INTERVAL '45 days')
    ON CONFLICT (user_id) DO NOTHING;
END $$;

-- ============================================================
-- 5. ЗАЯВКИ КЛИЕНТОВ (800+ заявок с реалистичным распределением)
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
    
    -- ID клиентов и сотрудников
    client_ids BIGINT[];
    client_count INTEGER;
    employee1_id BIGINT;
    employee2_id BIGINT;
    
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
    selected_employee_id BIGINT;
    
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
    SELECT id INTO tour_maldives_id FROM bookings.tours WHERE destination_city = 'Мальдивы' AND active = true LIMIT 1;
    SELECT id INTO tour_bali_id FROM bookings.tours WHERE destination_city = 'Бали' AND active = true LIMIT 1;
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
    
    -- Получаем ID сотрудников
    SELECT id INTO employee1_id FROM bookings.employees WHERE email = 'ivan.petrov@airline.com';
    SELECT id INTO employee2_id FROM bookings.employees WHERE email = 'maria.sidorova@airline.com';
    
    -- Генерируем заявки по месяцам (12 месяцев назад от текущей даты)
    FOR month_num IN 1..12 LOOP
        -- Определяем количество заявок и направления в зависимости от месяца
        CASE month_num
            -- Зима (декабрь, январь, февраль) - месяцы 1, 2, 12
            WHEN 1, 2, 12 THEN
                requests_per_month := 50 + floor(random() * 30)::INTEGER; -- 50-80 заявок
                destination_tours := ARRAY[tour_sochi_ski_id, tour_innsbruck_id, tour_dubai_id, tour_dubai_std_id, tour_egypt_id, tour_egypt_econ_id, tour_phuket_id, tour_maldives_id, tour_bali_id];
            
            -- Весна (март, апрель, май) - месяцы 3, 4, 5
            WHEN 3, 4, 5 THEN
                requests_per_month := 60 + floor(random() * 40)::INTEGER; -- 60-100 заявок
                destination_tours := ARRAY[tour_paris_id, tour_rome_id, tour_prague_id, tour_barcelona_id, tour_vienna_id, tour_stambul_id, tour_amsterdam_id, tour_turkey_id, tour_turkey_std_id, tour_turkey_kemer_id, tour_sochi_id];
            
            -- Лето (июнь, июль, август) - месяцы 6, 7, 8
            WHEN 6, 7, 8 THEN
                requests_per_month := 80 + floor(random() * 50)::INTEGER; -- 80-130 заявок (пик сезона)
                destination_tours := ARRAY[tour_turkey_id, tour_turkey_std_id, tour_turkey_kemer_id, tour_sochi_id, tour_sochi_premium_id, tour_barcelona_id, tour_paris_id, tour_rome_id, tour_stambul_id, tour_amsterdam_id, tour_phuket_id, tour_bali_id];
            
            -- Осень (сентябрь, октябрь, ноябрь) - месяцы 9, 10, 11
            WHEN 9, 10, 11 THEN
                requests_per_month := 55 + floor(random() * 35)::INTEGER; -- 55-90 заявок
                destination_tours := ARRAY[tour_paris_id, tour_rome_id, tour_prague_id, tour_vienna_id, tour_stambul_id, tour_amsterdam_id, tour_dubai_id, tour_dubai_std_id, tour_egypt_id, tour_egypt_econ_id];
            
            ELSE
                requests_per_month := 50;
                destination_tours := ARRAY[tour_sochi_id, tour_turkey_id, tour_paris_id];
        END CASE;
        
        -- Фильтруем NULL значения из массива туров
        destination_tours := ARRAY(SELECT unnest(destination_tours) WHERE unnest IS NOT NULL);
        
        IF array_length(destination_tours, 1) IS NULL OR client_count IS NULL THEN
            CONTINUE;
        END IF;
        
        -- Генерируем заявки для месяца
        FOR i IN 1..requests_per_month LOOP
            -- Случайная дата в пределах месяца
            request_date := NOW() - (month_num || ' months')::INTERVAL - floor(random() * 28)::INTEGER * INTERVAL '1 day';
            
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
            
            -- Назначаем сотрудника в зависимости от статуса
            selected_employee_id := NULL;
            IF selected_status IN ('IN_PROGRESS', 'COMPLETED') THEN
                selected_employee_id := CASE WHEN random() < 0.5 THEN employee1_id ELSE employee2_id END;
            END IF;
            
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
                selected_employee_id,
                request_date
            )
            ON CONFLICT DO NOTHING;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Создано заявок с сезонным распределением';
END $$;

-- ============================================================
-- 6. ИСТОРИЯ ИЗМЕНЕНИЙ ЗАЯВОК
-- ============================================================

DO $$
DECLARE
    req_in_progress_id BIGINT;
    req_completed_id BIGINT;
    req_new_id BIGINT;
    employee1_id BIGINT;
    employee2_id BIGINT;
    i INTEGER;
BEGIN
    SELECT id INTO employee1_id FROM bookings.employees WHERE email = 'ivan.petrov@airline.com';
    SELECT id INTO employee2_id FROM bookings.employees WHERE email = 'maria.sidorova@airline.com';
    
    -- История для заявок в работе (первые 10)
    FOR i IN 1..10 LOOP
        SELECT id INTO req_in_progress_id FROM bookings.client_requests 
        WHERE status = 'IN_PROGRESS' AND employee_id IS NOT NULL 
        ORDER BY created_at DESC LIMIT 1 OFFSET (i - 1);
        
        IF req_in_progress_id IS NOT NULL AND employee1_id IS NOT NULL THEN
            INSERT INTO bookings.request_history (request_id, changed_by_employee_id, field_name, old_value, new_value, description, changed_at) VALUES
            (req_in_progress_id, employee1_id, 'status', 'NEW', 'IN_PROGRESS', 'Заявка взята в работу', NOW() - INTERVAL '10 days')
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
    
    -- История для завершенных заявок (первые 20)
    FOR i IN 1..20 LOOP
        SELECT id INTO req_completed_id FROM bookings.client_requests 
        WHERE status = 'COMPLETED' AND employee_id IS NOT NULL 
        ORDER BY created_at DESC LIMIT 1 OFFSET (i - 1);
        
        IF req_completed_id IS NOT NULL AND employee2_id IS NOT NULL THEN
            INSERT INTO bookings.request_history (request_id, changed_by_employee_id, field_name, old_value, new_value, description, changed_at) VALUES
            (req_completed_id, employee2_id, 'status', 'NEW', 'IN_PROGRESS', 'Начата обработка заявки', NOW() - INTERVAL '25 days'),
            (req_completed_id, employee2_id, 'status', 'IN_PROGRESS', 'COMPLETED', 'Тур успешно забронирован, оплата получена', NOW() - INTERVAL '20 days')
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
END $$;

-- ============================================================
-- 7. КОММЕНТАРИИ К ЗАЯВКАМ
-- ============================================================

DO $$
DECLARE
    req_new_id BIGINT;
    req_in_progress_id BIGINT;
    employee1_id BIGINT;
    employee2_id BIGINT;
    i INTEGER;
BEGIN
    SELECT id INTO employee1_id FROM bookings.employees WHERE email = 'ivan.petrov@airline.com';
    SELECT id INTO employee2_id FROM bookings.employees WHERE email = 'maria.sidorova@airline.com';
    
    -- Комментарии для новых заявок (первые 15)
    FOR i IN 1..15 LOOP
        SELECT id INTO req_new_id FROM bookings.client_requests 
        WHERE status = 'NEW' 
        ORDER BY created_at DESC LIMIT 1 OFFSET (i - 1);
        
        IF req_new_id IS NOT NULL AND employee1_id IS NOT NULL THEN
            INSERT INTO bookings.request_comments (request_id, employee_id, comment, is_internal, created_at) VALUES
            (req_new_id, employee1_id, 'Клиент уточняет даты, нужно перезвонить завтра', true, NOW() - INTERVAL '1 day'),
            (req_new_id, employee1_id, 'Перезвонил, клиент подтвердил интерес', true, NOW() - INTERVAL '12 hours')
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
    
    -- Комментарии для заявок в работе (первые 20)
    FOR i IN 1..20 LOOP
        SELECT id INTO req_in_progress_id FROM bookings.client_requests 
        WHERE status = 'IN_PROGRESS' AND employee_id IS NOT NULL 
        ORDER BY created_at DESC LIMIT 1 OFFSET (i - 1);
        
        IF req_in_progress_id IS NOT NULL AND employee2_id IS NOT NULL THEN
            INSERT INTO bookings.request_comments (request_id, employee_id, comment, is_internal, created_at) VALUES
            (req_in_progress_id, employee2_id, 'Ожидаю подтверждение от отеля', true, NOW() - INTERVAL '6 days'),
            (req_in_progress_id, employee2_id, 'Отель подтвердил бронирование, отправляю документы клиенту', true, NOW() - INTERVAL '5 days')
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
END $$;

-- ============================================================
-- ИТОГОВАЯ СТАТИСТИКА
-- ============================================================

DO $$
DECLARE
    total_tours INTEGER;
    total_clients INTEGER;
    total_requests INTEGER;
    total_employees INTEGER;
    total_history INTEGER;
    total_comments INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_tours FROM bookings.tours;
    SELECT COUNT(*) INTO total_clients FROM bookings.clients;
    SELECT COUNT(*) INTO total_requests FROM bookings.client_requests;
    SELECT COUNT(*) INTO total_employees FROM bookings.employees;
    SELECT COUNT(*) INTO total_history FROM bookings.request_history;
    SELECT COUNT(*) INTO total_comments FROM bookings.request_comments;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'ДЕМО-ДАННЫЕ УСПЕШНО ЗАГРУЖЕНЫ!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Туров: %', total_tours;
    RAISE NOTICE 'Клиентов: %', total_clients;
    RAISE NOTICE 'Заявок: %', total_requests;
    RAISE NOTICE 'Сотрудников: %', total_employees;
    RAISE NOTICE 'Записей истории: %', total_history;
    RAISE NOTICE 'Комментариев: %', total_comments;
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Для входа используйте:';
    RAISE NOTICE '  admin / password123 (администратор)';
    RAISE NOTICE '  employee1 / password123 (сотрудник)';
    RAISE NOTICE '  client1 / password123 (клиент)';
    RAISE NOTICE '========================================';
END $$;
