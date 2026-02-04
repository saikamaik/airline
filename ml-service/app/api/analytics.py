"""API эндпоинты для аналитики"""

import logging
import pandas as pd
from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional
from app.schemas.analytics import (
    RequestStatistics,
    PopularDestination,
    SeasonalTrend,
    DemandForecast,
    DemandForecastTableRow,
    PriceOptimization,
    AnalyticsResponse,
    TimeRange
)
from app.services.analytics_service import AnalyticsService
from app.exceptions import DatabaseError, ServiceUnavailableError, DataValidationError

logger = logging.getLogger(__name__)

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
    except DatabaseError as e:
        logger.error(f"Database error in get_full_analytics: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_full_analytics: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_full_analytics: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


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
    except DatabaseError as e:
        logger.error(f"Database error in get_statistics: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_statistics: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_statistics: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


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
    except DatabaseError as e:
        logger.error(f"Database error in get_popular_destinations: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_popular_destinations: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_popular_destinations: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.get("/all-destinations")
async def get_all_destinations():
    """
    Получить все направления из базы данных.
    
    Возвращает список всех уникальных направлений (destination_city) из таблицы tours.
    """
    try:
        tours = analytics_service.data_service.get_tours()
        if tours.empty:
            return []
        
        destinations = tours['destination_city'].dropna().unique().tolist()
        return sorted([d for d in destinations if d])  # Убираем пустые значения и сортируем
    except DatabaseError as e:
        logger.error(f"Database error in get_all_destinations: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_all_destinations: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_all_destinations: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


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
    except DatabaseError as e:
        logger.error(f"Database error in get_seasonal_trends: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_seasonal_trends: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_seasonal_trends: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.get("/forecast/table", response_model=List[DemandForecastTableRow])
async def get_demand_forecast_table():
    """
    Получить прогноз спроса в табличном формате.
    
    Возвращает данные в формате таблицы:
    - Направление
    - Текущий спрос (заявок/неделю)
    - Прогноз (заявок/неделю)
    - Изменение (%)
    - Тренд (Растущий/Стабильный/Падающий)
    - Уверенность
    - Рекомендация
    """
    try:
        result = analytics_service.get_demand_forecast_table()
        logger.info(f"Forecast table returned {len(result)} rows")
        return result
    except DatabaseError as e:
        logger.error(f"Database error in get_demand_forecast_table: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_demand_forecast_table: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_demand_forecast_table: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.get("/forecast")
async def get_demand_forecast(
    destination: Optional[str] = Query(default=None),
    horizon_months: int = Query(default=6, ge=1, le=12)
):
    """
    Получить прогноз спроса по месяцам.
    
    Использует линейную регрессию на исторических данных.
    
    - **destination**: Конкретное направление (опционально)
    - **horizon_months**: Горизонт прогноза в месяцах (1-12)
    
    Возвращает:
    - Текущий и прогнозируемый спрос по месяцам
    - Тренд (растущий/падающий/стабильный)
    - Уверенность прогноза
    - Рекомендации
    """
    try:
        forecasts = analytics_service.forecast_demand(destination, horizon_months)
        
        # Преобразуем в формат, ожидаемый фронтендом
        if not forecasts:
            return {
                "destination": destination,
                "forecast": [],
                "recommendations": ["Недостаточно данных для прогноза. Нужно минимум 4 заявки за последние 6 месяцев."]
            }
        
        # Группируем прогнозы по месяцам (используем текущую дату + месяцы вперед)
        from datetime import datetime
        from dateutil.relativedelta import relativedelta
        forecast_data = []
        recommendations = []
        total_predicted_revenue = 0.0
        
        base_date = datetime.now().replace(day=1)  # Первый день текущего месяца
        
        # Если указано конкретное направление, используем его прогноз
        # Иначе берем первый доступный прогноз
        if forecasts:
            f = forecasts[0]  # Используем первый прогноз
            
            # Генерируем прогноз на несколько месяцев вперед
            # Используем линейную экстраполяцию для каждого месяца
            for i in range(min(horizon_months, 12)):  # Максимум 12 месяцев
                forecast_date = base_date + relativedelta(months=i+1)  # Следующий месяц
                
                # Экстраполируем спрос: базовый спрос + тренд * номер месяца
                # Если тренд растущий, увеличиваем спрос, если падающий - уменьшаем
                trend_multiplier = 1.0
                if f.trend == "rising":
                    trend_multiplier = 1.0 + (i * 0.1)  # Рост на 10% каждый месяц
                elif f.trend == "falling":
                    trend_multiplier = max(0.5, 1.0 - (i * 0.1))  # Падение на 10% каждый месяц
                
                monthly_demand = max(0, int(f.predicted_demand * trend_multiplier))
                monthly_revenue = f.predicted_revenue * trend_multiplier
                
                forecast_data.append({
                    "date": forecast_date.strftime("%Y-%m"),
                    "predictedDemand": monthly_demand,
                    "predictedRevenue": round(monthly_revenue, 2),
                    "confidence": max(0.3, f.confidence - (i * 0.05))  # Уверенность снижается со временем
                })
                total_predicted_revenue += monthly_revenue
                
            if f.recommendation and f.recommendation not in recommendations:
                recommendations.append(f.recommendation)
        
        # Добавляем общую сводку по направлениям (топ-5 по умолчанию)
        destination_summary = []
        for f in forecasts[:5]:  # Топ-5 направлений по умолчанию
            destination_summary.append({
                "destination": f.destination,
                "predictedRevenue": f.predicted_revenue,
                "predictedDemand": f.predicted_demand,
                "trend": f.trend
            })
        
        return {
            "destination": destination or (forecasts[0].destination if forecasts else None),
            "forecast": forecast_data,
            "totalPredictedRevenue": round(total_predicted_revenue, 2),
            "destinationBreakdown": destination_summary,
            "recommendations": list(set(recommendations))[:5],  # Уникальные рекомендации, максимум 5
            "hasMore": len(forecasts) > 5  # Флаг наличия дополнительных данных
        }
    except DatabaseError as e:
        logger.error(f"Database error in get_demand_forecast: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_demand_forecast: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_demand_forecast: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.get("/clusters")
async def get_tour_clusters(
    n_clusters: int = Query(default=3, ge=2, le=10)
):
    """
    Кластеризация туров по характеристикам.
    
    Группирует туры по:
    - Цене
    - Длительности
    - Популярности
    - Конверсии
    
    Возвращает кластеры с описанием типа туров.
    """
    try:
        clusters = analytics_service.get_tour_clusters(n_clusters)
        return clusters
    except DatabaseError as e:
        logger.error(f"Database error in get_tour_clusters: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_tour_clusters: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_tour_clusters: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.get("/model-metrics")
async def get_model_metrics(
    limit: int = Query(default=5, ge=1, le=50, description="Количество направлений для отображения")
):
    """
    Получить метрики качества ML-моделей.
    
    Показывает точность каждой модели в ансамбле:
    - Linear Regression
    - Random Forest
    - Gradient Boosting
    - Ансамбль (взвешенное среднее)
    
    Args:
        limit: Количество направлений для отображения (по умолчанию 5)
    """
    try:
        # Получаем метрики для всех направлений
        requests = analytics_service.data_service.get_requests(180)
        if requests.empty:
            return []
        
        destinations = requests['destination_city'].value_counts()
        destinations_to_analyze = destinations[destinations >= 4].index.tolist()[:limit]  # Ограничиваем по limit
        
        metrics_list = []
        for dest in destinations_to_analyze:
            if pd.isna(dest):
                continue
            
            dest_requests = requests[requests['destination_city'] == dest]
            if len(dest_requests) < 4:
                continue
            
            # Получаем метрики через обучение/загрузку модели
            from datetime import datetime
            dest_requests_copy = dest_requests.copy()
            dest_requests_copy['created_at'] = pd.to_datetime(dest_requests_copy['created_at'])
            dest_requests_copy['week'] = dest_requests_copy['created_at'].dt.isocalendar().week
            
            def get_season(month):
                if month in [12, 1, 2]:
                    return 0
                elif month in [3, 4, 5]:
                    return 1
                elif month in [6, 7, 8]:
                    return 2
                else:
                    return 3
            
            dest_requests_copy['month'] = dest_requests_copy['created_at'].dt.month
            dest_requests_copy['season'] = dest_requests_copy['month'].apply(get_season)
            
            weekly_data = dest_requests_copy.groupby(['week', 'season']).size().reset_index(name='count')
            if len(weekly_data) < 3:
                continue
            
            X = weekly_data[['week', 'season']].values
            y = weekly_data['count'].values
            
            _, metrics = analytics_service._get_or_train_ensemble_model(dest, X, y)
            
            from app.schemas.analytics import ModelMetrics
            metrics_list.append(
                ModelMetrics(
                    destination=dest,
                    **metrics
                )
            )
        
        # Проверяем есть ли еще направления
        all_destinations = destinations[destinations >= 4].index.tolist()
        has_more = len(all_destinations) > limit
        
        return {
            "metrics": metrics_list,
            "hasMore": has_more,
            "total": len(all_destinations)
        }
    except DatabaseError as e:
        logger.error(f"Database error in get_model_metrics: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_model_metrics: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_model_metrics: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.get("/anomalies")
async def get_anomalous_tours():
    """
    Детекция аномальных туров с помощью ML.
    
    Находит туры с необычными паттернами:
    - Высокий спрос, но низкая цена
    - Низкий спрос, но высокая цена
    - Необычные паттерны (высокая отмена)
    
    Возвращает рекомендации по корректировке цен.
    """
    try:
        anomalies = analytics_service.get_anomalous_tours()
        return anomalies
    except DatabaseError as e:
        logger.error(f"Database error in get_anomalous_tours: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_anomalous_tours: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_anomalous_tours: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


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
    except DatabaseError as e:
        logger.error(f"Database error in get_price_recommendations: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_price_recommendations: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_price_recommendations: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


@router.get("/dashboard")
async def get_dashboard_data():
    """
    Получить данные для дашборда администратора.
    
    Краткая сводка ключевых метрик.
    """
    try:
        stats = analytics_service.get_request_statistics(30)
        destinations = analytics_service.get_popular_destinations(30, 10)  # Топ-10 направлений по умолчанию
        forecasts = analytics_service.forecast_demand()[:5]  # Топ-5 прогнозов по умолчанию
        
        # Рассчитываем общую выручку и средний чек
        total_revenue = sum(d.revenue for d in destinations)
        completed_count = stats.completed
        average_check = total_revenue / completed_count if completed_count > 0 else 0.0
        
        # Получаем прогноз выручки на следующий месяц
        next_month_forecasts = analytics_service.forecast_demand(horizon_months=1)
        next_month_revenue = sum(f.predicted_revenue for f in next_month_forecasts)
        
        # Детальный прогноз по направлениям (топ-5 по умолчанию)
        revenue_by_destination = []
        for f in next_month_forecasts[:5]:  # Топ-5 направлений по умолчанию
            revenue_by_destination.append({
                "destination": f.destination,
                "predictedRevenue": f.predicted_revenue,
                "predictedDemand": f.predicted_demand,
                "currentRevenue": f.current_revenue,
                "trend": f.trend
            })
        
        return {
            "period": "last_30_days",
            "totalRequests": stats.total_requests,
            "totalRevenue": round(total_revenue, 2),
            "averageRequestValue": round(average_check, 2),
            "nextMonthPredictedRevenue": round(next_month_revenue, 2),
            "revenueByDestination": revenue_by_destination,
            "requestsByStatus": {
                "new": stats.new_requests,
                "in_progress": stats.in_progress,
                "completed": stats.completed,
                "cancelled": stats.cancelled
            },
            "topDestinations": [{"destination": d.destination, "count": d.request_count} for d in destinations],
            "recentTrends": [],
            "rising_destinations": [
                f.destination for f in forecasts if f.trend == "rising"
            ],
            "alerts": _generate_alerts(stats, forecasts)
        }
    except DatabaseError as e:
        logger.error(f"Database error in get_dashboard_data: {e}")
        raise HTTPException(status_code=503, detail="Database service unavailable")
    except ServiceUnavailableError as e:
        logger.error(f"Service unavailable in get_dashboard_data: {e}")
        raise HTTPException(status_code=503, detail="Service temporarily unavailable")
    except Exception as e:
        logger.error(f"Unexpected error in get_dashboard_data: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")


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
