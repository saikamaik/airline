"""Сервис аналитики и прогнозирования"""

import os
import logging
from typing import List, Optional, Dict
from datetime import datetime, timedelta
import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor, IsolationForest
from sklearn.cluster import KMeans
import joblib
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
from app.services.data_service import DataService
from app.config import get_settings

logger = logging.getLogger(__name__)


class AnalyticsService:
    """Сервис аналитики и прогнозирования"""
    
    MONTH_NAMES = {
        1: "Январь", 2: "Февраль", 3: "Март", 4: "Апрель",
        5: "Май", 6: "Июнь", 7: "Июль", 8: "Август",
        9: "Сентябрь", 10: "Октябрь", 11: "Ноябрь", 12: "Декабрь"
    }
    
    def __init__(self):
        self.data_service = DataService()
        self.settings = get_settings()
        self._forecast_models_cache: Dict[str, LinearRegression] = {}
        self._forecast_cache_path = os.path.join(self.settings.model_path, "forecast_models")
        
        # Создаем директорию для моделей если её нет
        os.makedirs(self._forecast_cache_path, exist_ok=True)
        
        # Загружаем кэшированные модели прогноза при старте
        self._load_cached_forecast_models()
    
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
                    top_destinations=destinations if destinations else []  # Показываем все топ направления
                )
            )
        
        return trends
    
    def forecast_seasonal_trends(self, forecast_months: int = 3) -> List[SeasonalTrend]:
        """
        Прогнозирование сезонных трендов на N месяцев вперед
        
        Args:
            forecast_months: Количество месяцев для прогноза (по умолчанию 3)
            
        Returns:
            Список прогнозов по месяцам с предсказанным спросом и ценами
        """
        try:
            logger.info(f"Forecasting seasonal trends for {forecast_months} months ahead")
            
            # Получаем исторические данные за последние 12 месяцев для учета сезонности
            monthly_stats = self.data_service.get_monthly_stats(12)
            
            logger.info(f"Retrieved {len(monthly_stats)} months of historical data")
            
            if monthly_stats.empty or len(monthly_stats) < 2:
                logger.warning(f"Insufficient historical data for seasonal forecast: got {len(monthly_stats)} months, need at least 2")
                return []
            
            # Подготавливаем данные для модели
            monthly_stats = monthly_stats.sort_values('month')
        except Exception as e:
            logger.error(f"Error loading historical data for seasonal forecast: {e}", exc_info=True)
            return []
        
        try:
            # Создаем признаки: номер месяца в году (1-12) для учета сезонности
            monthly_stats['month_num'] = monthly_stats['month'].apply(
                lambda x: x.month if hasattr(x, 'month') else 1
            )
            
            # Создаем временной индекс (порядковый номер месяца)
            monthly_stats['time_index'] = range(len(monthly_stats))
            
            # Вычисляем тренд (рост/спад) по последним месяцам
            # Берем последние 3 месяца для определения тренда
            recent_months = min(3, len(monthly_stats))
            recent_demand = monthly_stats['request_count'].tail(recent_months).values
            if len(recent_demand) >= 2:
                # Линейный тренд: положительный = рост, отрицательный = спад
                trend_slope = (recent_demand[-1] - recent_demand[0]) / len(recent_demand)
            else:
                trend_slope = 0
            
            # Вычисляем средние значения по месяцам года для сезонности
            monthly_averages = {}
            for month in range(1, 13):
                month_data = monthly_stats[monthly_stats['month_num'] == month]
                if not month_data.empty:
                    monthly_averages[month] = {
                        'demand': month_data['request_count'].mean(),
                        'price': month_data['avg_price'].mean()
                    }
            
            # Общие средние значения
            overall_avg_demand = monthly_stats['request_count'].mean()
            overall_avg_price = monthly_stats['avg_price'].mean()
            
            # Последние значения (базовая линия)
            last_demand = monthly_stats['request_count'].iloc[-1]
            last_price = monthly_stats['avg_price'].iloc[-1]
            
            logger.info(f"Trend slope: {trend_slope:.2f}, Last demand: {last_demand}, Avg demand: {overall_avg_demand:.2f}")
            
            # Генерируем прогнозы на следующие месяцы
            forecasts = []
            current_date = datetime.now()
            
            for i in range(1, forecast_months + 1):
                forecast_date = current_date + timedelta(days=30 * i)
                month_num = forecast_date.month
                
                # Прогноз на основе сезонности + тренда
                if month_num in monthly_averages:
                    # Используем сезонный паттерн
                    seasonal_demand = monthly_averages[month_num]['demand']
                    seasonal_price = monthly_averages[month_num]['price']
                    
                    # Добавляем тренд: продолжаем тенденцию последних месяцев
                    trend_adjustment = trend_slope * i * 0.8  # Затухающий тренд
                    predicted_demand = seasonal_demand + trend_adjustment
                    
                    # Цена следует за спросом, но более стабильно
                    price_trend = trend_slope * 50 * i  # Цена меняется медленнее
                    predicted_price = seasonal_price + price_trend
                else:
                    # Fallback: используем общие средние + тренд
                    predicted_demand = overall_avg_demand + (trend_slope * i * 0.8)
                    predicted_price = overall_avg_price + (trend_slope * 50 * i)
                
                # Обеспечиваем разумные границы
                predicted_demand = max(1, int(predicted_demand))
                predicted_price = max(10000, float(predicted_price))
                
                # Получаем топ направления на основе исторических данных для этого месяца
                same_month_data = monthly_stats[monthly_stats['month_num'] == month_num]
                
                top_destinations = []
                if not same_month_data.empty and 'destinations' in same_month_data.columns:
                    # Берем последние данные для этого месяца
                    try:
                        destinations = same_month_data.iloc[-1]['destinations']
                        if isinstance(destinations, list):
                            top_destinations = destinations[:3]  # Берем топ-3 направления
                        elif isinstance(destinations, str):
                            # На случай если преобразование не сработало
                            top_destinations = [d.strip() for d in destinations.split(',') if d.strip()][:3]
                    except Exception as e:
                        logger.warning(f"Failed to extract destinations for month {month_num}: {e}")
                        top_destinations = []
                
                forecasts.append(
                    SeasonalTrend(
                        month=month_num,
                        month_name=f"{self.MONTH_NAMES.get(month_num, str(month_num))} (прогноз)",
                        request_count=predicted_demand,
                        avg_price=round(predicted_price, 2),
                        top_destinations=top_destinations,
                        is_forecast=True  # Маркер что это прогноз
                    )
                )
            
            logger.info(f"Generated {len(forecasts)} seasonal forecasts")
            return forecasts
        except Exception as e:
            logger.error(f"Error generating seasonal forecast: {e}", exc_info=True)
            return []
    
    def forecast_demand(
        self, 
        destination: Optional[str] = None,
        horizon_months: int = 6
    ) -> List[DemandForecast]:
        """Прогноз спроса на направления по месяцам"""
        
        # Получаем исторические данные (минимум 12 месяцев для сезонности)
        requests = self.data_service.get_requests(365)  # 12 месяцев
        
        if requests.empty:
            return []
        
        # Группируем по направлению и месяцу
        requests['created_at'] = pd.to_datetime(requests['created_at'])
        requests['year_month'] = requests['created_at'].dt.to_period('M')
        
        forecasts = []
        destinations_to_analyze = [destination] if destination else \
            requests['destination_city'].unique()  # Анализируем все направления
        
        for dest in destinations_to_analyze:
            if pd.isna(dest):
                continue
                
            dest_requests = requests[requests['destination_city'] == dest]
            
            if len(dest_requests) < 4:  # Недостаточно данных
                continue
            
            monthly_counts = dest_requests.groupby('year_month').size().reset_index(name='count')
            monthly_counts['year_month_str'] = monthly_counts['year_month'].astype(str)
            
            if len(monthly_counts) < 3:
                continue
            
            # Подготовка данных для прогноза
            X = monthly_counts.index.values.reshape(-1, 1)
            y = monthly_counts['count'].values
            
            # Используем ансамбль моделей для более точного прогноза (если достаточно данных)
            # Для малых выборок используем простую линейную регрессию
            use_ensemble = len(monthly_counts) >= 6
            
            if use_ensemble:
                # Ансамбль моделей для лучшей точности
                ensemble_model, metrics = self._get_or_train_ensemble_model(dest, X, y)
                model = ensemble_model
                # Для определения тренда используем линейную регрессию отдельно
                # (ансамбль не имеет прямого коэффициента тренда)
                lr_for_trend = LinearRegression()
                lr_for_trend.fit(X, y)
                trend_coef = lr_for_trend.coef_[0]
                confidence = metrics.get('ensemble_r2', 0.5)
            else:
                # Простая линейная регрессия для малых выборок
                model = self._get_or_train_forecast_model(dest, X, y)
                trend_coef = model.coef_[0] if hasattr(model, 'coef_') else 0
                confidence = max(0.3, min(0.95, model.score(X, y)))
            
            # Прогноз на следующие месяцы
            next_period = np.array([[len(monthly_counts)]])
            predicted = max(0, int(model.predict(next_period)[0]))
            current = int(monthly_counts['count'].iloc[-1])
            
            # Рассчитываем выручку
            completed_requests = dest_requests[dest_requests['status'] == 'COMPLETED']
            current_revenue = completed_requests['price'].sum() if not completed_requests.empty else 0.0
            
            # Прогнозируем выручку на основе прогноза спроса и средней цены
            avg_price = dest_requests['price'].mean() if not dest_requests.empty else 0.0
            
            # Если нет завершенных заявок, используем среднюю цену туров в этом направлении
            if avg_price == 0 or pd.isna(avg_price):
                tours = self.data_service.get_tours()
                dest_tours = tours[tours['destination_city'] == dest] if not tours.empty else pd.DataFrame()
                if not dest_tours.empty:
                    avg_price = dest_tours['price'].mean()
                else:
                    avg_price = 50000.0  # Значение по умолчанию
            
            # Конверсия: если нет данных, используем среднюю по всем направлениям
            if len(dest_requests) > 0:
                conversion_rate = len(completed_requests) / len(dest_requests)
            else:
                # Используем среднюю конверсию по всем направлениям
                all_requests = self.data_service.get_requests(180)
                all_completed = len(all_requests[all_requests['status'] == 'COMPLETED'])
                conversion_rate = all_completed / len(all_requests) if len(all_requests) > 0 else 0.5
            
            # Прогнозируем выручку: прогноз спроса * конверсия * средняя цена
            predicted_revenue = predicted * conversion_rate * float(avg_price)
            
            # Определяем тренд на основе коэффициента или изменения значений
            if use_ensemble:
                # Для ансамбля используем вычисленный trend_coef
                coef = trend_coef
            else:
                # Для линейной регрессии используем коэффициент модели
                coef = model.coef_[0] if hasattr(model, 'coef_') else trend_coef
            
            if coef > 0.5:
                trend = "rising"
                recommendation = self._generate_recommendation(dest, "rising", predicted, predicted_revenue, avg_price)
            elif coef < -0.5:
                trend = "falling"
                recommendation = self._generate_recommendation(dest, "falling", predicted, predicted_revenue, avg_price)
            else:
                trend = "stable"
                recommendation = self._generate_recommendation(dest, "stable", predicted, predicted_revenue, avg_price)
            
            forecasts.append(
                DemandForecast(
                    destination=dest,
                    current_demand=current,
                    predicted_demand=predicted,
                    current_revenue=round(float(current_revenue), 2),
                    predicted_revenue=round(float(predicted_revenue), 2),
                    confidence=round(confidence, 2),
                    trend=trend,
                    recommendation=recommendation
                )
            )
        
        return sorted(forecasts, key=lambda x: x.predicted_demand, reverse=True)
    
    def get_demand_forecast_table(self) -> List[DemandForecastTableRow]:
        """Получить прогноз спроса в табличном формате (спрос по неделям)"""
        import logging
        logger = logging.getLogger(__name__)
        from datetime import datetime, timedelta
        
        try:
            # Получаем данные за последние 4 недели для расчета текущего недельного спроса
            requests = self.data_service.get_requests(30)  # За последний месяц
            logger.info(f"Got {len(requests)} requests for forecast table")
            
            if requests.empty:
                logger.warning("No requests found for forecast table")
                return []
            
            # Получаем прогнозы (они уже рассчитаны по месяцам)
            monthly_forecasts = self.forecast_demand()
            logger.info(f"Got {len(monthly_forecasts)} monthly forecasts")
            
            if not monthly_forecasts:
                logger.warning("No monthly forecasts found")
                return []
            
            # Группируем заявки по направлению и неделе
            requests['created_at'] = pd.to_datetime(requests['created_at'])
            requests['week'] = requests['created_at'].dt.to_period('W')
            
            table_rows = []
            
            for forecast in monthly_forecasts:
                dest = forecast.destination
                dest_requests = requests[requests['destination_city'] == dest]
                
                if dest_requests.empty:
                    continue
                
                # Рассчитываем средний недельный спрос за последние 4 недели
                weekly_counts = dest_requests.groupby('week').size()
                if len(weekly_counts) > 0:
                    current_demand_per_week = int(weekly_counts.mean())
                else:
                    current_demand_per_week = 0
                
                # Прогноз на неделю = прогноз на месяц / 4.3 (среднее количество недель в месяце)
                predicted_demand_per_week = max(0, int(forecast.predicted_demand / 4.3))
                
                # Рассчитываем изменение в процентах
                if current_demand_per_week > 0:
                    change_percent = ((predicted_demand_per_week - current_demand_per_week) / current_demand_per_week) * 100
                else:
                    change_percent = 100.0 if predicted_demand_per_week > 0 else 0.0
                
                # Округляем до 1 знака после запятой
                change_percent = round(change_percent, 1)
                
                # Переводим тренд на русский
                trend_ru = {
                    "rising": "Растущий",
                    "falling": "Падающий",
                    "stable": "Стабильный"
                }.get(forecast.trend, "Стабильный")
                
                table_rows.append(
                    DemandForecastTableRow(
                        destination=dest,
                        current_demand_per_week=current_demand_per_week,
                        predicted_demand_per_week=predicted_demand_per_week,
                        change_percent=round(change_percent, 1),
                        trend=trend_ru,
                        confidence=forecast.confidence,
                        recommendation=forecast.recommendation
                    )
                )
            
            logger.info(f"Generated {len(table_rows)} table rows")
            
            # Сортируем по прогнозируемому спросу (по убыванию)
            result = sorted(table_rows, key=lambda x: x.predicted_demand_per_week, reverse=True)
            logger.info(f"Returning {len(result)} sorted rows")
            return result
        except Exception as e:
            logger.error(f"Error in get_demand_forecast_table: {e}", exc_info=True)
            raise
    
    def _generate_recommendation(
        self, 
        destination: str, 
        trend: str, 
        predicted_demand: int,
        predicted_revenue: float,
        avg_price: float
    ) -> str:
        """Генерация ML-рекомендаций на основе анализа данных"""
        recommendations = []
        
        # Анализируем данные по турам в этом направлении
        tours = self.data_service.get_tours()
        dest_tours = tours[tours['destination_city'] == destination] if not tours.empty else pd.DataFrame()
        requests = self.data_service.get_requests(90)
        dest_requests = requests[requests['destination_city'] == destination] if not requests.empty else pd.DataFrame()
        
        if not dest_tours.empty and not dest_requests.empty:
            # Анализ загрузки туров
            for _, tour in dest_tours.iterrows():
                tour_id = tour['id']
                tour_name = tour['name']
                tour_requests = dest_requests[dest_requests['tour_id'] == tour_id]
                
                if len(tour_requests) >= 3:
                    completed = len(tour_requests[tour_requests['status'] == 'COMPLETED'])
                    cancelled = len(tour_requests[tour_requests['status'] == 'CANCELLED'])
                    conversion = completed / len(tour_requests) if len(tour_requests) > 0 else 0
                    
                    # Рекомендации на основе конверсии и спроса
                    if trend == "rising" and conversion > 0.6:
                        recommendations.append(
                            f"Тур '{tour_name}' показывает высокую конверсию ({conversion*100:.1f}%) "
                            f"и растущий спрос. Рекомендуется увеличить количество доступных мест."
                        )
                    elif trend == "rising" and conversion < 0.3:
                        recommendations.append(
                            f"Тур '{tour_name}' имеет низкую конверсию ({conversion*100:.1f}%), "
                            f"но спрос растет. Рассмотрите оптимизацию цены или улучшение предложения."
                        )
                    elif trend == "falling" and cancelled > completed:
                        recommendations.append(
                            f"Тур '{tour_name}' имеет высокий процент отмен ({cancelled}/{len(tour_requests)}). "
                            f"Рекомендуется пересмотреть условия или добавить гибкие опции бронирования."
                        )
        
        # Общие рекомендации по направлению
        if trend == "rising":
            if predicted_revenue > avg_price * predicted_demand * 0.8:
                recommendations.append(
                    f"Направление {destination} показывает стабильный рост. "
                    f"Прогнозируемая выручка на следующий месяц: {predicted_revenue:,.0f} ₽. "
                    f"Рекомендуется расширить предложение туров в этом направлении."
                )
            else:
                recommendations.append(
                    f"Спрос на {destination} растет, но средний чек ниже ожидаемого. "
                    f"Рассмотрите добавление премиум-туров или улучшение качества предложения."
                )
        elif trend == "falling":
            recommendations.append(
                f"Спрос на {destination} снижается. Прогнозируемая выручка: {predicted_revenue:,.0f} ₽. "
                f"Рекомендуется запустить маркетинговую кампанию или предложить специальные акции."
            )
        else:
            # Стабильный тренд - проверяем, что выручка не 0
            if predicted_revenue > 0:
                recommendations.append(
                    f"Направление {destination} показывает стабильный спрос. "
                    f"Прогнозируемая выручка: {predicted_revenue:,.0f} ₽. "
                    f"Поддерживайте текущий уровень предложения."
                )
            else:
                # Если выручка 0, значит нет завершенных заявок - нужно стимулировать спрос
                recommendations.append(
                    f"Направление {destination} показывает стабильный спрос, но нет завершенных заявок. "
                    f"Рекомендуется проанализировать причины низкой конверсии и улучшить предложение."
                )
        
        return " | ".join(recommendations[:3]) if recommendations else f"Поддерживайте текущий уровень предложения для {destination}"
    
    def _get_or_train_forecast_model(
        self, 
        destination: str, 
        X: np.ndarray, 
        y: np.ndarray
    ) -> LinearRegression:
        """Получить кэшированную модель или обучить новую"""
        model_path = os.path.join(
            self._forecast_cache_path, 
            f"forecast_{destination.replace(' ', '_')}.joblib"
        )
        
        # Пытаемся загрузить из кэша
        if destination in self._forecast_models_cache:
            return self._forecast_models_cache[destination]
        
        # Пытаемся загрузить из файла
        if os.path.exists(model_path):
            try:
                model = joblib.load(model_path)
                self._forecast_models_cache[destination] = model
                logger.debug(f"Loaded cached forecast model for {destination}")
                return model
            except Exception as e:
                logger.warning(f"Failed to load cached model for {destination}: {e}")
        
        # Обучаем новую модель
        logger.info(f"Training new forecast model for {destination}")
        model = LinearRegression()
        model.fit(X, y)
        
        # Сохраняем модель
        try:
            joblib.dump(model, model_path)
            self._forecast_models_cache[destination] = model
            logger.debug(f"Saved forecast model for {destination}")
        except Exception as e:
            logger.warning(f"Failed to save forecast model for {destination}: {e}")
        
        return model
    
    def _get_or_train_ensemble_model(
        self,
        destination: str,
        X: np.ndarray,
        y: np.ndarray
    ) -> tuple:
        """Обучить ансамбль моделей и вернуть метрики"""
        from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
        
        # Обучаем три модели
        lr = LinearRegression()
        rf = RandomForestRegressor(n_estimators=50, random_state=42, max_depth=5)
        gb = GradientBoostingRegressor(n_estimators=50, random_state=42, max_depth=3)
        
        lr.fit(X, y)
        rf.fit(X, y)
        gb.fit(X, y)
        
        # Предсказания
        y_pred_lr = lr.predict(X)
        y_pred_rf = rf.predict(X)
        y_pred_gb = gb.predict(X)
        
        # Метрики для каждой модели
        r2_lr = r2_score(y, y_pred_lr)
        r2_rf = r2_score(y, y_pred_rf)
        r2_gb = r2_score(y, y_pred_gb)
        
        mae_lr = mean_absolute_error(y, y_pred_lr)
        mae_rf = mean_absolute_error(y, y_pred_rf)
        mae_gb = mean_absolute_error(y, y_pred_gb)
        
        rmse_lr = np.sqrt(mean_squared_error(y, y_pred_lr))
        rmse_rf = np.sqrt(mean_squared_error(y, y_pred_rf))
        rmse_gb = np.sqrt(mean_squared_error(y, y_pred_gb))
        
        # Взвешенное среднее предсказаний (веса по R²)
        total_r2 = r2_lr + r2_rf + r2_gb
        if total_r2 > 0:
            w_lr = r2_lr / total_r2
            w_rf = r2_rf / total_r2
            w_gb = r2_gb / total_r2
        else:
            w_lr = w_rf = w_gb = 1.0 / 3.0
        
        y_pred_ensemble = w_lr * y_pred_lr + w_rf * y_pred_rf + w_gb * y_pred_gb
        
        # Метрики ансамбля
        r2_ensemble = r2_score(y, y_pred_ensemble)
        mae_ensemble = mean_absolute_error(y, y_pred_ensemble)
        rmse_ensemble = np.sqrt(mean_squared_error(y, y_pred_ensemble))
        
        # Вычисляем средний R²
        avg_r2 = (r2_lr + r2_rf + r2_gb) / 3.0
        
        metrics = {
            "linear_r2": round(float(r2_lr), 3),
            "random_forest_r2": round(float(r2_rf), 3),
            "gradient_boosting_r2": round(float(r2_gb), 3),
            "ensemble_r2": round(float(r2_ensemble), 3),
            "ensemble_mae": round(float(mae_ensemble), 2),
            "ensemble_rmse": round(float(rmse_ensemble), 2),
            "avg_r2": round(float(avg_r2), 3),
            "weights": {
                "linear": round(float(w_lr), 3),
                "random_forest": round(float(w_rf), 3),
                "gradient_boosting": round(float(w_gb), 3)
            }
        }
        
        # Создаем простую модель-обертку для совместимости
        class EnsembleModel:
            def predict(self, X):
                return w_lr * lr.predict(X) + w_rf * rf.predict(X) + w_gb * gb.predict(X)
        
        return EnsembleModel(), metrics
    
    def _load_cached_forecast_models(self):
        """Загрузить все кэшированные модели прогноза при старте"""
        try:
            if not os.path.exists(self._forecast_cache_path):
                return
            
            for filename in os.listdir(self._forecast_cache_path):
                if filename.endswith('.joblib'):
                    try:
                        destination = filename.replace('forecast_', '').replace('.joblib', '').replace('_', ' ')
                        model_path = os.path.join(self._forecast_cache_path, filename)
                        model = joblib.load(model_path)
                        self._forecast_models_cache[destination] = model
                        logger.info(f"Loaded cached forecast model: {destination}")
                    except Exception as e:
                        logger.warning(f"Failed to load forecast model {filename}: {e}")
        except Exception as e:
            logger.warning(f"Failed to load cached forecast models: {e}")
    
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
    
    def get_tour_clusters(self, n_clusters: int = 3) -> List:
        """
        Кластеризация туров по характеристикам с гарантированным разделением на категории
        
        Вместо KMeans используется детерминированная кластеризация по ценовым категориям:
        - БЮДЖЕТ: цена < 60,000₽
        - СРЕДНИЙ: 60,000₽ <= цена <= 100,000₽
        - ПРЕМИУМ: цена > 100,000₽
        """
        from app.schemas.analytics import TourCluster
        
        tours = self.data_service.get_tours()
        requests = self.data_service.get_requests(180)
        
        if tours.empty:
            return []
        
        # Определяем ценовые пороги (актуальные для 2026 года)
        BUDGET_MAX = 60000  # Максимальная цена бюджетных туров
        PREMIUM_MIN = 100000  # Минимальная цена премиум туров
        
        # Разделяем туры на категории по цене
        budget_tours = tours[tours['price'] < BUDGET_MAX]
        middle_tours = tours[(tours['price'] >= BUDGET_MAX) & (tours['price'] <= PREMIUM_MIN)]
        premium_tours = tours[tours['price'] > PREMIUM_MIN]
        
        logger.info(f"Tour distribution: Budget={len(budget_tours)}, Middle={len(middle_tours)}, Premium={len(premium_tours)}")
        
        # Формирование результата
        result = []
        
        # Функция для создания кластера
        def create_cluster(cluster_id: int, cluster_tours: pd.DataFrame, category_name: str, price_range: str) -> dict:
            if cluster_tours.empty:
                return None
                
            cluster_tour_ids = cluster_tours['id'].tolist()
            cluster_requests = requests[requests['tour_id'].isin(cluster_tour_ids)]
            
            avg_price = cluster_tours['price'].mean()
            avg_duration = cluster_tours['duration_days'].mean()
            total_popularity = len(cluster_requests)
            completed = len(cluster_requests[cluster_requests['status'] == 'COMPLETED'])
            avg_conversion = completed / total_popularity if total_popularity > 0 else 0.0
            
            # Определение типа по длительности
            if avg_duration < 7:
                duration_category = "короткие"
            elif avg_duration > 10:
                duration_category = "длительные"
            else:
                duration_category = "средней длительности"
            
            # Определение типа по популярности
            if avg_conversion > 0.6:
                popularity_category = "популярные"
            elif avg_conversion < 0.3:
                popularity_category = "низкой популярности"
            else:
                popularity_category = "средней популярности"
            
            # Формируем описание
            if category_name == "бюджетные":
                if duration_category == "короткие":
                    description = f"Бюджетные короткие туры ({price_range})"
                elif duration_category == "длительные":
                    description = f"Бюджетные туры длительного отдыха ({price_range})"
                else:
                    description = f"Бюджетные туры ({price_range})"
            elif category_name == "среднего класса":
                if duration_category == "длительные":
                    description = f"Туры среднего класса длительного отдыха ({price_range})"
                elif duration_category == "короткие":
                    description = f"Короткие туры среднего класса ({price_range})"
                else:
                    description = f"Туры среднего класса ({price_range})"
            else:  # премиум
                if duration_category == "длительные":
                    description = f"Премиум-туры длительного отдыха ({price_range})"
                else:
                    description = f"Премиум-туры ({price_range})"
            
            # Список туров в кластере
            tours_list = []
            for _, tour in cluster_tours.iterrows():
                tour_requests_count = len(requests[requests['tour_id'] == tour['id']])
                tours_list.append({
                    'tour_id': int(tour['id']),
                    'tour_name': tour['name'],
                    'destination': tour['destination_city'],
                    'price': float(tour['price']),
                    'duration': int(tour['duration_days']),
                    'popularity': tour_requests_count
                })
            
            # Сортируем туры по популярности
            tours_list.sort(key=lambda x: x['popularity'], reverse=True)
            
            return TourCluster(
                cluster_id=cluster_id,
                cluster_type=category_name.replace(" класса", ""),
                description=description,
                tours=tours_list,
                avg_price=round(float(avg_price), 2),
                avg_duration=round(float(avg_duration), 1),
                total_popularity=total_popularity,
                avg_conversion=round(avg_conversion, 2)
            )
        
        # Создаем кластеры для каждой категории
        cluster_id = 0
        
        # Бюджетные туры (cluster_id=0)
        if not budget_tours.empty:
            cluster = create_cluster(
                cluster_id, 
                budget_tours, 
                "бюджетные",
                f"до {BUDGET_MAX:,}₽".replace(",", " ")
            )
            if cluster:
                result.append(cluster)
                cluster_id += 1
        
        # Туры среднего класса (cluster_id=1)
        if not middle_tours.empty:
            cluster = create_cluster(
                cluster_id, 
                middle_tours, 
                "среднего класса",
                f"{BUDGET_MAX:,}-{PREMIUM_MIN:,}₽".replace(",", " ")
            )
            if cluster:
                result.append(cluster)
                cluster_id += 1
        
        # Премиум туры (cluster_id=2)
        if not premium_tours.empty:
            cluster = create_cluster(
                cluster_id, 
                premium_tours, 
                "премиум",
                f"от {PREMIUM_MIN:,}₽".replace(",", " ")
            )
            if cluster:
                result.append(cluster)
        
        logger.info(f"Created {len(result)} clusters")
        return result
    
    def get_anomalous_tours(self) -> List:
        """Детекция аномальных туров"""
        from app.schemas.analytics import AnomalousTour
        
        tours = self.data_service.get_tours()
        requests = self.data_service.get_requests(180)
        
        if tours.empty:
            return []
        
        anomalies = []
        
        for _, tour in tours.iterrows():
            tour_requests = requests[requests['tour_id'] == tour['id']]
            
            if len(tour_requests) < 3:
                continue
            
            popularity = len(tour_requests)
            completed = len(tour_requests[tour_requests['status'] == 'COMPLETED'])
            conversion = completed / popularity if popularity > 0 else 0.0
            current_price = float(tour['price'])
            
            # Нормализация для детекции аномалий
            all_tours_avg_price = tours['price'].mean()
            all_tours_avg_popularity = len(requests) / len(tours) if len(tours) > 0 else 0
            
            # Оценка спроса (популярность относительно среднего)
            demand_score = popularity / all_tours_avg_popularity if all_tours_avg_popularity > 0 else 0.0
            demand_score = min(demand_score, 2.0) / 2.0  # Нормализация к 0-1
            
            # Оценка цены (относительно среднего)
            price_score = current_price / all_tours_avg_price if all_tours_avg_price > 0 else 1.0
            price_score = min(price_score, 2.0) / 2.0  # Нормализация к 0-1
            
            # Детекция аномалий
            anomaly_type = None
            recommendation = ""
            
            # Высокий спрос, низкая цена
            if demand_score > 0.7 and price_score < 0.6:
                anomaly_type = "high_demand_low_price"
                expected_revenue_impact = current_price * popularity * 0.15  # +15% при повышении цены
                recommendation = f"Тур '{tour['name']}' имеет высокий спрос ({popularity} заявок), но низкую цену ({current_price:,.0f} ₽). Рекомендуется повысить цену на 10-15% для максимизации выручки."
            
            # Низкий спрос, высокая цена
            elif demand_score < 0.4 and price_score > 0.8:
                anomaly_type = "low_demand_high_price"
                expected_revenue_impact = -current_price * popularity * 0.1  # -10% при снижении цены, но больше заявок
                recommendation = f"Тур '{tour['name']}' имеет низкий спрос ({popularity} заявок) при высокой цене ({current_price:,.0f} ₽). Рекомендуется снизить цену на 10-15% для увеличения спроса."
            
            if anomaly_type:
                anomalies.append(AnomalousTour(
                    tour_id=int(tour['id']),
                    tour_name=tour['name'],
                    destination=tour['destination_city'],
                    current_price=current_price,
                    demand_score=round(demand_score, 3),
                    price_score=round(price_score, 3),
                    anomaly_type=anomaly_type,
                    expected_revenue_impact=round(expected_revenue_impact, 2),
                    recommendation=recommendation
                ))
        
        return anomalies
    
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
