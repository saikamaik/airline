import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Alert,
} from '@mui/material';
import { authApi } from '../api/auth';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      console.log('=== LoginPage: Попытка входа для', username);
      const response = await authApi.login({ username, password });
      console.log('=== LoginPage: Получен ответ:', response);
      
      localStorage.setItem('jwt_token', response.token);
      localStorage.setItem('username', response.username);
      localStorage.setItem('roles', JSON.stringify(response.roles));
      
      console.log('=== LoginPage: Токен сохранён, роли:', response.roles);
      // Перенаправляем сотрудников на их панель, админов на dashboard
      const isEmployee = response.roles?.includes('ROLE_EMPLOYEE') && !response.roles?.includes('ROLE_ADMIN');
      navigate(isEmployee ? '/employee-dashboard' : '/dashboard');
    } catch (err: any) {
      console.error('=== LoginPage: Ошибка входа:', err);
      const errorMessage = err.response?.data?.error || err.response?.data || err.message || 'Ошибка входа. Проверьте логин и пароль.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" component="h1" gutterBottom align="center">
            Админ-панель турагентства
          </Typography>
          <Typography variant="body2" gutterBottom align="center" color="text.secondary" sx={{ mb: 3 }}>
            Войдите для управления системой
          </Typography>

          <form onSubmit={handleLogin}>
            <TextField
              fullWidth
              label="Логин"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              required
              autoFocus
            />
            <TextField
              fullWidth
              label="Пароль"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              required
            />

            {error && (
              <Alert severity="error" sx={{ mt: 2 }}>
                {error}
              </Alert>
            )}

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{ mt: 3 }}
            >
              {loading ? 'Вход...' : 'Войти'}
            </Button>
          </form>

          <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
            <Typography variant="caption" display="block">
              <strong>Демо-доступы:</strong>
            </Typography>
            <Typography variant="caption" display="block">
              Админ: admin / password123
            </Typography>
            <Typography variant="caption" display="block">
              Пользователь: user / password123
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}

