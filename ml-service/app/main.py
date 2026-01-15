"""
ML Service для TravelAgency

Сервис машинного обучения для:
- Персонализированных рекомендаций туров
- Аналитики и статистики
- Прогнозирования спроса
- Оптимизации цен
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.config import get_settings
from app.api.recommendations import router as recommendations_router
from app.api.analytics import router as analytics_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifecycle events"""
    # Startup
    print("🚀 ML Service starting...")
    yield
    # Shutdown
    print("👋 ML Service shutting down...")


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
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # В продакшене указать конкретные домены
    allow_credentials=True,
    allow_methods=["*"],
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
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.debug
    )
