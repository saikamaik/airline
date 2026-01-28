-- ============================================================
-- ДЕМО-ДАННЫЕ ДЛЯ ЗАЩИТЫ ДИПЛОМА
-- ============================================================
-- Этот скрипт заполняет БД реалистичными данными для демонстрации
-- Выполните его перед защитой: psql -U dbuser -d jcourse -f demo_data_for_defense.sql
-- Или через pgAdmin: выполните скрипт в схеме bookings
-- ============================================================

SET search_path TO bookings;

-- ============================================================
-- 1. ТУРЫ (добавляем больше туров для демонстрации)
-- ============================================================

-- Удаляем существующие туры (опционально, если нужно начать с чистого листа)
-- DELETE FROM bookings.tour_flights;
-- DELETE FROM bookings.client_requests;
-- DELETE FROM bookings.tours;

-- Добавляем разнообразные туры
INSERT INTO bookings.tours (name, description, price, duration_days, destination_city, active, created_at) VALUES
-- Пляжные туры
('Отдых в Сочи', 'Комфортабельный отель на берегу моря, завтраки включены', 45000.00, 7, 'Сочи', true, NOW() - INTERVAL '30 days'),
('Турция, Анталия', 'Все включено, 5 звезд, аквапарк на территории', 85000.00, 10, 'Анталия', true, NOW() - INTERVAL '25 days'),
('Египет, Хургада', 'Красное море, дайвинг, все включено', 65000.00, 8, 'Хургада', true, NOW() - INTERVAL '20 days'),
('ОАЭ, Дубай', 'Роскошный отдых, шоппинг, экскурсии', 120000.00, 7, 'Дубай', true, NOW() - INTERVAL '15 days'),
('Тайланд, Пхукет', 'Экзотика, пляжи, экскурсии', 95000.00, 12, 'Пхукет', true, NOW() - INTERVAL '10 days'),

-- Экскурсионные туры
('Париж и Версаль', 'Экскурсионный тур по столице Франции', 78000.00, 5, 'Париж', true, NOW() - INTERVAL '28 days'),
('Рим и Ватикан', 'Классический тур по Вечному городу', 72000.00, 6, 'Рим', true, NOW() - INTERVAL '22 days'),
('Прага - сердце Европы', 'Романтический тур по столице Чехии', 55000.00, 4, 'Прага', true, NOW() - INTERVAL '18 days'),
('Барселона и Коста-Брава', 'Испания: архитектура и пляжи', 68000.00, 7, 'Барселона', true, NOW() - INTERVAL '12 days'),
('Вена и Зальцбург', 'Музыкальная столица Европы', 65000.00, 5, 'Вена', true, NOW() - INTERVAL '8 days'),

-- Горнолыжные туры
('Сочи, Красная Поляна', 'Горнолыжный курорт, подъемники включены', 52000.00, 6, 'Сочи', true, NOW() - INTERVAL '14 days'),
('Австрия, Инсбрук', 'Альпы, горные лыжи, традиционная кухня', 89000.00, 7, 'Инсбрук', true, NOW() - INTERVAL '6 days'),

-- Неактивные туры (для демонстрации фильтрации)
('Мальдивы (сезон окончен)', 'Райские острова, сезон закрыт', 150000.00, 10, 'Мальдивы', false, NOW() - INTERVAL '60 days'),
('Бали (недоступен)', 'Экзотический отдых, временно недоступен', 110000.00, 14, 'Бали', false, NOW() - INTERVAL '45 days')

ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. ПОЛЬЗОВАТЕЛИ И РОЛИ (если еще не созданы)
-- ============================================================

-- Роли уже должны быть созданы миграциями, но на всякий случай
-- Таблицы users и roles находятся в схеме bookings (согласно default_schema в application.properties)
INSERT INTO bookings.roles (name) VALUES 
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_EMPLOYEE')
ON CONFLICT (name) DO NOTHING;

-- Демо-пользователи (пароли захешированы BCrypt: password123)
-- admin / password123
-- employee1 / password123
-- employee2 / password123
-- client1 / password123
-- client2 / password123

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
-- 3. КЛИЕНТЫ
-- ============================================================

INSERT INTO bookings.clients (first_name, last_name, email, phone, vip_status, user_id, created_at) VALUES
('Анна', 'Иванова', 'anna.ivanova@mail.ru', '+7 (999) 123-45-67', false, 
 (SELECT id FROM bookings.users WHERE username = 'client1'), NOW() - INTERVAL '30 days'),
('Сергей', 'Смирнов', 'sergey.smirnov@mail.ru', '+7 (999) 234-56-78', true,
 (SELECT id FROM bookings.users WHERE username = 'client2'), NOW() - INTERVAL '25 days'),
('Елена', 'Петрова', 'elena.petrova@yandex.ru', '+7 (999) 345-67-89', false, NULL, NOW() - INTERVAL '20 days'),
('Дмитрий', 'Козлов', 'dmitry.kozlov@gmail.com', '+7 (999) 456-78-90', false, NULL, NOW() - INTERVAL '15 days'),
('Ольга', 'Новикова', 'olga.novikova@mail.ru', '+7 (999) 567-89-01', true, NULL, NOW() - INTERVAL '10 days'),
('Александр', 'Морозов', 'alex.morozov@yandex.ru', '+7 (999) 678-90-12', false, NULL, NOW() - INTERVAL '5 days')
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- 4. СОТРУДНИКИ
-- ============================================================

INSERT INTO bookings.employees (user_id, first_name, last_name, email, phone, hire_date, active, created_at) VALUES
((SELECT id FROM bookings.users WHERE username = 'employee1'), 
 'Иван', 'Петров', 'ivan.petrov@airline.com', '+7 (495) 111-22-33', 
 CURRENT_DATE - INTERVAL '2 years', true, NOW() - INTERVAL '60 days'),
((SELECT id FROM bookings.users WHERE username = 'employee2'), 
 'Мария', 'Сидорова', 'maria.sidorova@airline.com', '+7 (495) 222-33-44', 
 CURRENT_DATE - INTERVAL '1 year', true, NOW() - INTERVAL '45 days')
ON CONFLICT (user_id) DO NOTHING;

-- ============================================================
-- 5. ЗАЯВКИ КЛИЕНТОВ (разные статусы и даты)
-- ============================================================

-- Получаем ID туров и клиентов для использования
DO $$
DECLARE
    tour_sochi_id BIGINT;
    tour_turkey_id BIGINT;
    tour_egypt_id BIGINT;
    tour_dubai_id BIGINT;
    tour_phuket_id BIGINT;
    tour_paris_id BIGINT;
    tour_rome_id BIGINT;
    tour_prague_id BIGINT;
    client1_id BIGINT;
    client2_id BIGINT;
    client3_id BIGINT;
    client4_id BIGINT;
    employee1_id BIGINT;
    employee2_id BIGINT;
BEGIN
    -- Получаем ID туров
    SELECT id INTO tour_sochi_id FROM bookings.tours WHERE destination_city = 'Сочи' LIMIT 1;
    SELECT id INTO tour_turkey_id FROM bookings.tours WHERE destination_city = 'Анталия' LIMIT 1;
    SELECT id INTO tour_egypt_id FROM bookings.tours WHERE destination_city = 'Хургада' LIMIT 1;
    SELECT id INTO tour_dubai_id FROM bookings.tours WHERE destination_city = 'Дубай' LIMIT 1;
    SELECT id INTO tour_phuket_id FROM bookings.tours WHERE destination_city = 'Пхукет' LIMIT 1;
    SELECT id INTO tour_paris_id FROM bookings.tours WHERE destination_city = 'Париж' LIMIT 1;
    SELECT id INTO tour_rome_id FROM bookings.tours WHERE destination_city = 'Рим' LIMIT 1;
    SELECT id INTO tour_prague_id FROM bookings.tours WHERE destination_city = 'Прага' LIMIT 1;
    
    -- Получаем ID клиентов
    SELECT id INTO client1_id FROM bookings.clients WHERE email = 'anna.ivanova@mail.ru';
    SELECT id INTO client2_id FROM bookings.clients WHERE email = 'sergey.smirnov@mail.ru';
    SELECT id INTO client3_id FROM bookings.clients WHERE email = 'elena.petrova@yandex.ru';
    SELECT id INTO client4_id FROM bookings.clients WHERE email = 'dmitry.kozlov@gmail.com';
    
    -- Получаем ID сотрудников
    SELECT id INTO employee1_id FROM bookings.employees WHERE email = 'ivan.petrov@airline.com';
    SELECT id INTO employee2_id FROM bookings.employees WHERE email = 'maria.sidorova@airline.com';
    
    -- Новые заявки (последние 7 дней)
    INSERT INTO bookings.client_requests (tour_id, user_name, user_email, user_phone, status, priority, comment, client_id, employee_id, created_at) VALUES
    (tour_sochi_id, 'Анна Иванова', 'anna.ivanova@mail.ru', '+7 (999) 123-45-67', 'NEW', 'NORMAL', 'Интересует тур на выходные', client1_id, NULL, NOW() - INTERVAL '2 days'),
    (tour_turkey_id, 'Сергей Смирнов', 'sergey.smirnov@mail.ru', '+7 (999) 234-56-78', 'NEW', 'HIGH', 'VIP клиент, срочно', client2_id, NULL, NOW() - INTERVAL '1 day'),
    (tour_egypt_id, 'Елена Петрова', 'elena.petrova@yandex.ru', '+7 (999) 345-67-89', 'NEW', 'NORMAL', 'Хочу на море', client3_id, NULL, NOW() - INTERVAL '3 days'),
    
    -- В работе (последние 14 дней)
    (tour_dubai_id, 'Дмитрий Козлов', 'dmitry.kozlov@gmail.com', '+7 (999) 456-78-90', 'IN_PROGRESS', 'NORMAL', 'Уточняю детали', client4_id, employee1_id, NOW() - INTERVAL '10 days'),
    (tour_phuket_id, 'Ольга Новикова', 'olga.novikova@mail.ru', '+7 (999) 567-89-01', 'IN_PROGRESS', 'HIGH', 'VIP клиент', 
     (SELECT id FROM bookings.clients WHERE email = 'olga.novikova@mail.ru'), employee2_id, NOW() - INTERVAL '8 days'),
    (tour_paris_id, 'Александр Морозов', 'alex.morozov@yandex.ru', '+7 (999) 678-90-12', 'IN_PROGRESS', 'NORMAL', 'Романтическое путешествие', 
     (SELECT id FROM bookings.clients WHERE email = 'alex.morozov@yandex.ru'), employee1_id, NOW() - INTERVAL '5 days'),
    
    -- Завершенные (последние 30 дней)
    (tour_rome_id, 'Анна Иванова', 'anna.ivanova@mail.ru', '+7 (999) 123-45-67', 'COMPLETED', 'NORMAL', 'Успешно забронирован', client1_id, employee1_id, NOW() - INTERVAL '25 days'),
    (tour_prague_id, 'Сергей Смирнов', 'sergey.smirnov@mail.ru', '+7 (999) 234-56-78', 'COMPLETED', 'HIGH', 'VIP клиент, все прошло отлично', client2_id, employee2_id, NOW() - INTERVAL '20 days'),
    (tour_sochi_id, 'Елена Петрова', 'elena.petrova@yandex.ru', '+7 (999) 345-67-89', 'COMPLETED', 'NORMAL', 'Клиент доволен', client3_id, employee1_id, NOW() - INTERVAL '15 days'),
    (tour_turkey_id, 'Дмитрий Козлов', 'dmitry.kozlov@gmail.com', '+7 (999) 456-78-90', 'COMPLETED', 'NORMAL', 'Все включено, отлично', client4_id, employee2_id, NOW() - INTERVAL '12 days'),
    
    -- Отмененные
    (tour_egypt_id, 'Ольга Новикова', 'olga.novikova@mail.ru', '+7 (999) 567-89-01', 'CANCELLED', 'NORMAL', 'Клиент передумал', 
     (SELECT id FROM bookings.clients WHERE email = 'olga.novikova@mail.ru'), employee1_id, NOW() - INTERVAL '18 days'),
    (tour_dubai_id, 'Александр Морозов', 'alex.morozov@yandex.ru', '+7 (999) 678-90-12', 'CANCELLED', 'LOW', 'Не подошли даты', 
     (SELECT id FROM bookings.clients WHERE email = 'alex.morozov@yandex.ru'), NULL, NOW() - INTERVAL '22 days');
END $$;

-- ============================================================
-- 6. ИСТОРИЯ ИЗМЕНЕНИЙ ЗАЯВОК
-- ============================================================

DO $$
DECLARE
    req_in_progress_id BIGINT;
    req_completed_id BIGINT;
    employee1_id BIGINT;
    employee2_id BIGINT;
BEGIN
    -- Получаем ID заявок и сотрудников
    SELECT id INTO req_in_progress_id FROM bookings.client_requests WHERE status = 'IN_PROGRESS' LIMIT 1;
    SELECT id INTO req_completed_id FROM bookings.client_requests WHERE status = 'COMPLETED' LIMIT 1;
    SELECT id INTO employee1_id FROM bookings.employees WHERE email = 'ivan.petrov@airline.com';
    SELECT id INTO employee2_id FROM bookings.employees WHERE email = 'maria.sidorova@airline.com';
    
    -- История изменений для заявки в работе
    IF req_in_progress_id IS NOT NULL AND employee1_id IS NOT NULL THEN
        INSERT INTO bookings.request_history (request_id, changed_by_employee_id, field_name, old_value, new_value, description, changed_at) VALUES
        (req_in_progress_id, employee1_id, 'status', 'NEW', 'IN_PROGRESS', 'Заявка взята в работу', NOW() - INTERVAL '8 days'),
        (req_in_progress_id, employee1_id, 'priority', 'NORMAL', 'HIGH', 'Повышен приоритет по просьбе клиента', NOW() - INTERVAL '7 days');
    END IF;
    
    -- История изменений для завершенной заявки
    IF req_completed_id IS NOT NULL AND employee2_id IS NOT NULL THEN
        INSERT INTO bookings.request_history (request_id, changed_by_employee_id, field_name, old_value, new_value, description, changed_at) VALUES
        (req_completed_id, employee2_id, 'status', 'NEW', 'IN_PROGRESS', 'Начата обработка заявки', NOW() - INTERVAL '20 days'),
        (req_completed_id, employee2_id, 'status', 'IN_PROGRESS', 'COMPLETED', 'Тур успешно забронирован, оплата получена', NOW() - INTERVAL '19 days');
    END IF;
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
BEGIN
    SELECT id INTO req_new_id FROM bookings.client_requests WHERE status = 'NEW' LIMIT 1;
    SELECT id INTO req_in_progress_id FROM bookings.client_requests WHERE status = 'IN_PROGRESS' LIMIT 1;
    SELECT id INTO employee1_id FROM bookings.employees WHERE email = 'ivan.petrov@airline.com';
    SELECT id INTO employee2_id FROM bookings.employees WHERE email = 'maria.sidorova@airline.com';
    
    -- Внутренние комментарии
    IF req_new_id IS NOT NULL AND employee1_id IS NOT NULL THEN
        INSERT INTO bookings.request_comments (request_id, employee_id, comment, is_internal, created_at) VALUES
        (req_new_id, employee1_id, 'Клиент уточняет даты, нужно перезвонить завтра', true, NOW() - INTERVAL '1 day'),
        (req_new_id, employee1_id, 'Перезвонил, клиент подтвердил интерес', true, NOW() - INTERVAL '12 hours');
    END IF;
    
    IF req_in_progress_id IS NOT NULL AND employee2_id IS NOT NULL THEN
        INSERT INTO bookings.request_comments (request_id, employee_id, comment, is_internal, created_at) VALUES
        (req_in_progress_id, employee2_id, 'Ожидаю подтверждение от отеля', true, NOW() - INTERVAL '6 days'),
        (req_in_progress_id, employee2_id, 'Отель подтвердил бронирование, отправляю документы клиенту', true, NOW() - INTERVAL '5 days');
    END IF;
END $$;

-- ============================================================
-- ИТОГОВАЯ СТАТИСТИКА
-- ============================================================

DO $$
DECLARE
    tours_count INTEGER;
    clients_count INTEGER;
    requests_count INTEGER;
    employees_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO tours_count FROM bookings.tours;
    SELECT COUNT(*) INTO clients_count FROM bookings.clients;
    SELECT COUNT(*) INTO requests_count FROM bookings.client_requests;
    SELECT COUNT(*) INTO employees_count FROM bookings.employees;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'ДЕМО-ДАННЫЕ УСПЕШНО ЗАГРУЖЕНЫ!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Туров: %', tours_count;
    RAISE NOTICE 'Клиентов: %', clients_count;
    RAISE NOTICE 'Заявок: %', requests_count;
    RAISE NOTICE 'Сотрудников: %', employees_count;
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Для входа используйте:';
    RAISE NOTICE '  admin / password123 (администратор)';
    RAISE NOTICE '  employee1 / password123 (сотрудник)';
    RAISE NOTICE '  client1 / password123 (клиент)';
    RAISE NOTICE '========================================';
END $$;
