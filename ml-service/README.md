# ML Service для TravelAgency

Сервис машинного обучения для персонализированных рекомендаций туров и аналитики.

## Технологии

- **Python 3.11**
- **FastAPI** - веб-фреймворк
- **Pandas** - обработка данных
- **Scikit-learn** - ML алгоритмы
- **SQLAlchemy** - работа с БД
- **PostgreSQL** - база данных

## Функционал

### Рекомендации (`/recommendations`)

- `POST /recommendations/` - Персонализированные рекомендации
- `GET /recommendations/similar/{tour_id}` - Похожие туры
- `GET /recommendations/popular` - Популярные туры

### Аналитика (`/analytics`)

- `GET /analytics/` - Полная аналитика
- `GET /analytics/statistics` - Статистика по заявкам
- `GET /analytics/destinations` - Популярные направления
- `GET /analytics/trends` - Сезонные тренды
- `GET /analytics/forecast` - Прогноз спроса
- `GET /analytics/price-optimization` - Рекомендации по ценам
- `GET /analytics/dashboard` - Данные для дашборда

## Запуск

### Локально

```bash
# Установка зависимостей
pip install -r requirements.txt

# Запуск
uvicorn app.main:app --reload --port 8000
```

### Docker

```bash
# Собрать и запустить
docker-compose up --build

# Или через основной docker-compose
cd ../airline/docker
docker-compose up ml-service
```

## API Документация

После запуска доступна по адресу:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## Алгоритмы

### Рекомендации

Используется **контентная фильтрация** на основе:
- Косинусного сходства признаков туров
- Истории покупок пользователя
- Предпочтений по цене и длительности

### Прогноз спроса

**Линейная регрессия** на исторических данных:
- Анализ трендов по направлениям
- Сезонная декомпозиция
- Уверенность прогноза на основе R²

### Оптимизация цен

Эвристический алгоритм на основе:
- Конверсии по турам
- Среднерыночных цен по направлению
- Эластичности спроса

## Переменные окружения

```env
DATABASE_URL=postgresql://user:pass@host:5432/db
BACKEND_URL=http://localhost:8080
DEBUG=true
MODEL_PATH=./models
```

## Интеграция с Backend

Backend (Spring Boot) вызывает ML-сервис через HTTP:

```java
// MlServiceClient.java
webClient.get()
    .uri("/analytics/dashboard")
    .retrieve()
    .bodyToMono(JsonNode.class);
```

Эндпоинты в Backend:
- `GET /admin/analytics` - Полная аналитика
- `GET /admin/analytics/dashboard` - Дашборд
- `GET /recommendations` - Рекомендации для мобильного приложения
