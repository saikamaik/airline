from pydantic_settings import BaseSettings
from pydantic import ConfigDict
from functools import lru_cache
import os


class Settings(BaseSettings):
    """Настройки приложения"""
    
    # Настройка Pydantic для разрешения использования model_ префикса
    model_config = ConfigDict(
        protected_namespaces=('settings_',),  # Разрешаем model_ префикс
        env_file=".env"
    )
    
    # Основные настройки
    app_name: str = "TravelAgency ML Service"
    debug: bool = os.getenv("DEBUG", "false").lower() == "true"
    
    # База данных (основной backend)
    # Pydantic автоматически читает DATABASE_URL из переменных окружения
    database_url: str = os.getenv(
        "DATABASE_URL", 
        "postgresql://postgres:postgres@localhost:5432/jcourse"
    )
    
    # Backend API (не используется напрямую, но может быть полезен)
    backend_url: str = os.getenv("BACKEND_URL", "http://localhost:8080")
    
    # ML модели
    model_path: str = os.getenv("MODEL_PATH", "./models")


@lru_cache()
def get_settings() -> Settings:
    return Settings()
