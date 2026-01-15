"""Сервис для работы с данными из основной БД"""

from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta
import pandas as pd
from sqlalchemy import create_engine, text
from app.config import get_settings


class DataService:
    """Сервис для получения данных из PostgreSQL"""
    
    def __init__(self):
        settings = get_settings()
        self.engine = create_engine(settings.database_url)
    
    def get_tours(self, active_only: bool = True) -> pd.DataFrame:
        """Получить список туров"""
        query = """
            SELECT id, name, description, price, duration_days, 
                   destination_city, active, created_at
            FROM bookings.tours
        """
        if active_only:
            query += " WHERE active = true"
        
        return pd.read_sql(query, self.engine)
    
    def get_requests(self, days: int = 90) -> pd.DataFrame:
        """Получить заявки за последние N дней"""
        query = f"""
            SELECT cr.id, cr.tour_id, cr.status, cr.priority, 
                   cr.created_at, cr.updated_at,
                   t.name as tour_name, t.destination_city, t.price
            FROM bookings.client_requests cr
            LEFT JOIN bookings.tours t ON cr.tour_id = t.id
            WHERE cr.created_at >= NOW() - INTERVAL '{days} days'
            ORDER BY cr.created_at DESC
        """
        return pd.read_sql(query, self.engine)
    
    def get_request_history(self, request_id: int) -> pd.DataFrame:
        """Получить историю изменений заявки"""
        query = """
            SELECT * FROM bookings.request_history
            WHERE request_id = :request_id
            ORDER BY changed_at
        """
        return pd.read_sql(query, self.engine, params={"request_id": request_id})
    
    def get_user_requests(self, user_id: int) -> pd.DataFrame:
        """Получить заявки конкретного пользователя"""
        query = """
            SELECT cr.*, t.destination_city, t.price, t.duration_days
            FROM bookings.client_requests cr
            LEFT JOIN bookings.tours t ON cr.tour_id = t.id
            LEFT JOIN bookings.clients c ON cr.client_id = c.id
            WHERE c.user_id = :user_id
            ORDER BY cr.created_at DESC
        """
        return pd.read_sql(query, self.engine, params={"user_id": user_id})
    
    def get_destination_stats(self, days: int = 365) -> pd.DataFrame:
        """Статистика по направлениям"""
        query = f"""
            SELECT 
                t.destination_city,
                COUNT(cr.id) as request_count,
                SUM(t.price) as total_revenue,
                AVG(t.price) as avg_price,
                COUNT(CASE WHEN cr.status = 'COMPLETED' THEN 1 END) as completed_count
            FROM bookings.client_requests cr
            JOIN bookings.tours t ON cr.tour_id = t.id
            WHERE cr.created_at >= NOW() - INTERVAL '{days} days'
            GROUP BY t.destination_city
            ORDER BY request_count DESC
        """
        return pd.read_sql(query, self.engine)
    
    def get_monthly_stats(self, months: int = 12) -> pd.DataFrame:
        """Помесячная статистика"""
        query = f"""
            SELECT 
                DATE_TRUNC('month', cr.created_at) as month,
                COUNT(cr.id) as request_count,
                AVG(t.price) as avg_price,
                array_agg(DISTINCT t.destination_city) as destinations
            FROM bookings.client_requests cr
            JOIN bookings.tours t ON cr.tour_id = t.id
            WHERE cr.created_at >= NOW() - INTERVAL '{months} months'
            GROUP BY DATE_TRUNC('month', cr.created_at)
            ORDER BY month
        """
        return pd.read_sql(query, self.engine)
