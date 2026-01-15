import { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Card,
  CardContent,
  Stack,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import {
  TrendingUp,
  Assignment,
  Person,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ru } from 'date-fns/locale';
import { employeeApi } from '../api/employeeApi';
import { EmployeeDto, EmployeeSalesDto } from '../api/employees';
import { ClientRequestDto } from '../types';

const statusLabels: Record<string, string> = {
  NEW: 'Новая',
  IN_PROGRESS: 'В обработке',
  COMPLETED: 'Завершена',
  CANCELLED: 'Отменена',
};

const statusColors: Record<string, 'default' | 'primary' | 'success' | 'error'> = {
  NEW: 'primary',
  IN_PROGRESS: 'default',
  COMPLETED: 'success',
  CANCELLED: 'error',
};

export default function EmployeeDashboard() {
  const [profile, setProfile] = useState<EmployeeDto | null>(null);
  const [requests, setRequests] = useState<ClientRequestDto[]>([]);
  const [sales, setSales] = useState<EmployeeSalesDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

  useEffect(() => {
    loadData();
  }, [page, statusFilter]);

  useEffect(() => {
    if (startDate && endDate) {
      loadSales();
    }
  }, [startDate, endDate]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [profileData, requestsData] = await Promise.all([
        employeeApi.getProfile(),
        employeeApi.getMyRequests(statusFilter || undefined, page, 20),
      ]);
      setProfile(profileData);
      setRequests(requestsData.content);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadSales = async () => {
    try {
      const start = startDate ? startDate.toISOString().split('T')[0] : undefined;
      const end = endDate ? endDate.toISOString().split('T')[0] : undefined;
      const salesData = await employeeApi.getMySales(start, end);
      setSales(salesData);
    } catch (error) {
      console.error('Error loading sales:', error);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ru-RU');
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ru}>
      <Box>
        <Typography variant="h4" gutterBottom>
          Панель сотрудника
        </Typography>

        {/* Профиль */}
        {profile && (
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Stack direction="row" spacing={2} alignItems="center">
                <Person fontSize="large" />
                <Box>
                  <Typography variant="h6">
                    {profile.firstName} {profile.lastName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {profile.email}
                  </Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        )}

        {/* Статистика */}
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Stack direction="row" spacing={2} alignItems="center">
                  <Assignment color="primary" />
                  <Box>
                    <Typography color="text.secondary">Всего заявок</Typography>
                    <Typography variant="h4">{requests.length}</Typography>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Stack direction="row" spacing={2} alignItems="center">
                  <TrendingUp color="success" />
                  <Box>
                    <Typography color="text.secondary">Продажи</Typography>
                    <Typography variant="h4">{sales?.totalSales || 0}</Typography>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Stack direction="row" spacing={2} alignItems="center">
                  <TrendingUp color="primary" />
                  <Box>
                    <Typography color="text.secondary">Выручка</Typography>
                    <Typography variant="h4">
                      {sales?.totalRevenue.toFixed(2) || '0.00'} ₽
                    </Typography>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Фильтры для продаж */}
        <Paper sx={{ p: 2, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Отчет по продажам
          </Typography>
          <Stack direction="row" spacing={2} alignItems="center">
            <DatePicker
              label="Начало периода"
              value={startDate}
              onChange={setStartDate}
              slotProps={{ textField: { size: 'small' } }}
            />
            <DatePicker
              label="Конец периода"
              value={endDate}
              onChange={setEndDate}
              slotProps={{ textField: { size: 'small' } }}
            />
            <Button variant="outlined" onClick={loadSales}>
              Обновить
            </Button>
          </Stack>
          {sales && sales.startDate && sales.endDate && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              Период: {new Date(sales.startDate).toLocaleDateString('ru-RU')} - {new Date(sales.endDate).toLocaleDateString('ru-RU')}
            </Typography>
          )}
        </Paper>

        {/* Заявки */}
        <Paper>
          <Box sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Мои заявки
            </Typography>
            <FormControl size="small" sx={{ minWidth: 200, mb: 2 }}>
              <InputLabel>Фильтр по статусу</InputLabel>
              <Select
                value={statusFilter}
                label="Фильтр по статусу"
                onChange={(e) => {
                  setStatusFilter(e.target.value);
                  setPage(0);
                }}
              >
                <MenuItem value="">Все</MenuItem>
                <MenuItem value="NEW">Новые</MenuItem>
                <MenuItem value="IN_PROGRESS">В обработке</MenuItem>
                <MenuItem value="COMPLETED">Завершённые</MenuItem>
                <MenuItem value="CANCELLED">Отменённые</MenuItem>
              </Select>
            </FormControl>
          </Box>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Тур</TableCell>
                  <TableCell>Клиент</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Телефон</TableCell>
                  <TableCell>Дата</TableCell>
                  <TableCell>Статус</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      Загрузка...
                    </TableCell>
                  </TableRow>
                ) : requests.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      Нет заявок
                    </TableCell>
                  </TableRow>
                ) : (
                  requests.map((request) => (
                    <TableRow key={request.id}>
                      <TableCell>{request.tourName}</TableCell>
                      <TableCell>{request.userName}</TableCell>
                      <TableCell>{request.userEmail}</TableCell>
                      <TableCell>{request.userPhone || '—'}</TableCell>
                      <TableCell>
                        {request.createdAt ? formatDate(request.createdAt) : '—'}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={statusLabels[request.status || 'NEW']}
                          color={statusColors[request.status || 'NEW']}
                          size="small"
                        />
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      </Box>
    </LocalizationProvider>
  );
}

