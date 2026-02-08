from pydantic import BaseModel, ConfigDict
from typing import List, Optional, Dict
from datetime import date
from enum import Enum


class TimeRange(str, Enum):
    WEEK = "week"
    MONTH = "month"
    QUARTER = "quarter"
    YEAR = "year"


class RequestStatistics(BaseModel):
    """Статистика по заявкам"""
    total_requests: int
    new_requests: int
    in_progress: int
    completed: int
    cancelled: int
    conversion_rate: float  # % завершённых от общего числа
    avg_processing_time_hours: Optional[float] = None


class PopularDestination(BaseModel):
    """Популярное направление"""
    destination: str
    request_count: int
    revenue: float
    growth_percent: float  # Рост по сравнению с прошлым периодом


class SeasonalTrend(BaseModel):
    """Сезонный тренд"""
    month: int
    month_name: str
    request_count: int
    avg_price: float
    top_destinations: List[str]
    is_forecast: bool = False  # Маркер: True если это прогноз, False если исторические данные


class DemandForecast(BaseModel):
    """Прогноз спроса"""
    destination: str
    current_demand: int
    predicted_demand: int
    current_revenue: float  # Текущая выручка
    predicted_revenue: float  # Прогнозируемая выручка
    confidence: float  # Уверенность прогноза (0-1)
    trend: str  # "rising", "falling", "stable"
    recommendation: str  # Рекомендация для менеджера


class PriceOptimization(BaseModel):
    """Оптимизация цены"""
    tour_id: int
    tour_name: str
    current_price: float
    recommended_price: float
    expected_demand_change: float
    reasoning: str


class AnomalousTour(BaseModel):
    """Аномальный тур (требует внимания)"""
    tour_id: int
    tour_name: str
    destination: str
    current_price: float
    anomaly_type: str  # "high_demand_low_price", "low_demand_high_price", "unusual_pattern"
    demand_score: float  # Оценка спроса (0-1)
    price_score: float  # Оценка цены относительно спроса (0-1)
    recommendation: str  # Рекомендация по цене
    expected_revenue_impact: float  # Ожидаемое влияние на выручку при изменении цены


class TourCluster(BaseModel):
    """Кластер туров"""
    cluster_id: int
    cluster_type: str
    description: str
    tours: List[Dict]
    avg_price: float
    avg_duration: float
    total_popularity: int
    avg_conversion: float


class ModelMetrics(BaseModel):
    """Метрики качества моделей"""
    destination: str
    linear_r2: float
    random_forest_r2: float
    gradient_boosting_r2: float
    ensemble_r2: float
    ensemble_mae: float
    ensemble_rmse: float
    avg_r2: float
    weights: Dict[str, float]


class DemandForecastTableRow(BaseModel):
    """Строка таблицы прогноза спроса"""
    destination: str
    current_demand_per_week: int  # Текущий спрос (заявок/неделю)
    predicted_demand_per_week: int  # Прогноз (заявок/неделю)
    change_percent: float  # Изменение в процентах
    trend: str  # "Растущий", "Стабильный", "Падающий"
    confidence: float  # Уверенность (0-1)
    recommendation: str  # Рекомендация


class AnalyticsResponse(BaseModel):
    """Общий ответ аналитики"""
    # Настройка Pydantic для разрешения использования model_ префикса
    model_config = ConfigDict(protected_namespaces=('settings_',))
    
    period: str
    statistics: RequestStatistics
    popular_destinations: List[PopularDestination]
    seasonal_trends: List[SeasonalTrend]
    demand_forecasts: List[DemandForecast]
    price_recommendations: Optional[List[PriceOptimization]] = None
    tour_clusters: Optional[List[TourCluster]] = None
    model_metrics: Optional[List[ModelMetrics]] = None