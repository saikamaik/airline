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

Для локальной разработки используется Docker Compose:
```bash
cd airline/docker
docker-compose up -d
```

## 👤 Автор

**saikamaik**
- GitHub: [@saikamaik](https://github.com/saikamaik)

Проект создан в рамках дипломной работы.
