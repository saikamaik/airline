from pydantic import BaseModel
from typing import List, Optional
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


class DemandForecast(BaseModel):
    """Прогноз спроса"""
    destination: str
    current_demand: int
    predicted_demand: int
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


class AnalyticsResponse(BaseModel):
    """Общий ответ аналитики"""
    period: str
    statistics: RequestStatistics
    popular_destinations: List[PopularDestination]
    seasonal_trends: List[SeasonalTrend]
    demand_forecasts: List[DemandForecast]
    price_recommendations: Optional[List[PriceOptimization]] = None
