import { useEffect, useState } from 'react';
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
} from '@mui/material';
import {
  Download,
  TrendingUp,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ru } from 'date-fns/locale';
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
import { statisticsApi, Statistics, DestinationStat } from '../api/statistics';

export default function AnalyticsPage() {
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
            Детальная аналитика
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
                <Grid container spacing={3}>
                  <Grid item xs={12} sm={4}>
                    <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'primary.lighter', borderRadius: 2 }}>
                      <Typography variant="caption" color="text.secondary">
                        Средняя цена
                      </Typography>
                      <Typography variant="h4" color="primary">
                        {stats?.avgTourPrice.toFixed(2) || 0} ₽
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={4}>
                    <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'success.lighter', borderRadius: 2 }}>
                      <Typography variant="caption" color="text.secondary">
                        Минимальная цена
                      </Typography>
                      <Typography variant="h4" color="success.main">
                        {stats?.minTourPrice.toFixed(2) || 0} ₽
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={4}>
                    <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'error.lighter', borderRadius: 2 }}>
                      <Typography variant="caption" color="text.secondary">
                        Максимальная цена
                      </Typography>
                      <Typography variant="h4" color="error.main">
                        {stats?.maxTourPrice.toFixed(2) || 0} ₽
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>
              </Paper>
            </Grid>
          </Grid>
        )}
      </Box>
    </LocalizationProvider>
  );
}

