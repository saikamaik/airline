"""Кастомные исключения для ML-сервиса"""


class DatabaseError(Exception):
    """Ошибка подключения к базе данных"""
    pass


class DataValidationError(Exception):
    """Ошибка валидации данных"""
    pass


class ServiceUnavailableError(Exception):
    """Сервис временно недоступен"""
    pass
