from pydantic import BaseModel
from typing import Optional, List
from decimal import Decimal


class TourSchema(BaseModel):
    """Схема тура"""
    id: int
    name: str
    description: Optional[str] = None
    price: float
    duration_days: int
    destination_city: str
    active: bool = True
    
    class Config:
        from_attributes = True


class TourRecommendation(BaseModel):
    """Рекомендация тура"""
    tour_id: int
    tour_name: str
    destination: str
    price: float
    score: float  # Оценка релевантности (0-1)
    reason: str   # Причина рекомендации


class RecommendationRequest(BaseModel):
    """Запрос на рекомендации"""
    user_id: Optional[int] = None
    preferred_destinations: Optional[List[str]] = None
    min_price: Optional[float] = None
    max_price: Optional[float] = None
    preferred_duration: Optional[int] = None
    limit: int = 5


class RecommendationResponse(BaseModel):
    """Ответ с рекомендациями"""
    recommendations: List[TourRecommendation]
    total_tours_analyzed: int
    model_version: str = "1.0"
