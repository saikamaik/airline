import axios from 'axios';

// Используем переменную окружения для продакшна, или относительный путь для разработки
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Добавляем JWT токен к каждому запросу
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Обработка ошибок авторизации
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
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

