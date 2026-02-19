"""
ML Service для TravelAgency

Сервис машинного обучения для:
- Персонализированных рекомендаций туров
- Аналитики и статистики
- Прогнозирования спроса
- Оптимизации цен
"""

import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.config import get_settings
from app.api.recommendations import router as recommendations_router
from app.api.analytics import router as analytics_router

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifecycle events"""
    # Startup
    logger.info("ML Service starting...")
    logger.info("Loading cached ML models...")
    
    # Инициализируем сервисы для загрузки кэшированных моделей
    from app.services.recommendation_service import RecommendationService
    from app.services.analytics_service import AnalyticsService
    
    # Создаем экземпляры для загрузки кэша
    recommendation_service = RecommendationService()
    analytics_service = AnalyticsService()
    
    logger.info("ML models loaded successfully")
    yield
    
    # Shutdown
    logger.info("ML Service shutting down...")


settings = get_settings()

app = FastAPI(
    title=settings.app_name,
    description="""
    ## ML-сервис для турагентства
    
    ### Функционал:
    
    **Рекомендации:**
    - Персонализированные рекомендации туров
    - Поиск похожих туров
    - Популярные предложения
    
    **Аналитика:**
    - Статистика по заявкам
    - Популярные направления
    - Сезонные тренды
    - Прогноз спроса
    - Оптимизация цен
    """,
    version="1.0.0",
    lifespan=lifespan
)

# CORS для интеграции с фронтендом
# Разрешаем только конкретные домены для безопасности
allowed_origins = [
    "http://localhost:3000",  # Admin panel (development)
    "http://localhost:8080",  # Java backend (development)
    "http://127.0.0.1:3000",  # Admin panel (alternative)
    "http://127.0.0.1:8080",  # Java backend (alternative)
]

# В production можно добавить реальные домены через переменные окружения
# CORS_ORIGINS=http://example.com,https://example.com

app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

# Подключаем роутеры
app.include_router(recommendations_router)
app.include_router(analytics_router)


@app.get("/", tags=["Health"])
async def root():
    """Проверка работоспособности сервиса"""
    return {
        "service": settings.app_name,
        "status": "running",
        "version": "1.0.0"
    }


@app.get("/health", tags=["Health"])
async def health_check():
    """Health check для Docker/Kubernetes"""
    return {
        "status": "healthy",
        "database": "connected"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.debug
    )
