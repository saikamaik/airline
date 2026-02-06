import axios from 'axios';

// Используем переменную окружения для продакшна, или относительный путь для разработки
// Убираем trailing slash и /api если они есть, так как пути уже содержат нужные префиксы
const rawUrl = import.meta.env.VITE_API_URL || '/api';

// Обработка URL: если это не относительный путь (/api), то должен быть абсолютный URL
let processedUrl = rawUrl;
if (processedUrl !== '/api') {
  // Если URL не начинается с http:// или https://, добавляем https://
  if (!processedUrl.startsWith('http://') && !processedUrl.startsWith('https://')) {
    processedUrl = `https://${processedUrl}`;
  }
  // Убираем /api в конце если есть
  if (processedUrl.endsWith('/api')) {
    processedUrl = processedUrl.replace(/\/api$/, '');
  }
  // Убираем trailing slash
  if (processedUrl.endsWith('/')) {
    processedUrl = processedUrl.slice(0, -1);
  }
} else {
  // Для относительного пути /api оставляем как есть
  processedUrl = '/api';
}

const API_BASE_URL = processedUrl;

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Логирование для отладки (включаем и в продакшене для диагностики)
console.log('[API Client] Initialized with baseURL:', API_BASE_URL);
console.log('[API Client] VITE_API_URL from env:', import.meta.env.VITE_API_URL || 'not set');

// Добавляем JWT токен к каждому запросу
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Логирование для отладки (включаем и в продакшене)
    const fullUrl = config.baseURL && !config.baseURL.startsWith('http') 
      ? `${window.location.origin}${config.baseURL}${config.url}`
      : `${config.baseURL}${config.url}`;
    console.log(`[API Request] ${config.method?.toUpperCase()} ${fullUrl}`, config.data || '');
    return config;
  },
  (error) => Promise.reject(error)
);

// Обработка ошибок авторизации
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Логирование для отладки (включаем и в продакшене)
    const fullUrl = error.config?.baseURL && !error.config.baseURL.startsWith('http')
      ? `${window.location.origin}${error.config.baseURL}${error.config.url}`
      : `${error.config?.baseURL || ''}${error.config?.url || ''}`;
    console.error('[API Error]', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      baseURL: error.config?.baseURL,
      fullUrl: fullUrl,
      method: error.config?.method,
      data: error.response?.data,
      message: error.message
    });
    
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

