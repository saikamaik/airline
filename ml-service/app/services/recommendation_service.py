"""Сервис рекомендаций туров на основе ML"""

import os
import logging
from typing import List, Optional
import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.metrics.pairwise import cosine_similarity
import joblib
from app.schemas.tour import TourRecommendation, RecommendationRequest
from app.services.data_service import DataService
from app.config import get_settings

logger = logging.getLogger(__name__)


class RecommendationService:
    """Сервис рекомендаций на основе контентной фильтрации"""
    
    def __init__(self):
        self.data_service = DataService()
        self.settings = get_settings()
        self.scaler = StandardScaler()
        self._tours_cache = None
        self._features_cache = None
        self._model_cache_path = os.path.join(self.settings.model_path, "recommendation_scaler.joblib")
        self._features_cache_path = os.path.join(self.settings.model_path, "recommendation_features.joblib")
        self._tours_hash_cache_path = os.path.join(self.settings.model_path, "tours_hash.txt")
        
        # Создаем директорию для моделей если её нет
        os.makedirs(self.settings.model_path, exist_ok=True)
        
        # Загружаем кэшированные модели при старте
        self._load_cached_models()
    
    def _load_tours(self) -> pd.DataFrame:
        """Загрузить туры с кэшированием"""
        if self._tours_cache is None:
            self._tours_cache = self.data_service.get_tours(active_only=True)
            # Проверяем, изменились ли данные
            self._check_and_update_cache()
        return self._tours_cache
    
    def _get_tours_hash(self, tours: pd.DataFrame) -> str:
        """Вычислить хэш туров для проверки изменений"""
        import hashlib
        # Используем ID и updated_at для определения изменений
        if tours.empty:
            return ""
        tour_ids = sorted(tours['id'].astype(str).tolist())
        hash_input = "|".join(tour_ids)
        return hashlib.md5(hash_input.encode()).hexdigest()
    
    def _check_and_update_cache(self):
        """Проверить, изменились ли данные, и обновить кэш при необходимости"""
        if self._tours_cache is None or self._tours_cache.empty:
            return
        
        current_hash = self._get_tours_hash(self._tours_cache)
        
        # Читаем сохраненный хэш
        saved_hash = None
        if os.path.exists(self._tours_hash_cache_path):
            try:
                with open(self._tours_hash_cache_path, 'r') as f:
                    saved_hash = f.read().strip()
            except Exception as e:
                logger.warning(f"Failed to read tours hash: {e}")
        
        # Если данные изменились, сбрасываем кэш
        if saved_hash != current_hash:
            logger.info("Tours data changed, invalidating feature cache")
            self._features_cache = None
            # Сохраняем новый хэш
            try:
                with open(self._tours_hash_cache_path, 'w') as f:
                    f.write(current_hash)
            except Exception as e:
                logger.warning(f"Failed to save tours hash: {e}")
    
    def _load_cached_models(self):
        """Загрузить кэшированные модели при старте"""
        try:
            if os.path.exists(self._model_cache_path):
                self.scaler = joblib.load(self._model_cache_path)
                logger.info("Loaded cached scaler model")
            else:
                logger.info("No cached scaler found, will train on first request")
        except Exception as e:
            logger.warning(f"Failed to load cached scaler: {e}, will train new model")
            self.scaler = StandardScaler()
    
    def _save_models(self):
        """Сохранить обученные модели"""
        try:
            joblib.dump(self.scaler, self._model_cache_path)
            logger.info(f"Saved scaler model to {self._model_cache_path}")
        except Exception as e:
            logger.error(f"Failed to save scaler model: {e}")
    
    def _prepare_features(self, tours: pd.DataFrame) -> np.ndarray:
        """Подготовить признаки для ML модели с кэшированием"""
        # Пытаемся загрузить из кэша
        if self._features_cache is not None:
            return self._features_cache
        
        # Пытаемся загрузить из файла
        if os.path.exists(self._features_cache_path):
            try:
                cached_data = joblib.load(self._features_cache_path)
                # Проверяем, что количество туров совпадает
                if cached_data.get('tours_count') == len(tours):
                    self._features_cache = cached_data['features']
                    logger.info("Loaded cached features")
                    return self._features_cache
            except Exception as e:
                logger.warning(f"Failed to load cached features: {e}")
        
        # Если кэша нет, обучаем модель
        logger.info("Training new features model")
        
        # Числовые признаки
        numeric_features = tours[['price', 'duration_days']].copy()
        
        # Нормализация (fit только если scaler не был обучен)
        if not hasattr(self.scaler, 'mean_') or self.scaler.mean_ is None:
            numeric_scaled = self.scaler.fit_transform(numeric_features)
            self._save_models()  # Сохраняем обученный scaler
        else:
            numeric_scaled = self.scaler.transform(numeric_features)
        
        # One-hot encoding для направлений
        destination_dummies = pd.get_dummies(
            tours['destination_city'], 
            prefix='dest'
        )
        
        # Объединяем признаки
        features = np.hstack([numeric_scaled, destination_dummies.values])
        self._features_cache = features
        
        # Сохраняем в кэш
        try:
            joblib.dump({
                'features': features,
                'tours_count': len(tours)
            }, self._features_cache_path)
            logger.info(f"Saved features cache to {self._features_cache_path}")
        except Exception as e:
            logger.warning(f"Failed to save features cache: {e}")
        
        return features
    
    def get_recommendations(
        self, 
        request: RecommendationRequest
    ) -> List[TourRecommendation]:
        """Получить рекомендации туров"""
        
        tours = self._load_tours()
        
        if tours.empty:
            return []
        
        # Фильтрация по цене
        filtered_tours = tours.copy()
        if request.min_price is not None:
            filtered_tours = filtered_tours[
                filtered_tours['price'] >= request.min_price
            ]
        if request.max_price is not None:
            filtered_tours = filtered_tours[
                filtered_tours['price'] <= request.max_price
            ]
        
        # Фильтрация по предпочитаемым направлениям
        if request.preferred_destinations:
            destination_filter = filtered_tours['destination_city'].isin(
                request.preferred_destinations
            )
            if destination_filter.any():
                filtered_tours = filtered_tours[destination_filter]
        
        if filtered_tours.empty:
            # Если фильтры слишком строгие, вернём топ популярных
            filtered_tours = tours.head(request.limit)
        
        # Получаем историю пользователя для персонализации
        user_preferences = self._get_user_preferences(request.user_id)
        
        # Скоринг туров
        scored_tours = self._score_tours(
            filtered_tours, 
            user_preferences,
            request
        )
        
        # Формируем рекомендации
        recommendations = []
        for _, tour in scored_tours.head(request.limit).iterrows():
            reason = self._generate_reason(tour, user_preferences, request)
            recommendations.append(
                TourRecommendation(
                    tour_id=int(tour['id']),
                    tour_name=tour['name'],
                    destination=tour['destination_city'],
                    price=float(tour['price']),
                    score=float(tour['score']),
                    reason=reason
                )
            )
        
        return recommendations
    
    def _get_user_preferences(self, user_id: Optional[int]) -> dict:
        """Извлечь предпочтения пользователя из истории"""
        if user_id is None:
            return {}
        
        try:
            user_requests = self.data_service.get_user_requests(user_id)
            if user_requests.empty:
                return {}
            
            return {
                'avg_price': user_requests['price'].mean(),
                'preferred_destinations': user_requests['destination_city'].value_counts().head(3).index.tolist(),
                'avg_duration': user_requests['duration_days'].mean(),
                'total_requests': len(user_requests)
            }
        except Exception:
            return {}
    
    def _score_tours(
        self, 
        tours: pd.DataFrame, 
        user_preferences: dict,
        request: RecommendationRequest
    ) -> pd.DataFrame:
        """Оценить туры по релевантности"""
        
        scored = tours.copy()
        scored['score'] = 0.5  # Базовый скор
        
        # Повышаем скор для предпочитаемых направлений
        if user_preferences.get('preferred_destinations'):
            dest_mask = scored['destination_city'].isin(
                user_preferences['preferred_destinations']
            )
            scored.loc[dest_mask, 'score'] += 0.2
        
        # Учитываем предпочтения по цене
        if user_preferences.get('avg_price'):
            avg_price = user_preferences['avg_price']
            # Чем ближе к средней цене пользователя, тем выше скор
            price_diff = abs(scored['price'] - avg_price) / avg_price
            scored['score'] += (1 - price_diff.clip(0, 1)) * 0.15
        
        # Учитываем предпочтения по длительности
        if request.preferred_duration:
            duration_diff = abs(
                scored['duration_days'] - request.preferred_duration
            ) / request.preferred_duration
            scored['score'] += (1 - duration_diff.clip(0, 1)) * 0.1
        
        # Нормализуем скор
        scored['score'] = scored['score'].clip(0, 1)
        
        # Сортируем по скору
        return scored.sort_values('score', ascending=False)
    
    def _generate_reason(
        self, 
        tour: pd.Series, 
        user_preferences: dict,
        request: RecommendationRequest
    ) -> str:
        """Сгенерировать причину рекомендации"""
        reasons = []
        
        if user_preferences.get('preferred_destinations'):
            if tour['destination_city'] in user_preferences['preferred_destinations']:
                reasons.append(
                    f"Вы ранее интересовались направлением {tour['destination_city']}"
                )
        
        if request.preferred_destinations:
            if tour['destination_city'] in request.preferred_destinations:
                reasons.append(f"Соответствует выбранному направлению")
        
        if request.preferred_duration:
            if abs(tour['duration_days'] - request.preferred_duration) <= 2:
                reasons.append(
                    f"Подходящая длительность ({tour['duration_days']} дней)"
                )
        
        if user_preferences.get('avg_price'):
            avg = user_preferences['avg_price']
            if abs(tour['price'] - avg) / avg < 0.2:
                reasons.append("Соответствует вашему бюджету")
        
        if not reasons:
            reasons.append("Популярное предложение")
        
        return ". ".join(reasons)
    
    def get_similar_tours(
        self, 
        tour_id: int, 
        limit: int = 5
    ) -> List[TourRecommendation]:
        """Найти похожие туры"""
        
        tours = self._load_tours()
        
        if tours.empty or tour_id not in tours['id'].values:
            return []
        
        features = self._prepare_features(tours)
        
        # Находим индекс тура
        tour_idx = tours[tours['id'] == tour_id].index[0]
        
        # Вычисляем косинусное сходство
        similarities = cosine_similarity(
            features[tour_idx:tour_idx+1], 
            features
        )[0]
        
        # Получаем индексы похожих туров (исключая сам тур)
        similar_indices = np.argsort(similarities)[::-1][1:limit+1]
        
        recommendations = []
        for idx in similar_indices:
            tour = tours.iloc[idx]
            recommendations.append(
                TourRecommendation(
                    tour_id=int(tour['id']),
                    tour_name=tour['name'],
                    destination=tour['destination_city'],
                    price=float(tour['price']),
                    score=float(similarities[idx]),
                    reason="Похож на выбранный тур"
                )
            )
        
        return recommendations
    
    def invalidate_cache(self):
        """Сбросить кэш (вызывать при обновлении туров)"""
        logger.info("Invalidating recommendation cache")
        self._tours_cache = None
        self._features_cache = None
        
        # Удаляем файлы кэша
        try:
            if os.path.exists(self._features_cache_path):
                os.remove(self._features_cache_path)
            if os.path.exists(self._tours_hash_cache_path):
                os.remove(self._tours_hash_cache_path)
            logger.info("Cache files removed")
        except Exception as e:
            logger.warning(f"Failed to remove cache files: {e}")