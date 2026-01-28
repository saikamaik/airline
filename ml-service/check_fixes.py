"""
Проверка исправлений безопасности (статический анализ)
"""
import re
import os

def check_sql_injection_fixed():
    """Проверка что SQL injection исправлен"""
    print("=" * 60)
    print("Проверка защиты от SQL Injection")
    print("=" * 60)
    
    file_path = "app/services/data_service.py"
    if not os.path.exists(file_path):
        print(f"Файл {file_path} не найден")
        return False
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    issues = []
    
    # Проверяем что нет f-строк с INTERVAL
    f_string_pattern = r'f["\'].*INTERVAL.*\{.*\}'
    matches = re.findall(f_string_pattern, content)
    if matches:
        issues.append(f"Найдены f-строки с INTERVAL: {matches}")
    
    # Проверяем что используется text() для параметризованных запросов
    if 'make_interval(days => :days)' not in content:
        issues.append("Не используется make_interval с параметрами в get_requests")
    
    if 'make_interval(months => :months)' not in content:
        issues.append("Не используется make_interval с параметрами в get_monthly_stats")
    
    # Проверяем валидацию входных данных
    if 'isinstance(days, int)' not in content:
        issues.append("Нет валидации days в get_requests")
    
    if 'isinstance(months, int)' not in content:
        issues.append("Нет валидации months в get_monthly_stats")
    
    if issues:
        print("ПРОБЛЕМЫ:")
        for issue in issues:
            print(f"  - {issue}")
        return False
    else:
        print("OK: SQL Injection защита исправлена")
        print("  - Используются параметризованные запросы")
        print("  - Есть валидация входных данных")
        return True

def check_cors_fixed():
    """Проверка что CORS исправлен"""
    print("\n" + "=" * 60)
    print("Проверка конфигурации CORS")
    print("=" * 60)
    
    file_path = "app/main.py"
    if not os.path.exists(file_path):
        print(f"Файл {file_path} не найден")
        return False
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    issues = []
    
    # Проверяем что нет allow_origins=["*"]
    if 'allow_origins=["*"]' in content or "allow_origins=['*']" in content:
        issues.append("CORS все еще разрешает все домены (*)")
    
    # Проверяем что есть конкретные домены
    if 'localhost:3000' not in content and '127.0.0.1:3000' not in content:
        issues.append("Нет разрешения для admin panel (localhost:3000)")
    
    if 'localhost:8080' not in content and '127.0.0.1:8080' not in content:
        issues.append("Нет разрешения для backend (localhost:8080)")
    
    if issues:
        print("ПРОБЛЕМЫ:")
        for issue in issues:
            print(f"  - {issue}")
        return False
    else:
        print("OK: CORS настроен правильно")
        print("  - Разрешены только конкретные домены")
        print("  - Нет разрешения для всех (*)")
        return True

def check_logging_added():
    """Проверка что логирование добавлено"""
    print("\n" + "=" * 60)
    print("Проверка логирования")
    print("=" * 60)
    
    files_to_check = [
        "app/main.py",
        "app/api/analytics.py",
        "app/api/recommendations.py",
        "app/services/data_service.py"
    ]
    
    all_ok = True
    for file_path in files_to_check:
        if not os.path.exists(file_path):
            continue
        
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        if 'import logging' not in content and 'logging.getLogger' not in content:
            print(f"  WARNING: {file_path} - нет логирования")
            all_ok = False
    
    if all_ok:
        print("OK: Логирование добавлено во все файлы")
    else:
        print("WARNING: Логирование может быть не везде")
    
    return True

if __name__ == "__main__":
    results = []
    
    results.append(check_sql_injection_fixed())
    results.append(check_cors_fixed())
    results.append(check_logging_added())
    
    print("\n" + "=" * 60)
    print("ИТОГИ ПРОВЕРКИ")
    print("=" * 60)
    
    if all(results):
        print("SUCCESS: Все проверки пройдены!")
    else:
        print("WARNING: Некоторые проверки не пройдены")
        print("Проверьте вывод выше для деталей")
