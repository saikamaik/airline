"""API эндпоинты для аналитики"""

from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional
from app.schemas.analytics import (
    RequestStatistics,
    PopularDestination,
    SeasonalTrend,
    DemandForecast,
    PriceOptimization,
    AnalyticsResponse,
    TimeRange
)
from app.services.analytics_service import AnalyticsService

router = APIRouter(prefix="/analytics", tags=["Аналитика"])

analytics_service = AnalyticsService()


@router.get("/", response_model=AnalyticsResponse)
async def get_full_analytics(
    period: TimeRange = Query(default=TimeRange.MONTH)
):
    """
    Получить полную аналитику по системе.
    
    - **period**: Период анализа (week, month, quarter, year)
    
    Включает:
    - Статистику по заявкам
    - Популярные направления
    - Сезонные тренды
    - Прогнозы спроса
    - Рекомендации по ценам
    """
    try:
        return analytics_service.get_full_analytics(period)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/statistics", response_model=RequestStatistics)
async def get_statistics(
    days: int = Query(default=30, ge=1, le=365)
):
    """
    Получить статистику по заявкам.
    
    - Общее количество заявок
    - Разбивка по статусам
    - Конверсия
    - Среднее время обработки
    """
    try:
        return analytics_service.get_request_statistics(days)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/destinations", response_model=List[PopularDestination])
async def get_popular_destinations(
    days: int = Query(default=90, ge=1, le=365),
    limit: int = Query(default=10, ge=1, le=50)
):
    """
    Получить популярные направления.
    
    - Количество заявок по направлению
    - Выручка
    - Рост по сравнению с прошлым периодом
    """
    try:
        return analytics_service.get_popular_destinations(days, limit)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/trends", response_model=List[SeasonalTrend])
async def get_seasonal_trends(
    months: int = Query(default=12, ge=1, le=24)
):
    """
    Получить сезонные тренды.
    
    Помесячная статистика:
    - Количество заявок
    - Средняя цена
    - Топ направлений за месяц
    """
    try:
        return analytics_service.get_seasonal_trends(months)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/forecast", response_model=List[DemandForecast])
async def get_demand_forecast(
    destination: Optional[str] = Query(default=None),
    horizon_days: int = Query(default=30, ge=7, le=90)
):
    """
    Получить прогноз спроса.
    
    Использует линейную регрессию на исторических данных.
    
    - **destination**: Конкретное направление (опционально)
    - **horizon_days**: Горизонт прогноза в днях
    
    Возвращает:
    - Текущий и прогнозируемый спрос
    - Тренд (растущий/падающий/стабильный)
    - Уверенность прогноза
    - Рекомендации
    """
    try:
        return analytics_service.forecast_demand(destination, horizon_days)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/price-optimization", response_model=List[PriceOptimization])
async def get_price_recommendations(
    tour_ids: Optional[List[int]] = Query(default=None)
):
    """
    Получить рекомендации по оптимизации цен.
    
    Анализирует:
    - Конверсию по турам
    - Среднерыночные цены по направлению
    - Эластичность спроса
    
    Возвращает рекомендации по изменению цен.
    """
    try:
        return analytics_service.get_price_recommendations(tour_ids)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/dashboard")
async def get_dashboard_data():
    """
    Получить данные для дашборда администратора.
    
    Краткая сводка ключевых метрик.
    """
    try:
        stats = analytics_service.get_request_statistics(30)
        destinations = analytics_service.get_popular_destinations(30, 5)
        forecasts = analytics_service.forecast_demand()[:3]
        
        return {
            "period": "last_30_days",
            "total_requests": stats.total_requests,
            "conversion_rate": stats.conversion_rate,
            "new_requests": stats.new_requests,
            "top_destinations": [d.destination for d in destinations],
            "rising_destinations": [
                f.destination for f in forecasts if f.trend == "rising"
            ],
            "alerts": _generate_alerts(stats, forecasts)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def _generate_alerts(
    stats: RequestStatistics, 
    forecasts: List[DemandForecast]
) -> List[dict]:
    """Генерация алертов для дашборда"""
    alerts = []
    
    if stats.new_requests > 10:
        alerts.append({
            "type": "warning",
            "message": f"Много необработанных заявок: {stats.new_requests}"
        })
    
    if stats.conversion_rate < 30:
        alerts.append({
            "type": "info",
            "message": f"Низкая конверсия ({stats.conversion_rate}%). Проверьте цены."
        })
    
    for forecast in forecasts:
        if forecast.trend == "falling" and forecast.confidence > 0.6:
            alerts.append({
                "type": "warning",
                "message": f"Падает спрос на {forecast.destination}"
            })
    
    return alerts
