"""Сервис аналитики и прогнозирования"""

from typing import List, Optional
from datetime import datetime, timedelta
import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
from app.schemas.analytics import (
    RequestStatistics, 
    PopularDestination,
    SeasonalTrend,
    DemandForecast,
    PriceOptimization,
    AnalyticsResponse,
    TimeRange
)
from app.services.data_service import DataService


class AnalyticsService:
    """Сервис аналитики и прогнозирования"""
    
    MONTH_NAMES = {
        1: "Январь", 2: "Февраль", 3: "Март", 4: "Апрель",
        5: "Май", 6: "Июнь", 7: "Июль", 8: "Август",
        9: "Сентябрь", 10: "Октябрь", 11: "Ноябрь", 12: "Декабрь"
    }
    
    def __init__(self):
        self.data_service = DataService()
    
    def get_request_statistics(
        self, 
        days: int = 30
    ) -> RequestStatistics:
        """Получить статистику по заявкам"""
        
        requests = self.data_service.get_requests(days)
        
        if requests.empty:
            return RequestStatistics(
                total_requests=0,
                new_requests=0,
                in_progress=0,
                completed=0,
                cancelled=0,
                conversion_rate=0.0
            )
        
        total = len(requests)
        status_counts = requests['status'].value_counts()
        
        new = status_counts.get('NEW', 0)
        in_progress = status_counts.get('IN_PROGRESS', 0)
        completed = status_counts.get('COMPLETED', 0)
        cancelled = status_counts.get('CANCELLED', 0)
        
        # Конверсия = завершённые / (всего - отменённые)
        conversion_base = total - cancelled
        conversion_rate = (completed / conversion_base * 100) if conversion_base > 0 else 0
        
        # Среднее время обработки (для завершённых)
        avg_time = None
        completed_requests = requests[requests['status'] == 'COMPLETED']
        if not completed_requests.empty and 'updated_at' in completed_requests.columns:
            time_diff = (
                pd.to_datetime(completed_requests['updated_at']) - 
                pd.to_datetime(completed_requests['created_at'])
            )
            avg_time = time_diff.mean().total_seconds() / 3600  # В часах
        
        return RequestStatistics(
            total_requests=total,
            new_requests=int(new),
            in_progress=int(in_progress),
            completed=int(completed),
            cancelled=int(cancelled),
            conversion_rate=round(conversion_rate, 2),
            avg_processing_time_hours=round(avg_time, 2) if avg_time else None
        )
    
    def get_popular_destinations(
        self, 
        days: int = 90,
        limit: int = 10
    ) -> List[PopularDestination]:
        """Получить популярные направления"""
        
        current_stats = self.data_service.get_destination_stats(days)
        previous_stats = self.data_service.get_destination_stats(days * 2)
        
        if current_stats.empty:
            return []
        
        destinations = []
        for _, row in current_stats.head(limit).iterrows():
            dest = row['destination_city']
            
            # Вычисляем рост
            prev_count = 0
            if not previous_stats.empty:
                prev_row = previous_stats[
                    previous_stats['destination_city'] == dest
                ]
                if not prev_row.empty:
                    prev_count = prev_row.iloc[0]['request_count']
            
            current_count = row['request_count']
            if prev_count > 0:
                growth = ((current_count - prev_count) / prev_count) * 100
            else:
                growth = 100.0 if current_count > 0 else 0.0
            
            destinations.append(
                PopularDestination(
                    destination=dest,
                    request_count=int(row['request_count']),
                    revenue=float(row['total_revenue'] or 0),
                    growth_percent=round(growth, 2)
                )
            )
        
        return destinations
    
    def get_seasonal_trends(self, months: int = 12) -> List[SeasonalTrend]:
        """Получить сезонные тренды"""
        
        monthly_stats = self.data_service.get_monthly_stats(months)
        
        if monthly_stats.empty:
            return []
        
        trends = []
        for _, row in monthly_stats.iterrows():
            month_num = row['month'].month if hasattr(row['month'], 'month') else 1
            
            destinations = row.get('destinations', [])
            if isinstance(destinations, str):
                destinations = [d.strip() for d in destinations.strip('{}').split(',')]
            
            trends.append(
                SeasonalTrend(
                    month=month_num,
                    month_name=self.MONTH_NAMES.get(month_num, str(month_num)),
                    request_count=int(row['request_count']),
                    avg_price=float(row['avg_price'] or 0),
                    top_destinations=destinations[:3] if destinations else []
                )
            )
        
        return trends
    
    def forecast_demand(
        self, 
        destination: Optional[str] = None,
        horizon_days: int = 30
    ) -> List[DemandForecast]:
        """Прогноз спроса на направления"""
        
        # Получаем исторические данные
        requests = self.data_service.get_requests(180)  # 6 месяцев
        
        if requests.empty:
            return []
        
        # Группируем по направлению и неделе
        requests['week'] = pd.to_datetime(requests['created_at']).dt.isocalendar().week
        
        forecasts = []
        destinations_to_analyze = [destination] if destination else \
            requests['destination_city'].unique()[:10]
        
        for dest in destinations_to_analyze:
            if pd.isna(dest):
                continue
                
            dest_requests = requests[requests['destination_city'] == dest]
            
            if len(dest_requests) < 4:  # Недостаточно данных
                continue
            
            weekly_counts = dest_requests.groupby('week').size().reset_index(name='count')
            
            if len(weekly_counts) < 3:
                continue
            
            # Простая линейная регрессия для прогноза
            X = weekly_counts.index.values.reshape(-1, 1)
            y = weekly_counts['count'].values
            
            model = LinearRegression()
            model.fit(X, y)
            
            # Прогноз на следующий период
            next_period = np.array([[len(weekly_counts)]])
            predicted = max(0, int(model.predict(next_period)[0]))
            current = int(weekly_counts['count'].iloc[-1])
            
            # Определяем тренд
            coef = model.coef_[0]
            if coef > 0.5:
                trend = "rising"
                recommendation = f"Рекомендуется увеличить предложение туров в {dest}"
            elif coef < -0.5:
                trend = "falling"
                recommendation = f"Рассмотрите скидки или акции для {dest}"
            else:
                trend = "stable"
                recommendation = f"Поддерживайте текущий уровень предложения для {dest}"
            
            # Оценка уверенности
            r2 = model.score(X, y)
            confidence = max(0.3, min(0.95, r2))
            
            forecasts.append(
                DemandForecast(
                    destination=dest,
                    current_demand=current,
                    predicted_demand=predicted,
                    confidence=round(confidence, 2),
                    trend=trend,
                    recommendation=recommendation
                )
            )
        
        return sorted(forecasts, key=lambda x: x.predicted_demand, reverse=True)
    
    def get_price_recommendations(
        self, 
        tour_ids: Optional[List[int]] = None
    ) -> List[PriceOptimization]:
        """Рекомендации по оптимизации цен"""
        
        tours = self.data_service.get_tours()
        requests = self.data_service.get_requests(90)
        
        if tours.empty:
            return []
        
        if tour_ids:
            tours = tours[tours['id'].isin(tour_ids)]
        
        recommendations = []
        
        for _, tour in tours.iterrows():
            tour_requests = requests[requests['tour_id'] == tour['id']]
            
            current_price = float(tour['price'])
            
            # Анализируем конверсию и спрос
            if len(tour_requests) >= 5:
                completed = len(tour_requests[tour_requests['status'] == 'COMPLETED'])
                conversion = completed / len(tour_requests)
                
                # Эвристика для рекомендации цены
                if conversion > 0.7:  # Высокая конверсия
                    recommended = current_price * 1.1  # +10%
                    change = 0.15  # Ожидаемое снижение спроса
                    reason = "Высокая конверсия позволяет увеличить цену"
                elif conversion < 0.3:  # Низкая конверсия
                    recommended = current_price * 0.9  # -10%
                    change = 0.2  # Ожидаемый рост спроса
                    reason = "Снижение цены может увеличить конверсию"
                else:
                    recommended = current_price
                    change = 0
                    reason = "Текущая цена оптимальна"
            else:
                # Недостаточно данных - смотрим на среднюю по рынку
                dest_avg = requests[
                    requests['destination_city'] == tour['destination_city']
                ]['price'].mean()
                
                if pd.isna(dest_avg):
                    continue
                
                if current_price > dest_avg * 1.2:
                    recommended = dest_avg * 1.1
                    change = 0.1
                    reason = "Цена выше среднерыночной для данного направления"
                elif current_price < dest_avg * 0.8:
                    recommended = dest_avg * 0.9
                    change = -0.05
                    reason = "Есть потенциал для увеличения цены"
                else:
                    continue
            
            if abs(recommended - current_price) > 100:  # Минимальная разница
                recommendations.append(
                    PriceOptimization(
                        tour_id=int(tour['id']),
                        tour_name=tour['name'],
                        current_price=current_price,
                        recommended_price=round(recommended, 2),
                        expected_demand_change=round(change, 2),
                        reasoning=reason
                    )
                )
        
        return recommendations
    
    def get_full_analytics(
        self, 
        time_range: TimeRange = TimeRange.MONTH
    ) -> AnalyticsResponse:
        """Получить полную аналитику"""
        
        days_map = {
            TimeRange.WEEK: 7,
            TimeRange.MONTH: 30,
            TimeRange.QUARTER: 90,
            TimeRange.YEAR: 365
        }
        
        days = days_map.get(time_range, 30)
        
        return AnalyticsResponse(
            period=time_range.value,
            statistics=self.get_request_statistics(days),
            popular_destinations=self.get_popular_destinations(days),
            seasonal_trends=self.get_seasonal_trends(12),
            demand_forecasts=self.forecast_demand(),
            price_recommendations=self.get_price_recommendations()
        )
