# Airline Management System

Система управления авиакомпанией с микросервисной архитектурой.

## 📁 Структура проекта

```
airline/
├── airline/         # Backend (Java Spring Boot)
├── admin-panel/     # Frontend админ-панель (React + TypeScript)
├── ml-service/      # ML сервис для аналитики (Python FastAPI)
└── mobile-app/      # Мобильное приложение (Kotlin/Android)
```

## 🚀 Технологии

- **Backend:** Java 17, Spring Boot, PostgreSQL
- **Frontend:** React, TypeScript, Vite, MUI
- **ML Service:** Python, FastAPI
- **Mobile:** Kotlin, Android

## ⚙️ Установка и запуск

### Backend (Java)
```bash
cd airline
./mvnw spring-boot:run
```

### Admin Panel (React)
```bash
cd admin-panel
npm install
npm run dev
```

### ML Service (Python)
```bash
cd ml-service
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### Mobile App (Kotlin)
Открыть проект в Android Studio и запустить.

## 🔧 База данных

### Вариант 1: Только PostgreSQL (для локальной разработки)
```bash
cd airline/docker
docker-compose up -d
```

### Вариант 2: Полный запуск через Docker Compose (рекомендуется для защиты)

Запуск всех сервисов одной командой:

```bash
# Из корня проекта airline/
docker-compose up -d
```

Это запустит:
- ✅ PostgreSQL база данных (порт 5432)
- ✅ Backend API (порт 8080)
- ✅ ML Service (порт 8000)
- ✅ Swagger UI (порт 8081)
- ✅ pgAdmin (порт 5050)

**Остановка всех сервисов:**
```bash
docker-compose down
```

**Просмотр логов:**
```bash
docker-compose logs -f airline-backend
docker-compose logs -f ml-service
```

**Пересборка после изменений:**
```bash
docker-compose up -d --build
```

#### Настройка переменных окружения

Создайте файл `.env` в корне проекта `airline/` (можно скопировать из примера ниже):

```env
# База данных
DB_PORT=5432
DB_USER=dbuser
DB_PASSWORD=dbpassword
DB_NAME=jcourse

# Backend
BACKEND_PORT=8080
JWT_SECRET=YourVerySecretKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
APP_DEMO_DATA_ENABLED=false
SPRING_JPA_SHOW_SQL=false

# ML Service
ML_SERVICE_PORT=8000
ML_DEBUG=false

# Дополнительные сервисы
SWAGGER_PORT=8081
PGADMIN_PORT=5050
PGADMIN_EMAIL=admin@admin.com
PGADMIN_PASSWORD=admin
```

**Важно:** Для защиты диплома установите:
- `APP_DEMO_DATA_ENABLED=false` - отключить демо-данные
- `SPRING_JPA_SHOW_SQL=false` - отключить SQL логи
- `ML_DEBUG=false` - отключить debug режим ML-сервиса

## 👤 Автор

**saikamaik**
- GitHub: [@saikamaik](https://github.com/saikamaik)

Проект создан в рамках дипломной работы.
