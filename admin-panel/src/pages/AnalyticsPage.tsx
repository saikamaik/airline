import React, { useEffect, useState, useRef } from 'react';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Stack,
  Button,
  TextField,
  Tabs,
  Tab,
  Alert,
  CircularProgress,
  Card,
  CardContent,
} from '@mui/material';
import {
  Download,
  TrendingUp,
  Psychology,
  ShowChart,
  Category,
  Assessment,
  Warning,
  ChevronLeft,
  ChevronRight,
} from '@mui/icons-material';
import Autocomplete from '@mui/material/Autocomplete';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import ru from 'date-fns/locale/ru';
import {
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
import { statisticsApi, Statistics } from '../api/statistics';
import { mlAnalyticsApi, MlAnalytics, MlDashboard, MlForecast, MlForecastTableRow, MlHealth, TourCluster, ModelMetrics } from '../api/mlAnalytics';

// Компонент для горизонтальной прокрутки с кнопками
function ScrollableGrid({ children }: { children: React.ReactNode }) {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [showLeftArrow, setShowLeftArrow] = useState(false);
  const [showRightArrow, setShowRightArrow] = useState(true);

  const checkScrollButtons = () => {
    if (scrollContainerRef.current) {
      const { scrollLeft, scrollWidth, clientWidth } = scrollContainerRef.current;
      setShowLeftArrow(scrollLeft > 0);
      setShowRightArrow(scrollLeft < scrollWidth - clientWidth - 10);
    }
  };

  useEffect(() => {
    checkScrollButtons();
    const container = scrollContainerRef.current;
    if (container) {
      container.addEventListener('scroll', checkScrollButtons);
      window.addEventListener('resize', checkScrollButtons);
      // Проверяем после небольшой задержки для корректной инициализации
      setTimeout(checkScrollButtons, 100);
      return () => {
        container.removeEventListener('scroll', checkScrollButtons);
        window.removeEventListener('resize', checkScrollButtons);
      };
    }
  }, [children]);

  const scroll = (direction: 'left' | 'right') => {
    if (scrollContainerRef.current) {
      const scrollAmount = 400;
      const currentScroll = scrollContainerRef.current.scrollLeft;
      scrollContainerRef.current.scrollTo({
        left: currentScroll + (direction === 'left' ? -scrollAmount : scrollAmount),
        behavior: 'smooth',
      });
    }
  };

  return (
    <Box sx={{ position: 'relative', width: '100%' }}>
      {showLeftArrow && (
        <Button
          onClick={() => scroll('left')}
          sx={{
            position: 'absolute',
            left: 0,
            top: '50%',
            transform: 'translateY(-50%)',
            zIndex: 10,
            minWidth: 40,
            minHeight: 40,
            borderRadius: '50%',
            bgcolor: 'background.paper',
            boxShadow: 2,
            '&:hover': {
              bgcolor: 'action.hover',
            },
          }}
        >
          <ChevronLeft />
        </Button>
      )}
      <Box
        ref={scrollContainerRef}
        sx={{
          display: 'flex',
          overflowX: 'auto',
          overflowY: 'hidden',
          scrollBehavior: 'smooth',
          '&::-webkit-scrollbar': {
            height: 8,
          },
          '&::-webkit-scrollbar-track': {
            background: 'transparent',
          },
          '&::-webkit-scrollbar-thumb': {
            background: 'rgba(0,0,0,0.2)',
            borderRadius: 4,
          },
          gap: 3,
          px: showLeftArrow || showRightArrow ? 5 : 0,
          '& > *': {
            flexShrink: 0,
            minWidth: 'fit-content',
          },
        }}
      >
        {children}
      </Box>
      {showRightArrow && (
        <Button
          onClick={() => scroll('right')}
          sx={{
            position: 'absolute',
            right: 0,
            top: '50%',
            transform: 'translateY(-50%)',
            zIndex: 10,
            minWidth: 40,
            minHeight: 40,
            borderRadius: '50%',
            bgcolor: 'background.paper',
            boxShadow: 2,
            '&:hover': {
              bgcolor: 'action.hover',
            },
          }}
        >
          <ChevronRight />
        </Button>
      )}
    </Box>
  );
}

export default function AnalyticsPage() {
  const [tabValue, setTabValue] = useState(0);
  const [stats, setStats] = useState<Statistics | null>(null);
  const [mlDashboard, setMlDashboard] = useState<MlDashboard | null>(null);
  const [mlAnalytics, setMlAnalytics] = useState<MlAnalytics | null>(null);
  const [mlForecast, setMlForecast] = useState<MlForecast | null>(null);
  const [mlForecastTable, setMlForecastTable] = useState<MlForecastTableRow[]>([]);
  const [mlHealth, setMlHealth] = useState<MlHealth | null>(null);
  const [loading, setLoading] = useState(true);
  const [mlLoading, setMlLoading] = useState(false);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
  const [period, setPeriod] = useState<string>('month');
  const [forecastDestination, setForecastDestination] = useState<string>('');
  const [availableDestinations, setAvailableDestinations] = useState<string[]>([]);
  const [tourClusters, setTourClusters] = useState<TourCluster[]>([]);
  const [modelMetrics, setModelMetrics] = useState<ModelMetrics[]>([]);
  const [anomalousTours, setAnomalousTours] = useState<any[]>([]);
  const [seasonalTrends, setSeasonalTrends] = useState<any[]>([]);
  const [seasonalForecast, setSeasonalForecast] = useState<any[]>([]);
  const [forecastMonths, setForecastMonths] = useState<number>(3);

  useEffect(() => {
    loadStats();
    loadMlData();
  }, []);

  // Автоматическая загрузка прогноза при открытии вкладки прогнозов
  useEffect(() => {
    if (tabValue === 2) {
      // Загружаем прогноз и таблицу при открытии вкладки прогнозов
      const loadForecastData = async () => {
        try {
          setMlLoading(true);
          const [forecast, table] = await Promise.all([
            mlAnalyticsApi.getForecast(forecastDestination || undefined),
            mlAnalyticsApi.getForecastTable()
          ]);
          console.log('Initial forecast loaded:', forecast);
          console.log('Initial table loaded:', table);
          if (forecast) {
            setMlForecast(forecast);
          }
          if (table && Array.isArray(table)) {
            setMlForecastTable(table);
          } else {
            console.warn('Table data is not an array:', table);
            setMlForecastTable([]);
          }
        } catch (error) {
          console.error('Error loading forecast:', error);
          setMlForecast(null);
          setMlForecastTable([]);
        } finally {
          setMlLoading(false);
        }
      };
      loadForecastData();
    }
  }, [tabValue]); // Загружаем только при открытии вкладки

  const loadMlData = async () => {
    try {
      setMlLoading(true);
      // Загружаем все ML данные параллельно, включая все направления
      const [dashboard, analytics, forecast, health, clusters, metrics, anomalies, seasonal, allDestinations] = await Promise.allSettled([
        mlAnalyticsApi.getDashboard(),
        mlAnalyticsApi.getFullAnalytics(period),
        mlAnalyticsApi.getForecast(forecastDestination || undefined),
        mlAnalyticsApi.checkHealth(),
        mlAnalyticsApi.getTourClusters(3),
        mlAnalyticsApi.getModelMetrics(),
        mlAnalyticsApi.getAnomalousTours(),
        mlAnalyticsApi.getSeasonalTrends(12),
        mlAnalyticsApi.getAllDestinations(),
      ]);

      // Загружаем все направления из базы
      if (allDestinations.status === 'fulfilled' && allDestinations.value) {
        setAvailableDestinations(allDestinations.value);
      } else {
        // Fallback: извлекаем из других источников
        const destinations = new Set<string>();
        if (dashboard.status === 'fulfilled' && dashboard.value) {
          if (dashboard.value.topDestinations) {
            dashboard.value.topDestinations.forEach(d => destinations.add(d.destination));
          }
          if (dashboard.value.revenueByDestination) {
            dashboard.value.revenueByDestination.forEach(d => destinations.add(d.destination));
          }
        }
        if (analytics.status === 'fulfilled' && analytics.value) {
          if (analytics.value.topDestinations) {
            analytics.value.topDestinations.forEach(d => destinations.add(d.destination));
          }
        }
        if (forecast.status === 'fulfilled' && forecast.value) {
          if (forecast.value.destinationBreakdown) {
            forecast.value.destinationBreakdown.forEach(d => destinations.add(d.destination));
          }
        }
        setAvailableDestinations(Array.from(destinations).sort());
      }

      if (dashboard.status === 'fulfilled') {
        setMlDashboard(dashboard.value);
      }
      if (analytics.status === 'fulfilled') {
        setMlAnalytics(analytics.value);
      }
      if (forecast.status === 'fulfilled') {
        setMlForecast(forecast.value);
      }
      
      // Загружаем табличный формат прогноза
      try {
        const table = await mlAnalyticsApi.getForecastTable();
        setMlForecastTable(table);
      } catch (error) {
        console.error('Error loading forecast table:', error);
      }
      if (health.status === 'fulfilled') setMlHealth(health.value);
      if (clusters.status === 'fulfilled') setTourClusters(clusters.value);
      if (metrics.status === 'fulfilled') setModelMetrics(metrics.value);
      if (anomalies.status === 'fulfilled') setAnomalousTours(anomalies.value);
      if (seasonal.status === 'fulfilled') setSeasonalTrends(seasonal.value);
    } catch (error) {
      console.error('Error loading ML data:', error);
    } finally {
      setMlLoading(false);
    }
  };

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
      link.download = `analytics_${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error exporting analytics:', error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'NEW':
        return 'warning';
      case 'IN_PROGRESS':
        return 'info';
      case 'COMPLETED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'NEW':
        return 'Новые';
      case 'IN_PROGRESS':
        return 'В работе';
      case 'COMPLETED':
        return 'Завершенные';
      case 'CANCELLED':
        return 'Отмененные';
      default:
        return status;
    }
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ru}>
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4">
            Аналитика и прогнозы
          </Typography>
        </Box>

        <Paper sx={{ mb: 3 }}>
          <Tabs 
            value={tabValue} 
            onChange={(_, newValue) => setTabValue(newValue)}
            variant="scrollable"
            scrollButtons="auto"
            allowScrollButtonsMobile
          >
            <Tab label="Базовая аналитика" icon={<ShowChart />} iconPosition="start" />
            <Tab label="ML Аналитика" icon={<Psychology />} iconPosition="start" />
            <Tab label="Прогнозы спроса" icon={<TrendingUp />} iconPosition="start" />
            <Tab label="Кластеризация" icon={<Category />} iconPosition="start" />
            <Tab label="Метрики моделей" icon={<Assessment />} iconPosition="start" />
            <Tab label="Аномалии" icon={<Warning />} iconPosition="start" />
          </Tabs>
        </Paper>

        {/* Базовая аналитика */}
        {tabValue === 0 && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          
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

        {loading ? (
          <Typography>Загрузка данных...</Typography>
        ) : (
          <Grid container spacing={3}>
            {/* Статистика по статусам */}
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Распределение заявок по статусам
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Статус</TableCell>
                        <TableCell align="right">Количество</TableCell>
                        <TableCell align="right">Процент</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {stats && Object.entries(stats.requestsByStatus).map(([status, count]) => {
                        const total = stats.totalRequests;
                        const percentage = total > 0 ? ((count / total) * 100).toFixed(1) : '0';
                        return (
                          <TableRow key={status}>
                            <TableCell>
                              <Chip
                                label={getStatusLabel(status)}
                                color={getStatusColor(status) as any}
                                size="small"
                              />
                            </TableCell>
                            <TableCell align="right">{count}</TableCell>
                            <TableCell align="right">{percentage}%</TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            </Grid>

            {/* Популярные направления */}
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Топ направлений
                </Typography>
                {stats && stats.topDestinations.length > 0 ? (
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Направление</TableCell>
                          <TableCell align="right">Туров</TableCell>
                          <TableCell align="right">Заявок</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {stats.topDestinations.map((dest, index) => (
                          <TableRow key={dest.destination}>
                            <TableCell>
                              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                <Chip
                                  label={index + 1}
                                  size="small"
                                  color="primary"
                                />
                                {dest.destination}
                              </Box>
                            </TableCell>
                            <TableCell align="right">{dest.tourCount}</TableCell>
                            <TableCell align="right">{dest.requestCount}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                ) : (
                  <Typography color="text.secondary">Нет данных</Typography>
                )}
              </Paper>
            </Grid>

            {/* График популярных направлений */}
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Популярные направления (график)
                </Typography>
                <ResponsiveContainer width="100%" height={400}>
                  <BarChart data={stats?.topDestinations || []}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="destination" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="tourCount" fill="#8884d8" name="Количество туров" />
                    <Bar dataKey="requestCount" fill="#82ca9d" name="Количество заявок" />
                  </BarChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>

            {/* График заявок по датам */}
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Динамика заявок по датам
                </Typography>
                <ResponsiveContainer width="100%" height={400}>
                  <LineChart data={stats?.requestsByDate || []}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="count"
                      stroke="#8884d8"
                      name="Количество заявок"
                      strokeWidth={2}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </Paper>
            </Grid>

            {/* Статистика по ценам */}
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Статистика по ценам туров
                </Typography>
                <ScrollableGrid>
                  <Box sx={{ minWidth: { xs: '100%', sm: 300 }, textAlign: 'center', p: 2, bgcolor: 'primary.lighter', borderRadius: 2 }}>
                    <Typography variant="caption" color="text.secondary">
                      Средняя цена
                    </Typography>
                    <Typography variant="h4" color="primary">
                      {stats?.avgTourPrice.toFixed(2) || 0} ₽
                    </Typography>
                  </Box>
                  <Box sx={{ minWidth: { xs: '100%', sm: 300 }, textAlign: 'center', p: 2, bgcolor: 'success.lighter', borderRadius: 2 }}>
                    <Typography variant="caption" color="text.secondary">
                      Минимальная цена
                    </Typography>
                    <Typography variant="h4" color="success.main">
                      {stats?.minTourPrice.toFixed(2) || 0} ₽
                    </Typography>
                  </Box>
                  <Box sx={{ minWidth: { xs: '100%', sm: 300 }, textAlign: 'center', p: 2, bgcolor: 'error.lighter', borderRadius: 2 }}>
                    <Typography variant="caption" color="text.secondary">
                      Максимальная цена
                    </Typography>
                    <Typography variant="h4" color="error.main">
                      {stats?.maxTourPrice.toFixed(2) || 0} ₽
                    </Typography>
                  </Box>
                </ScrollableGrid>
              </Paper>
            </Grid>
          </Grid>
        )}
        </Box>
        )}

        {/* ML Аналитика */}
        {tabValue === 1 && (
          <Box>
            {mlHealth && (
              <Alert 
                severity={mlHealth.status === 'ok' ? 'success' : 'warning'} 
                sx={{ mb: 3 }}
              >
                ML сервис: {mlHealth.ml_service === 'available' ? 'Доступен' : 'Недоступен'}
              </Alert>
            )}

            {mlLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Grid container spacing={3}>
                {/* Временные ряды с сезонностью + прогноз */}
                {(seasonalTrends.length > 0 || seasonalForecast.length > 0) && (
                  <Grid item xs={12}>
                    <Paper sx={{ p: 3 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Typography variant="h6">
                          Сезонные тренды и прогноз
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                          <TextField
                            select
                            size="small"
                            label="Прогноз на"
                            value={forecastMonths}
                            onChange={async (e) => {
                              const months = Number(e.target.value);
                              setForecastMonths(months);
                              setMlLoading(true);
                              try {
                                const forecast = await mlAnalyticsApi.getSeasonalForecast(months);
                                setSeasonalForecast(forecast);
                              } catch (error) {
                                console.error('Error loading forecast:', error);
                              } finally {
                                setMlLoading(false);
                              }
                            }}
                            sx={{ minWidth: 150 }}
                          >
                            <MenuItem value={1}>1 месяц</MenuItem>
                            <MenuItem value={2}>2 месяца</MenuItem>
                            <MenuItem value={3}>3 месяца</MenuItem>
                            <MenuItem value={4}>4 месяца</MenuItem>
                            <MenuItem value={5}>5 месяцев</MenuItem>
                            <MenuItem value={6}>6 месяцев</MenuItem>
                          </TextField>
                        </Box>
                      </Box>
                      <ResponsiveContainer width="100%" height={400}>
                        <LineChart data={[...seasonalTrends, ...seasonalForecast]}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis dataKey="month_name" />
                          <YAxis yAxisId="left" />
                          <YAxis yAxisId="right" orientation="right" />
                          <Tooltip 
                            content={({ active, payload }) => {
                              if (active && payload && payload.length) {
                                const data = payload[0].payload;
                                const isForecast = data.is_forecast;
                                return (
                                  <Box sx={{ 
                                    bgcolor: 'background.paper', 
                                    p: 2, 
                                    border: 1, 
                                    borderColor: isForecast ? 'warning.main' : 'divider',
                                    borderRadius: 1,
                                    boxShadow: 2
                                  }}>
                                    <Typography variant="subtitle2" gutterBottom>
                                      {data.month_name}
                                      {isForecast && (
                                        <Chip 
                                          label="Прогноз" 
                                          size="small" 
                                          color="warning" 
                                          sx={{ ml: 1 }} 
                                        />
                                      )}
                                    </Typography>
                                    <Typography variant="body2">
                                      Заявок: <strong>{data.request_count}</strong>
                                    </Typography>
                                    <Typography variant="body2">
                                      Средняя цена: <strong>{data.avg_price.toLocaleString('ru-RU')} ₽</strong>
                                    </Typography>
                                    {data.top_destinations && data.top_destinations.length > 0 && (
                                      <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
                                        {data.top_destinations.join(', ')}
                                      </Typography>
                                    )}
                                  </Box>
                                );
                              }
                              return null;
                            }}
                          />
                          <Legend />
                          <Line
                            yAxisId="left"
                            type="monotone"
                            dataKey="request_count"
                            stroke="#8884d8"
                            name="Количество заявок"
                            strokeWidth={2}
                            dot={(props) => {
                              const { cx, cy, payload } = props;
                              return payload.is_forecast ? (
                                <circle cx={cx} cy={cy} r={4} fill="#ff9800" stroke="#ff9800" strokeWidth={2} />
                              ) : (
                                <circle cx={cx} cy={cy} r={4} fill="#8884d8" stroke="#8884d8" strokeWidth={2} />
                              );
                            }}
                            strokeDasharray={(entry) => entry?.is_forecast ? "5 5" : ""}
                          />
                          <Line
                            yAxisId="right"
                            type="monotone"
                            dataKey="avg_price"
                            stroke="#ff7300"
                            name="Средняя цена (₽)"
                            strokeWidth={2}
                            dot={(props) => {
                              const { cx, cy, payload } = props;
                              return payload.is_forecast ? (
                                <circle cx={cx} cy={cy} r={4} fill="#ff9800" stroke="#ff9800" strokeWidth={2} />
                              ) : (
                                <circle cx={cx} cy={cy} r={4} fill="#ff7300" stroke="#ff7300" strokeWidth={2} />
                              );
                            }}
                            strokeDasharray={(entry) => entry?.is_forecast ? "5 5" : ""}
                          />
                        </LineChart>
                      </ResponsiveContainer>
                      <Alert severity="info" sx={{ mt: 2 }}>
                        <Typography variant="body2">
                          <strong>Прогноз</strong> отображается пунктирной линией и оранжевыми точками. 
                          Модель анализирует исторические данные за 24 месяца и учитывает сезонные паттерны.
                        </Typography>
                      </Alert>
                      <Box sx={{ mt: 2 }}>
                        <Typography variant="subtitle2" gutterBottom>
                          Топ направлений по месяцам:
                        </Typography>
                        <Grid container spacing={2}>
                          {[...seasonalTrends, ...seasonalForecast].map((trend, idx) => (
                            <Grid item xs={12} sm={6} md={4} lg={3} key={idx}>
                              <Paper sx={{ 
                                p: 1.5, 
                                bgcolor: trend.is_forecast ? 'warning.lighter' : 'grey.50',
                                border: trend.is_forecast ? 1 : 0,
                                borderColor: trend.is_forecast ? 'warning.main' : 'transparent'
                              }}>
                                <Typography variant="body2" fontWeight="bold">
                                  {trend.month_name}
                                  {trend.is_forecast && (
                                    <Chip label="Прогноз" size="small" color="warning" sx={{ ml: 1, height: 16 }} />
                                  )}
                                </Typography>
                                <Typography variant="caption" color="text.secondary">
                                  {trend.request_count} заявок, {trend.avg_price.toLocaleString('ru-RU')} ₽
                                </Typography>
                                {trend.top_destinations && trend.top_destinations.length > 0 && (
                                  <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
                                    {trend.top_destinations.join(', ')}
                                  </Typography>
                                )}
                              </Paper>
                            </Grid>
                          ))}
                        </Grid>
                      </Box>
                    </Paper>
                  </Grid>
                )}

                {/* ML Dashboard данные */}
                {mlDashboard && (
                  <>
                    <Grid item xs={12}>
                      <ScrollableGrid>
                        <Card sx={{ minWidth: { xs: 280, md: 320 } }}>
                          <CardContent>
                            <Typography variant="h6" gutterBottom>
                              Всего заявок (ML)
                            </Typography>
                            <Typography variant="h3" color="primary">
                              {mlDashboard.totalRequests}
                            </Typography>
                          </CardContent>
                        </Card>
                        <Card sx={{ minWidth: { xs: 280, md: 320 } }}>
                          <CardContent>
                            <Typography variant="h6" gutterBottom>
                              Общая выручка (ML)
                            </Typography>
                            <Typography variant="h3" color="success.main">
                              {mlDashboard.totalRevenue?.toLocaleString('ru-RU') || 0} ₽
                            </Typography>
                          </CardContent>
                        </Card>
                        <Card sx={{ minWidth: { xs: 280, md: 320 } }}>
                          <CardContent>
                            <Typography variant="h6" gutterBottom>
                              Средний чек (ML)
                            </Typography>
                            <Typography variant="h3" color="info.main">
                              {mlDashboard.averageRequestValue?.toLocaleString('ru-RU') || 0} ₽
                            </Typography>
                          </CardContent>
                        </Card>
                      </ScrollableGrid>
                    </Grid>

                    {/* Топ направлений ML */}
                    {mlDashboard.topDestinations && mlDashboard.topDestinations.length > 0 && (
                      <Grid item xs={12} md={6}>
                        <Paper sx={{ p: 3 }}>
                          <Typography variant="h6" gutterBottom>
                            Топ направлений (ML анализ)
                          </Typography>
                          <TableContainer>
                            <Table size="small">
                              <TableHead>
                                <TableRow>
                                  <TableCell>Направление</TableCell>
                                  <TableCell align="right">Заявок</TableCell>
                                </TableRow>
                              </TableHead>
                              <TableBody>
                                {mlDashboard.topDestinations.map((dest, index) => (
                                  <TableRow key={dest.destination}>
                                    <TableCell>
                                      <Chip label={index + 1} size="small" color="primary" sx={{ mr: 1 }} />
                                      {dest.destination}
                                    </TableCell>
                                    <TableCell align="right">{dest.count}</TableCell>
                                  </TableRow>
                                ))}
                              </TableBody>
                            </Table>
                          </TableContainer>
                        </Paper>
                      </Grid>
                    )}

                    {/* Тренды ML */}
                    {mlDashboard.recentTrends && mlDashboard.recentTrends.length > 0 && (
                      <Grid item xs={12} md={6}>
                        <Paper sx={{ p: 3 }}>
                          <Typography variant="h6" gutterBottom>
                            Последние тренды (ML)
                          </Typography>
                          <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={mlDashboard.recentTrends}>
                              <CartesianGrid strokeDasharray="3 3" />
                              <XAxis dataKey="date" />
                              <YAxis />
                              <Tooltip />
                              <Legend />
                              <Line
                                type="monotone"
                                dataKey="value"
                                stroke="#8884d8"
                                name="Значение"
                                strokeWidth={2}
                              />
                            </LineChart>
                          </ResponsiveContainer>
                        </Paper>
                      </Grid>
                    )}
                  </>
                )}

                {/* Полная ML аналитика */}
                {mlAnalytics && (
                  <>
                    {mlAnalytics.trends && mlAnalytics.trends.length > 0 && (
                      <Grid item xs={12}>
                        <Paper sx={{ p: 3 }}>
                          <Typography variant="h6" gutterBottom>
                            Тренды за период: {mlAnalytics.period}
                          </Typography>
                          <ResponsiveContainer width="100%" height={400}>
                            <LineChart data={mlAnalytics.trends}>
                              <CartesianGrid strokeDasharray="3 3" />
                              <XAxis dataKey="date" />
                              <YAxis yAxisId="left" />
                              <YAxis yAxisId="right" orientation="right" />
                              <Tooltip />
                              <Legend />
                              <Line
                                yAxisId="left"
                                type="monotone"
                                dataKey="requests"
                                stroke="#8884d8"
                                name="Заявки"
                                strokeWidth={2}
                              />
                              <Line
                                yAxisId="right"
                                type="monotone"
                                dataKey="revenue"
                                stroke="#82ca9d"
                                name="Выручка (₽)"
                                strokeWidth={2}
                              />
                            </LineChart>
                          </ResponsiveContainer>
                        </Paper>
                      </Grid>
                    )}

                    {mlAnalytics.topDestinations && mlAnalytics.topDestinations.length > 0 && (
                      <Grid item xs={12}>
                        <Paper sx={{ p: 3 }}>
                          <Typography variant="h6" gutterBottom>
                            Топ направлений с выручкой (ML)
                          </Typography>
                          <ResponsiveContainer width="100%" height={400}>
                            <BarChart data={mlAnalytics.topDestinations}>
                              <CartesianGrid strokeDasharray="3 3" />
                              <XAxis dataKey="destination" />
                              <YAxis yAxisId="left" />
                              <YAxis yAxisId="right" orientation="right" />
                              <Tooltip />
                              <Legend />
                              <Bar yAxisId="left" dataKey="count" fill="#8884d8" name="Количество заявок" />
                              <Bar yAxisId="right" dataKey="revenue" fill="#82ca9d" name="Выручка (₽)" />
                            </BarChart>
                          </ResponsiveContainer>
                        </Paper>
                      </Grid>
                    )}
                  </>
                )}

                {!mlDashboard && !mlAnalytics && (
                  <Grid item xs={12}>
                    <Alert severity="info">
                      ML данные недоступны. Убедитесь, что ML сервис запущен и доступен.
                    </Alert>
                  </Grid>
                )}
              </Grid>
            )}

            <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
              <Button
                variant="outlined"
                onClick={loadMlData}
                disabled={mlLoading}
              >
                Обновить данные
              </Button>
              <TextField
                select
                label="Период"
                value={period}
                onChange={(e) => {
                  setPeriod(e.target.value);
                  loadMlData();
                }}
                size="small"
                sx={{ minWidth: 150 }}
                SelectProps={{
                  native: true,
                }}
              >
                <option value="week">Неделя</option>
                <option value="month">Месяц</option>
                <option value="quarter">Квартал</option>
                <option value="year">Год</option>
              </TextField>
            </Box>
          </Box>
        )}

        {/* Прогнозы спроса */}
        {tabValue === 2 && (
          <Box>
            <Box sx={{ mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
              <Autocomplete
                options={availableDestinations}
                value={forecastDestination || null}
                onChange={async (_, newValue) => {
                  const destination = newValue || '';
                  setForecastDestination(destination);
                  // Автоматически загружаем прогноз при изменении направления
                  try {
                    setMlLoading(true);
                    const [forecast, table] = await Promise.all([
                      mlAnalyticsApi.getForecast(destination || undefined),
                      mlAnalyticsApi.getForecastTable()
                    ]);
                    console.log('Forecast loaded for destination:', destination, forecast);
                    console.log('Table loaded:', table);
                    if (forecast) {
                      setMlForecast(forecast);
                    }
                    if (table && Array.isArray(table)) {
                      setMlForecastTable(table);
                    } else {
                      console.warn('Table data is not an array:', table);
                      setMlForecastTable([]);
                    }
                  } catch (error) {
                    console.error('Error loading forecast:', error);
                    setMlForecast(null);
                    setMlForecastTable([]);
                  } finally {
                    setMlLoading(false);
                  }
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Направление (опционально)"
                    size="small"
                    placeholder="Выберите направление или оставьте пустым"
                    sx={{ minWidth: 300 }}
                  />
                )}
                freeSolo
                clearOnBlur
              />
              <Button
                variant="contained"
                onClick={async () => {
                  try {
                    setMlLoading(true);
                    const [forecast, table] = await Promise.all([
                      mlAnalyticsApi.getForecast(forecastDestination || undefined),
                      mlAnalyticsApi.getForecastTable()
                    ]);
                    setMlForecast(forecast);
                    setMlForecastTable(table);
                  } catch (error) {
                    console.error('Error loading forecast:', error);
                  } finally {
                    setMlLoading(false);
                  }
                }}
                disabled={mlLoading}
              >
                Обновить прогноз
              </Button>
            </Box>

            {mlLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : mlForecast ? (
              <Grid container spacing={3}>
                {mlForecast.forecast && Array.isArray(mlForecast.forecast) && mlForecast.forecast.length > 0 && (
                  <Grid item xs={12}>
                    <Paper sx={{ p: 3 }}>
                      <Typography variant="h6" gutterBottom>
                        Прогноз спроса {mlForecast.destination ? `для ${mlForecast.destination}` : '(общий)'}
                      </Typography>
                      {mlForecast.totalPredictedRevenue && (
                        <Alert severity="info" sx={{ mb: 2 }}>
                          Общая прогнозируемая выручка: {mlForecast.totalPredictedRevenue.toLocaleString('ru-RU')} ₽
                        </Alert>
                      )}
                      <ResponsiveContainer width="100%" height={400} key={`forecast-chart-${forecastDestination || 'all'}`}>
                        <LineChart data={mlForecast.forecast}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis 
                            dataKey="date" 
                            label={{ value: 'Месяц', position: 'insideBottom', offset: -5 }}
                          />
                          <YAxis 
                            yAxisId="left" 
                            label={{ value: 'Спрос (заявок)', angle: -90, position: 'insideLeft' }}
                          />
                          <YAxis 
                            yAxisId="right" 
                            orientation="right"
                            label={{ value: 'Выручка (₽)', angle: 90, position: 'insideRight' }}
                          />
                          <Tooltip 
                            formatter={(value: any, name: string) => {
                              if (name === 'Прогнозируемая выручка (₽)') {
                                return [value.toLocaleString('ru-RU') + ' ₽', name];
                              }
                              return [value, name];
                            }}
                          />
                          <Legend />
                          <Line
                            yAxisId="left"
                            type="monotone"
                            dataKey="predictedDemand"
                            stroke="#8884d8"
                            name="Прогнозируемый спрос (заявок)"
                            strokeWidth={2}
                          />
                          {mlForecast.forecast[0]?.predictedRevenue !== undefined && (
                            <Line
                              yAxisId="right"
                              type="monotone"
                              dataKey="predictedRevenue"
                              stroke="#82ca9d"
                              name="Прогнозируемая выручка (₽)"
                              strokeWidth={2}
                            />
                          )}
                          <Line
                            yAxisId="left"
                            type="monotone"
                            dataKey="confidence"
                            stroke="#82ca9d"
                            name="Уверенность"
                            strokeWidth={2}
                          />
                        </LineChart>
                      </ResponsiveContainer>
                    </Paper>
                  </Grid>
                )}

                {/* Таблица прогноза спроса в формате: Направление | Текущий спрос | Прогноз | Изменение | Тренд | Уверенность | Рекомендация */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 3, width: '100%', overflow: 'hidden' }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                      <Typography variant="h6">
                        Прогноз спроса по направлениям (табличный формат)
                      </Typography>
                      <Button
                        variant="outlined"
                        onClick={async () => {
                          try {
                            setMlLoading(true);
                            const table = await mlAnalyticsApi.getForecastTable();
                            setMlForecastTable(table);
                          } catch (error) {
                            console.error('Error loading forecast table:', error);
                            setMlForecastTable([]);
                          } finally {
                            setMlLoading(false);
                          }
                        }}
                        disabled={mlLoading}
                        size="small"
                      >
                        Обновить таблицу
                      </Button>
                    </Box>
                    {mlLoading ? (
                      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                        <CircularProgress />
                      </Box>
                    ) : mlForecastTable && mlForecastTable.length > 0 ? (
                      <Box sx={{ width: '100%', overflowX: 'auto' }}>
                        <TableContainer>
                          <Table sx={{ minWidth: 1200 }}>
                            <TableHead>
                              <TableRow>
                                <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Направление</TableCell>
                                <TableCell align="right" sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Текущий спрос (заявок/неделю)</TableCell>
                                <TableCell align="right" sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Прогноз (заявок/неделю)</TableCell>
                                <TableCell align="right" sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Изменение</TableCell>
                                <TableCell align="center" sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Тренд</TableCell>
                                <TableCell align="right" sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Уверенность</TableCell>
                                <TableCell sx={{ fontWeight: 'bold', backgroundColor: 'background.paper' }}>Рекомендация</TableCell>
                              </TableRow>
                            </TableHead>
                            <TableBody>
                              {mlForecastTable.map((row, idx) => (
                                <TableRow key={`forecast-${row.destination}-${idx}`} hover>
                                  <TableCell sx={{ fontWeight: 'medium' }}>{row.destination}</TableCell>
                                  <TableCell align="right">{row.current_demand_per_week}</TableCell>
                                  <TableCell align="right">{row.predicted_demand_per_week}</TableCell>
                                  <TableCell align="right">
                                    <Typography
                                      variant="body2"
                                      sx={{
                                        color: row.change_percent > 0 ? 'success.main' : row.change_percent < 0 ? 'error.main' : 'text.secondary',
                                        fontWeight: 'bold'
                                      }}
                                    >
                                      {row.change_percent > 0 ? '+' : ''}{row.change_percent.toFixed(1)}%
                                    </Typography>
                                  </TableCell>
                                  <TableCell align="center">
                                    <Chip
                                      label={row.trend}
                                      color={
                                        row.trend === 'Растущий' ? 'success' :
                                        row.trend === 'Падающий' ? 'error' : 'default'
                                      }
                                      size="small"
                                    />
                                  </TableCell>
                                  <TableCell align="right">
                                    {(row.confidence * 100).toFixed(0)}%
                                  </TableCell>
                                  <TableCell>
                                    <Typography variant="body2" sx={{ whiteSpace: 'normal', wordBreak: 'break-word', maxWidth: 400 }}>
                                      {row.recommendation}
                                    </Typography>
                                  </TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </TableContainer>
                      </Box>
                    ) : (
                      <Alert severity="info">
                        {mlForecastTable && mlForecastTable.length === 0 
                          ? 'Данные прогноза отсутствуют. Попробуйте обновить таблицу или проверьте доступность ML сервиса.'
                          : 'Нажмите "Обновить таблицу" для загрузки данных прогноза'}
                      </Alert>
                    )}
                  </Paper>
                </Grid>

                {mlForecast.destinationBreakdown && mlForecast.destinationBreakdown.length > 0 && (
                  <Grid item xs={12}>
                    <Paper sx={{ p: 3 }}>
                      <Typography variant="h6" gutterBottom>
                        Прогноз по направлениям (детальный)
                      </Typography>
                      <TableContainer>
                        <Table>
                          <TableHead>
                            <TableRow>
                              <TableCell>Направление</TableCell>
                              <TableCell align="right">Прогнозируемый спрос</TableCell>
                              <TableCell align="right">Прогнозируемая выручка (₽)</TableCell>
                              <TableCell align="center">Тренд</TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {mlForecast.destinationBreakdown.map((dest, idx) => (
                              <TableRow key={idx}>
                                <TableCell>{dest.destination}</TableCell>
                                <TableCell align="right">{dest.predictedDemand}</TableCell>
                                <TableCell align="right">{dest.predictedRevenue.toLocaleString('ru-RU')}</TableCell>
                                <TableCell align="center">
                                  <Chip
                                    label={dest.trend === 'rising' ? 'Рост' : dest.trend === 'falling' ? 'Спад' : 'Стабильно'}
                                    color={dest.trend === 'rising' ? 'success' : dest.trend === 'falling' ? 'error' : 'default'}
                                    size="small"
                                  />
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </Paper>
                  </Grid>
                )}

                {mlForecast.recommendations && mlForecast.recommendations.length > 0 && (
                  <Grid item xs={12}>
                    <Paper sx={{ p: 3 }}>
                      <Typography variant="h6" gutterBottom>
                        Рекомендации ML
                      </Typography>
                      <Stack spacing={1}>
                        {mlForecast.recommendations.map((rec, index) => (
                          <Alert key={index} severity="info">
                            {rec}
                          </Alert>
                        ))}
                      </Stack>
                    </Paper>
                  </Grid>
                )}

                {(!mlForecast.forecast || mlForecast.forecast.length === 0) && (
                  <Grid item xs={12}>
                    <Alert severity="info">
                      Прогноз недоступен. Попробуйте выбрать другое направление или проверьте доступность ML сервиса.
                    </Alert>
                  </Grid>
                )}
              </Grid>
            ) : (
              <Alert severity="info">
                Нажмите "Получить прогноз" для загрузки прогноза спроса
              </Alert>
            )}
          </Box>
        )}

        {/* Кластеризация туров */}
        {tabValue === 3 && (
          <Box>
            {mlLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : tourClusters && tourClusters.length > 0 ? (
              <ScrollableGrid>
                {tourClusters.map((cluster) => (
                  <Paper key={cluster.cluster_id} sx={{ p: 3, minWidth: { xs: 300, md: 400 } }}>
                    <Typography variant="h6" gutterBottom>
                      {cluster.description}
                    </Typography>
                    <Chip
                      label={cluster.cluster_type}
                      color={cluster.cluster_type === 'премиум' ? 'primary' : cluster.cluster_type === 'бюджетные' ? 'default' : 'secondary'}
                      sx={{ mb: 2 }}
                    />
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Средняя цена: {cluster.avg_price.toLocaleString('ru-RU')} ₽
                    </Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Средняя длительность: {cluster.avg_duration} дней
                    </Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Популярность: {cluster.total_popularity} заявок
                    </Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Конверсия: {(cluster.avg_conversion * 100).toFixed(1)}%
                    </Typography>
                    <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
                      Туры в кластере:
                    </Typography>
                    <Stack spacing={0.5}>
                      {cluster.tours.map((tour) => (
                        <Typography key={tour.tour_id} variant="body2">
                          • {tour.tour_name} ({tour.destination}) - {tour.price.toLocaleString('ru-RU')} ₽
                        </Typography>
                      ))}
                    </Stack>
                  </Paper>
                ))}
              </ScrollableGrid>
            ) : (
              <Alert severity="info">Кластеры не найдены. Недостаточно данных для кластеризации.</Alert>
            )}
          </Box>
        )}

        {/* Метрики моделей */}
        {tabValue === 4 && (
          <Box>
            <Alert severity="info" sx={{ mb: 3 }}>
              <Typography variant="subtitle2" gutterBottom>
                <strong>Описание метрик:</strong>
              </Typography>
              <Typography variant="body2" component="div">
                <strong>R² (коэффициент детерминации)</strong> — показывает, насколько хорошо модель объясняет данные. 
                Значение от 0 до 1: чем ближе к 1, тем лучше модель. 
                <br />• R² &gt; 0.7 — отличная модель
                <br />• R² 0.5-0.7 — хорошая модель
                <br />• R² &lt; 0.5 — модель требует улучшения
              </Typography>
              <Typography variant="body2" sx={{ mt: 1 }} component="div">
                <strong>MAE (Mean Absolute Error)</strong> — средняя абсолютная ошибка. 
                Показывает, на сколько в среднем ошибается модель (в единицах измерения). 
                Чем меньше, тем лучше.
              </Typography>
              <Typography variant="body2" sx={{ mt: 1 }} component="div">
                <strong>RMSE (Root Mean Squared Error)</strong> — корень из средней квадратичной ошибки. 
                Более чувствителен к большим ошибкам, чем MAE. Чем меньше, тем лучше.
              </Typography>
              <Typography variant="body2" sx={{ mt: 1 }} component="div">
                <strong>Веса моделей</strong> — показывают, какой вклад каждая модель вносит в финальный прогноз ансамбля. 
                Веса рассчитываются на основе качества каждой модели (R²).
              </Typography>
            </Alert>
            {mlLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : modelMetrics && modelMetrics.length > 0 ? (
              <ScrollableGrid>
                {modelMetrics.map((metric, idx) => (
                  <Paper key={idx} sx={{ p: 3, minWidth: { xs: 300, md: 400 } }}>
                    <Typography variant="h6" gutterBottom>
                      {metric.destination}
                    </Typography>
                    <TableContainer>
                      <Table size="small">
                        <TableBody>
                          <TableRow>
                            <TableCell>Linear Regression R²</TableCell>
                            <TableCell align="right">{metric.linear_r2.toFixed(3)}</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell>Random Forest R²</TableCell>
                            <TableCell align="right">{metric.random_forest_r2.toFixed(3)}</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell>Gradient Boosting R²</TableCell>
                            <TableCell align="right">{metric.gradient_boosting_r2.toFixed(3)}</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell><strong>Ансамбль R²</strong></TableCell>
                            <TableCell align="right"><strong>{metric.ensemble_r2.toFixed(3)}</strong></TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell>Ансамбль MAE</TableCell>
                            <TableCell align="right">{metric.ensemble_mae.toFixed(2)}</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell>Ансамбль RMSE</TableCell>
                            <TableCell align="right">{metric.ensemble_rmse.toFixed(2)}</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell colSpan={2}><strong>Веса моделей:</strong></TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell sx={{ pl: 4 }}>Linear</TableCell>
                            <TableCell align="right">{(metric.weights.linear * 100).toFixed(1)}%</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell sx={{ pl: 4 }}>Random Forest</TableCell>
                            <TableCell align="right">{(metric.weights.random_forest * 100).toFixed(1)}%</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell sx={{ pl: 4 }}>Gradient Boosting</TableCell>
                            <TableCell align="right">{(metric.weights.gradient_boosting * 100).toFixed(1)}%</TableCell>
                          </TableRow>
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </Paper>
                ))}
              </ScrollableGrid>
            ) : (
              <Alert severity="info">Метрики моделей недоступны. Недостаточно данных для обучения моделей.</Alert>
            )}
          </Box>
        )}

        {/* Аномальные туры */}
        {tabValue === 5 && (
          <Box>
            {mlLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : anomalousTours && anomalousTours.length > 0 ? (
              <Grid container spacing={3}>
                {anomalousTours.map((anomaly, idx) => (
                  <Grid item xs={12} key={idx}>
                    <Paper sx={{ p: 3 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 2 }}>
                        <Box>
                          <Typography variant="h6" gutterBottom>
                            {anomaly.tour_name}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {anomaly.destination}
                          </Typography>
                        </Box>
                        <Chip
                          label={
                            anomaly.anomaly_type === 'high_demand_low_price' ? 'Высокий спрос, низкая цена' :
                            anomaly.anomaly_type === 'low_demand_high_price' ? 'Низкий спрос, высокая цена' :
                            'Необычный паттерн'
                          }
                          color={
                            anomaly.anomaly_type === 'high_demand_low_price' ? 'success' :
                            anomaly.anomaly_type === 'low_demand_high_price' ? 'error' :
                            'warning'
                          }
                        />
                      </Box>
                      <Grid container spacing={2} sx={{ mb: 2 }}>
                        <Grid item xs={6} md={3}>
                          <Typography variant="body2" color="text.secondary">Текущая цена</Typography>
                          <Typography variant="h6">{anomaly.current_price.toLocaleString('ru-RU')} ₽</Typography>
                        </Grid>
                        <Grid item xs={6} md={3}>
                          <Typography variant="body2" color="text.secondary">Оценка спроса</Typography>
                          <Typography variant="h6">{(anomaly.demand_score * 100).toFixed(0)}%</Typography>
                        </Grid>
                        <Grid item xs={6} md={3}>
                          <Typography variant="body2" color="text.secondary">Оценка цены</Typography>
                          <Typography variant="h6">{(anomaly.price_score * 100).toFixed(0)}%</Typography>
                        </Grid>
                        <Grid item xs={6} md={3}>
                          <Typography variant="body2" color="text.secondary">Влияние на выручку</Typography>
                          <Typography variant="h6" color={anomaly.expected_revenue_impact > 0 ? 'success.main' : 'error.main'}>
                            {anomaly.expected_revenue_impact > 0 ? '+' : ''}{anomaly.expected_revenue_impact.toLocaleString('ru-RU')} ₽
                          </Typography>
                        </Grid>
                      </Grid>
                      <Alert severity="info">{anomaly.recommendation}</Alert>
                    </Paper>
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Alert severity="info">Аномальные туры не найдены. Все туры соответствуют ожидаемым паттернам.</Alert>
            )}
          </Box>
        )}
      </Box>
    </LocalizationProvider>
  );
}

