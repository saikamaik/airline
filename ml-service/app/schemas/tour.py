from pydantic import BaseModel, field_validator, model_validator, ConfigDict
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
    
    @field_validator('user_id')
    @classmethod
    def validate_user_id(cls, v):
        if v is not None and v < 1:
            raise ValueError('user_id must be positive integer')
        return v
    
    @field_validator('min_price', 'max_price')
    @classmethod
    def validate_price(cls, v):
        if v is not None and v < 0:
            raise ValueError('price must be non-negative')
        return v
    
    @field_validator('preferred_duration')
    @classmethod
    def validate_duration(cls, v):
        if v is not None and (v < 1 or v > 365):
            raise ValueError('preferred_duration must be between 1 and 365 days')
        return v
    
    @field_validator('limit')
    @classmethod
    def validate_limit(cls, v):
        if v < 1 or v > 100:
            raise ValueError('limit must be between 1 and 100')
        return v
    
    @model_validator(mode='after')
    def validate_price_range(self):
        if self.min_price is not None and self.max_price is not None:
            if self.min_price > self.max_price:
                raise ValueError('min_price cannot be greater than max_price')
        return self


class RecommendationResponse(BaseModel):
    """Ответ с рекомендациями"""
    model_config = ConfigDict(protected_namespaces=())
    
    recommendations: List[TourRecommendation]
    total_tours_analyzed: int
    model_version: str = "1.0"
