"""API эндпоинты для рекомендаций"""

from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional
from app.schemas.tour import (
    RecommendationRequest, 
    RecommendationResponse,
    TourRecommendation
)
from app.services.recommendation_service import RecommendationService

router = APIRouter(prefix="/recommendations", tags=["Рекомендации"])

recommendation_service = RecommendationService()


@router.post("/", response_model=RecommendationResponse)
async def get_recommendations(request: RecommendationRequest):
    """
    Получить персонализированные рекомендации туров.
    
    - **user_id**: ID пользователя для персонализации (опционально)
    - **preferred_destinations**: Список предпочитаемых направлений
    - **min_price/max_price**: Диапазон цен
    - **preferred_duration**: Желаемая длительность тура
    - **limit**: Количество рекомендаций (по умолчанию 5)
    """
    try:
        recommendations = recommendation_service.get_recommendations(request)
        
        # Получаем общее количество туров для информации
        tours = recommendation_service._load_tours()
        total_analyzed = len(tours) if tours is not None else 0
        
        return RecommendationResponse(
            recommendations=recommendations,
            total_tours_analyzed=total_analyzed,
            model_version="1.0"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/similar/{tour_id}", response_model=List[TourRecommendation])
async def get_similar_tours(
    tour_id: int,
    limit: int = Query(default=5, ge=1, le=20)
):
    """
    Получить похожие туры.
    
    Использует косинусное сходство для поиска туров 
    с похожими характеристиками (цена, длительность, направление).
    """
    try:
        similar = recommendation_service.get_similar_tours(tour_id, limit)
        if not similar:
            raise HTTPException(
                status_code=404, 
                detail=f"Тур с ID {tour_id} не найден или нет похожих туров"
            )
        return similar
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/popular", response_model=List[TourRecommendation])
async def get_popular_tours(
    limit: int = Query(default=10, ge=1, le=50)
):
    """
    Получить популярные туры.
    
    Возвращает туры с наибольшим количеством заявок.
    """
    try:
        # Используем рекомендации без персонализации
        request = RecommendationRequest(limit=limit)
        recommendations = recommendation_service.get_recommendations(request)
        return recommendations
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/invalidate-cache")
async def invalidate_cache():
    """
    Сбросить кэш рекомендаций.
    
    Вызывать после добавления/изменения туров.
    """
    recommendation_service.invalidate_cache()
    return {"message": "Кэш успешно сброшен"}
