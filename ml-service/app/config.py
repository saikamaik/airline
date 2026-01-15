from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    """Настройки приложения"""
    
    # Основные настройки
    app_name: str = "TravelAgency ML Service"
    debug: bool = True
    
    # База данных (основной backend)
    database_url: str = "postgresql://postgres:postgres@localhost:5432/jcourse"
    
    # Backend API
    backend_url: str = "http://localhost:8080"
    
    # ML модели
    model_path: str = "./models"
    
    class Config:
        env_file = ".env"


@lru_cache()
def get_settings() -> Settings:
    return Settings()
