import { useEffect, useState } from 'react';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  Button,
  TextField,
  Stack,
} from '@mui/material';
import {
  FlightTakeoff,
  Tour,
  Mail,
  TrendingUp,
  Download,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ru } from 'date-fns/locale';
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { statisticsApi, Statistics } from '../api/statistics';
import { authApi } from '../api/auth';

export default function Dashboard() {
  const navigate = useNavigate();
  
  useEffect(() => {
    // Перенаправляем сотрудников на их панель
    const isEmployee = authApi.hasRole('ROLE_EMPLOYEE') && !authApi.hasRole('ROLE_ADMIN');
    if (isEmployee) {
      navigate('/employee-dashboard');
    }
  }, [navigate]);
  const [stats, setStats] = useState<Statistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      setLoading(true);
      const start = startDate ? startDate.toISOString().split('T')[0] : undefined;
      const end = endDate ? endDate.toISOString().split('T')[0] : undefined;
      const data = await statisticsApi.getStatistics(start, end);
      setStats(data);
    } catch (error) {
      console.error('Error loading stats:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      const start = startDate ? startDate.toISOString().split('T')[0] : undefined;
      const end = endDate ? endDate.toISOString().split('T')[0] : undefined;
      const blob = await statisticsApi.exportToCsv(start, end);
      
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `statistics_${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error exporting statistics:', error);
    }
  };

  const StatCard = ({ title, value, icon, color }: any) => (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography color="textSecondary" variant="caption" gutterBottom>
              {title}
            </Typography>
            <Typography variant="h4">{loading ? '...' : value}</Typography>
          </Box>
          <Box
            sx={{
              bgcolor: color + '.lighter',
              color: color + '.main',
              p: 2,
              borderRadius: 2,
            }}
          >
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  // Цвета для графиков
  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];
  
  // Подготовка данных для круговой диаграммы статусов
  const statusData = stats?.requestsByStatus
    ? Object.entries(stats.requestsByStatus).map(([name, value]) => ({
        name: name === 'NEW' ? 'Новые' :
              name === 'IN_PROGRESS' ? 'В работе' :
              name === 'COMPLETED' ? 'Завершенные' : 'Отмененные',
        value,
      }))
    : [];

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ru}>
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4">
            Dashboard
          </Typography>
          
          <Stack direction="row" spacing={2} alignItems="center">
            <DatePicker
              label="Начало периода"
              value={startDate}
              onChange={(newValue) => setStartDate(newValue)}
              slotProps={{ textField: { size: 'small' } }}
            />
            <DatePicker
              label="Конец периода"
              value={endDate}
              onChange={(newValue) => setEndDate(newValue)}
              slotProps={{ textField: { size: 'small' } }}
            />
            <Button
              variant="outlined"
              onClick={loadStats}
              disabled={loading}
            >
              Применить фильтр
            </Button>
            <Button
              variant="outlined"
              onClick={() => {
                setStartDate(null);
                setEndDate(null);
                loadStats();
              }}
            >
              Сбросить
            </Button>
            <Button
              variant="contained"
              startIcon={<Download />}
              onClick={handleExport}
              disabled={loading}
            >
              Экспорт CSV
            </Button>
          </Stack>
        </Box>

        {/* Карточки со статистикой */}
      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Всего туров"
            value={stats?.totalTours || 0}
            icon={<Tour fontSize="large" />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Активных туров"
            value={stats?.activeTours || 0}
            icon={<FlightTakeoff fontSize="large" />}
            color="success"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Новых заявок"
            value={stats?.newRequests || 0}
            icon={<Mail fontSize="large" />}
            color="warning"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Всего заявок"
            value={stats?.totalRequests || 0}
            icon={<TrendingUp fontSize="large" />}
            color="info"
          />
        </Grid>
      </Grid>

      {/* Графики */}
      <Grid container spacing={3} sx={{ mt: 1 }}>
        {/* График заявок по статусам */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Заявки по статусам
            </Typography>
            {loading ? (
              <Typography>Загрузка...</Typography>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={statusData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {statusData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* График популярных направлений */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Популярные направления
            </Typography>
            {loading ? (
              <Typography>Загрузка...</Typography>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={stats?.topDestinations || []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="destination" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="tourCount" fill="#8884d8" name="Туры" />
                  <Bar dataKey="requestCount" fill="#82ca9d" name="Заявки" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* График заявок по датам */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Заявки за последние 7 дней
            </Typography>
            {loading ? (
              <Typography>Загрузка...</Typography>
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={stats?.requestsByDate || []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="count" stroke="#8884d8" name="Заявки" />
                </LineChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>

        {/* Статистика по ценам */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Статистика по ценам туров
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Средняя цена
                  </Typography>
                  <Typography variant="h5">
                    {loading ? '...' : `${stats?.avgTourPrice.toFixed(2) || 0} ₽`}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Минимальная цена
                  </Typography>
                  <Typography variant="h5">
                    {loading ? '...' : `${stats?.minTourPrice.toFixed(2) || 0} ₽`}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={4}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Максимальная цена
                  </Typography>
                  <Typography variant="h5">
                    {loading ? '...' : `${stats?.maxTourPrice.toFixed(2) || 0} ₽`}
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
      </Box>
    </LocalizationProvider>
  );
}

