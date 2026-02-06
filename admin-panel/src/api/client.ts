import axios from 'axios';

// Используем переменную окружения для продакшна, или относительный путь для разработки
// Убираем trailing slash и /api если они есть, так как пути уже содержат нужные префиксы
const rawUrl = import.meta.env.VITE_API_URL || '/api';
const API_BASE_URL = rawUrl.endsWith('/api') 
  ? rawUrl.replace(/\/api$/, '') 
  : rawUrl.endsWith('/') 
    ? rawUrl.slice(0, -1) 
    : rawUrl;

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Логирование для отладки (только в dev режиме)
if (import.meta.env.DEV) {
  console.log('API Client initialized with baseURL:', API_BASE_URL);
}

// Добавляем JWT токен к каждому запросу
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Логирование для отладки
    if (import.meta.env.DEV) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, config.data || '');
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Обработка ошибок авторизации
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Логирование для отладки
    if (import.meta.env.DEV) {
      console.error('[API Error]', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        url: error.config?.url,
        baseURL: error.config?.baseURL,
        method: error.config?.method,
        data: error.response?.data
      });
    }
    
    if (error.response?.status === 401) {
      // Не делаем редирект, если мы уже на странице логина
      // (проверяем по URL или по наличию токена)
      const currentPath = window.location.pathname;
      if (currentPath !== '/login' && currentPath !== '/') {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('username');
        localStorage.removeItem('roles');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;

