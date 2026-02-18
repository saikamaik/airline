"""Сервис для работы с данными из основной БД"""

import logging
from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta
import pandas as pd
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError, OperationalError, DatabaseError as SQLAlchemyDatabaseError
from app.config import get_settings
from app.exceptions import DatabaseError, ServiceUnavailableError

logger = logging.getLogger(__name__)


class DataService:
    """Сервис для получения данных из PostgreSQL"""
    
    def __init__(self):
        settings = get_settings()
        self.engine = create_engine(settings.database_url)
        logger.info("DataService initialized with database connection")
    
    def __del__(self):
        """Закрытие соединения с БД при удалении объекта"""
        if hasattr(self, 'engine'):
            try:
                self.engine.dispose()
                logger.debug("Database engine disposed")
            except Exception as e:
                logger.warning(f"Error disposing database engine: {e}")
    
    def close(self):
        """Явное закрытие соединения с БД"""
        if hasattr(self, 'engine'):
            try:
                self.engine.dispose()
                logger.info("Database engine closed explicitly")
            except Exception as e:
                logger.error(f"Error closing database engine: {e}")
    
    def get_tours(self, active_only: bool = True) -> pd.DataFrame:
        """Получить список туров"""
        try:
            query = """
                SELECT id, name, description, price, duration_days, 
                       destination_city, active, created_at
                FROM bookings.tours
            """
            if active_only:
                query += " WHERE active = true"
            
            return pd.read_sql(query, self.engine)
        except OperationalError as e:
            logger.error(f"Database connection error in get_tours: {e}")
            raise DatabaseError("Unable to connect to database") from e
        except SQLAlchemyError as e:
            logger.error(f"Database error in get_tours: {e}")
            raise DatabaseError("Database operation failed") from e
        except Exception as e:
            logger.error(f"Unexpected error in get_tours: {e}", exc_info=True)
            raise ServiceUnavailableError("Service temporarily unavailable") from e
    
    def get_requests(self, days: int = 90) -> pd.DataFrame:
        """Получить заявки за последние N дней"""
        # Валидация входных данных для защиты от SQL injection
        if not isinstance(days, int) or days < 1 or days > 3650:
            raise ValueError(f"days must be an integer between 1 and 3650, got {days}")
        
        # Используем параметризованный запрос через make_interval для безопасности
        try:
            query = text("""
                SELECT cr.id, cr.tour_id, cr.status, cr.priority, 
                       cr.created_at, cr.created_at as updated_at,
                       t.name as tour_name, t.destination_city, t.price
                FROM bookings.client_requests cr
                LEFT JOIN bookings.tours t ON cr.tour_id = t.id
                WHERE cr.created_at >= NOW() - make_interval(days => :days)
                ORDER BY cr.created_at DESC
            """)
            return pd.read_sql(query, self.engine, params={"days": days})
        except OperationalError as e:
            logger.error(f"Database connection error in get_requests: {e}")
            raise DatabaseError("Unable to connect to database") from e
        except SQLAlchemyError as e:
            logger.error(f"Database error in get_requests: {e}")
            raise DatabaseError("Database operation failed") from e
        except Exception as e:
            logger.error(f"Unexpected error in get_requests: {e}", exc_info=True)
            raise ServiceUnavailableError("Service temporarily unavailable") from e
    
    def get_request_history(self, request_id: int) -> pd.DataFrame:
        """Получить историю изменений заявки"""
        if not isinstance(request_id, int) or request_id < 1:
            raise ValueError(f"request_id must be a positive integer, got {request_id}")
        
        try:
            query = text("""
                SELECT * FROM bookings.request_history
                WHERE request_id = :request_id
                ORDER BY changed_at
            """)
            return pd.read_sql(query, self.engine, params={"request_id": request_id})
        except OperationalError as e:
            logger.error(f"Database connection error in get_request_history: {e}")
            raise DatabaseError("Unable to connect to database") from e
        except SQLAlchemyError as e:
            logger.error(f"Database error in get_request_history: {e}")
            raise DatabaseError("Database operation failed") from e
        except Exception as e:
            logger.error(f"Unexpected error in get_request_history: {e}", exc_info=True)
            raise ServiceUnavailableError("Service temporarily unavailable") from e
    
    def get_user_requests(self, user_id: int) -> pd.DataFrame:
        """Получить заявки конкретного пользователя"""
        if not isinstance(user_id, int) or user_id < 1:
            raise ValueError(f"user_id must be a positive integer, got {user_id}")
        
        try:
            query = text("""
                SELECT cr.*, t.destination_city, t.price, t.duration_days
                FROM bookings.client_requests cr
                LEFT JOIN bookings.tours t ON cr.tour_id = t.id
                LEFT JOIN bookings.clients c ON cr.client_id = c.id
                WHERE c.user_id = :user_id
                ORDER BY cr.created_at DESC
            """)
            return pd.read_sql(query, self.engine, params={"user_id": user_id})
        except OperationalError as e:
            logger.error(f"Database connection error in get_user_requests: {e}")
            raise DatabaseError("Unable to connect to database") from e
        except SQLAlchemyError as e:
            logger.error(f"Database error in get_user_requests: {e}")
            raise DatabaseError("Database operation failed") from e
        except Exception as e:
            logger.error(f"Unexpected error in get_user_requests: {e}", exc_info=True)
            raise ServiceUnavailableError("Service temporarily unavailable") from e
    
    def get_destination_stats(self, days: int = 365) -> pd.DataFrame:
        """Статистика по направлениям"""
        # Валидация входных данных для защиты от SQL injection
        if not isinstance(days, int) or days < 1 or days > 3650:
            raise ValueError(f"days must be an integer between 1 and 3650, got {days}")
        
        # Используем параметризованный запрос через make_interval для безопасности
        try:
            query = text("""
                SELECT 
                    t.destination_city,
                    COUNT(cr.id) as request_count,
                    SUM(CASE WHEN cr.status = 'COMPLETED' THEN t.price ELSE 0 END) as total_revenue,
                    AVG(CASE WHEN cr.status = 'COMPLETED' THEN t.price ELSE NULL END) as avg_price,
                    COUNT(CASE WHEN cr.status = 'COMPLETED' THEN 1 END) as completed_count
                FROM bookings.client_requests cr
                JOIN bookings.tours t ON cr.tour_id = t.id
                WHERE cr.created_at >= NOW() - make_interval(days => :days)
                GROUP BY t.destination_city
                ORDER BY request_count DESC
            """)
            return pd.read_sql(query, self.engine, params={"days": days})
        except OperationalError as e:
            logger.error(f"Database connection error in get_destination_stats: {e}")
            raise DatabaseError("Unable to connect to database") from e
        except SQLAlchemyError as e:
            logger.error(f"Database error in get_destination_stats: {e}")
            raise DatabaseError("Database operation failed") from e
        except Exception as e:
            logger.error(f"Unexpected error in get_destination_stats: {e}", exc_info=True)
            raise ServiceUnavailableError("Service temporarily unavailable") from e
    
    def get_monthly_stats(self, months: int = 12) -> pd.DataFrame:
        """Помесячная статистика"""
        # Валидация входных данных для защиты от SQL injection
        if not isinstance(months, int) or months < 1 or months > 120:
            raise ValueError(f"months must be an integer between 1 and 120, got {months}")
        
        # Используем параметризованный запрос через make_interval для безопасности
        try:
            query = text("""
                SELECT 
                    DATE_TRUNC('month', cr.created_at) as month,
                    COUNT(cr.id) as request_count,
                    AVG(t.price) as avg_price,
                    COALESCE(
                        string_agg(DISTINCT t.destination_city, ','),
                        ''
                    ) as destinations
                FROM bookings.client_requests cr
                JOIN bookings.tours t ON cr.tour_id = t.id
                WHERE cr.created_at >= NOW() - make_interval(months => :months)
                    AND t.destination_city IS NOT NULL
                GROUP BY DATE_TRUNC('month', cr.created_at)
                ORDER BY month
            """)
            df = pd.read_sql(query, self.engine, params={"months": months})
            
            # Преобразуем строку с направлениями в список
            if not df.empty and 'destinations' in df.columns:
                df['destinations'] = df['destinations'].apply(
                    lambda x: [d.strip() for d in str(x).split(',') if d.strip()] if x else []
                )
            
            return df
        except OperationalError as e:
            logger.error(f"Database connection error in get_monthly_stats: {e}")
            raise DatabaseError("Unable to connect to database") from e
        except SQLAlchemyError as e:
            logger.error(f"Database error in get_monthly_stats: {e}")
            raise DatabaseError("Database operation failed") from e
        except Exception as e:
            logger.error(f"Unexpected error in get_monthly_stats: {e}", exc_info=True)
            raise ServiceUnavailableError("Service temporarily unavailable") from e
    
    def check_connection(self) -> bool:
        """Проверить подключение к базе данных"""
        try:
            with self.engine.connect() as conn:
                conn.execute(text("SELECT 1"))
            return True
        except Exception as e:
            logger.error(f"Database connection check failed: {e}")
            return False
