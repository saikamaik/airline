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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Tabs,
  Tab,
  IconButton,
  Alert,
  Snackbar,
} from '@mui/material';
import {
  TrendingUp,
  Assignment,
  Person,
  CheckCircle,
  Visibility,
  Close,
  Add,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ru } from 'date-fns/locale';
import { employeeApi } from '../api/employeeApi';
import { EmployeeDto, EmployeeSalesDto } from '../api/employees';
import { ClientRequestDto } from '../types';
import CreateRequestDialog, { TourOption, NewRequestFormData } from '../components/CreateRequestDialog';

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
  const [availableRequests, setAvailableRequests] = useState<ClientRequestDto[]>([]);
  const [sales, setSales] = useState<EmployeeSalesDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const [selectedRequest, setSelectedRequest] = useState<ClientRequestDto | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [newStatus, setNewStatus] = useState('');
  const [comment, setComment] = useState('');
  const [comments, setComments] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [availableTours, setAvailableTours] = useState<TourOption[]>([]);
  const [toursLoading, setToursLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, [page, statusFilter, tabValue]);

  useEffect(() => {
    loadTours();
  }, []);

  useEffect(() => {
    if (startDate && endDate) {
      loadSales();
    }
  }, [startDate, endDate]);

  const loadData = async () => {
    try {
      setLoading(true);
      if (tabValue === 0) {
        // Доступные заявки
        const [profileData, availableData] = await Promise.all([
          employeeApi.getProfile(),
          employeeApi.getAvailableRequests(statusFilter || undefined, page, 20),
        ]);
        setProfile(profileData);
        setAvailableRequests(availableData.content);
      } else {
        // Мои заявки
        const [profileData, requestsData] = await Promise.all([
          employeeApi.getProfile(),
          employeeApi.getMyRequests(statusFilter || undefined, page, 20),
        ]);
        setProfile(profileData);
        setRequests(requestsData.content);
      }
    } catch (error) {
      console.error('Error loading data:', error);
      setError('Ошибка при загрузке данных');
    } finally {
      setLoading(false);
    }
  };

  const loadAllRequests = async () => {
    try {
      // Загружаем обе вкладки одновременно
      const [profileData, availableData, requestsData] = await Promise.all([
        employeeApi.getProfile(),
        employeeApi.getAvailableRequests(statusFilter || undefined, page, 20),
        employeeApi.getMyRequests(statusFilter || undefined, page, 20),
      ]);
      setProfile(profileData);
      setAvailableRequests(availableData.content);
      setRequests(requestsData.content);
    } catch (error) {
      console.error('Error loading all requests:', error);
      setError('Ошибка при загрузке данных');
    }
  };

  const loadTours = async () => {
    setToursLoading(true);
    try {
      const tours = await employeeApi.getActiveTours();
      setAvailableTours(tours);
    } catch (err) {
      console.error('Error loading tours:', err);
    } finally {
      setToursLoading(false);
    }
  };

  const handleCreateRequest = async (formData: NewRequestFormData) => {
    await employeeApi.createRequest({
      tourId: formData.tourId,
      userName: formData.userName,
      userEmail: formData.userEmail,
      userPhone: formData.userPhone || undefined,
      priority: formData.priority,
      comment: formData.comment || undefined,
    });
    setCreateDialogOpen(false);
    setSuccess('Заявка успешно создана');
    await loadAllRequests();
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

  const handleTakeRequest = async (requestId: number) => {
    try {
      await employeeApi.takeRequest(requestId);
      setSuccess('Заявка успешно взята в работу');
      
      // Загружаем обе вкладки
      await loadAllRequests();
      
      // Переключаемся на вкладку "Мои заявки"
      setTabValue(1);
    } catch (error) {
      console.error('Error taking request:', error);
      setError('Ошибка при взятии заявки');
    }
  };

  const handleViewRequest = async (request: ClientRequestDto) => {
    if (!request.id) {
      setError('Некорректная заявка');
      return;
    }
    
    setSelectedRequest(request);
    setNewStatus(request.status || 'NEW');
    setDialogOpen(true);
    
    // Загружаем комментарии
    try {
      const commentsData = await employeeApi.getComments(request.id);
      setComments(commentsData);
    } catch (error) {
      console.error('Error loading comments:', error);
    }
  };

  const handleUpdateStatus = async () => {
    if (!selectedRequest || !selectedRequest.id) return;
    
    try {
      await employeeApi.updateRequestStatus(selectedRequest.id, newStatus);
      setSuccess('Статус успешно обновлен');
      setDialogOpen(false);
      
      // Загружаем обе вкладки для актуальности данных
      await loadAllRequests();
    } catch (error: any) {
      console.error('Error updating status:', error);
      setError(error.response?.data?.message || 'Ошибка при обновлении статуса');
    }
  };

  const handleAddComment = async () => {
    if (!selectedRequest || !selectedRequest.id || !comment.trim()) return;
    
    try {
      await employeeApi.addComment(selectedRequest.id, comment, false);
      setComment('');
      setSuccess('Комментарий добавлен');
      
      // Перезагружаем комментарии
      const commentsData = await employeeApi.getComments(selectedRequest.id);
      setComments(commentsData);
    } catch (error) {
      console.error('Error adding comment:', error);
      setError('Ошибка при добавлении комментария');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ru-RU');
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ru}>
      <Box>
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="h4">
            Панель сотрудника
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setCreateDialogOpen(true)}
          >
            Создать заявку
          </Button>
        </Stack>

        {/* Сообщения об ошибках и успехе */}
        {error && (
          <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        {success && (
          <Alert severity="success" onClose={() => setSuccess(null)} sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

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
          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
            <Tab label="Доступные заявки" />
            <Tab label="Мои заявки" />
          </Tabs>

          <Box sx={{ p: 2 }}>
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
                  <TableCell>Действия</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      Загрузка...
                    </TableCell>
                  </TableRow>
                ) : tabValue === 0 ? (
                  availableRequests.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} align="center">
                        Нет доступных заявок
                      </TableCell>
                    </TableRow>
                  ) : (
                    availableRequests.map((request) => (
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
                        <TableCell>
                          <Stack direction="row" spacing={1}>
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() => handleViewRequest(request)}
                              title="Просмотр"
                            >
                              <Visibility />
                            </IconButton>
                            <IconButton
                              size="small"
                              color="success"
                              onClick={() => request.id && handleTakeRequest(request.id)}
                              disabled={!request.id}
                              title="Взять в работу"
                            >
                              <CheckCircle />
                            </IconButton>
                          </Stack>
                        </TableCell>
                      </TableRow>
                    ))
                  )
                ) : (
                  requests.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} align="center">
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
                        <TableCell>
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => handleViewRequest(request)}
                            title="Просмотр и редактирование"
                          >
                            <Visibility />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        {/* Диалог создания новой заявки */}
        <CreateRequestDialog
          open={createDialogOpen}
          onClose={() => setCreateDialogOpen(false)}
          onSuccess={(message) => setSuccess(message)}
          tours={availableTours}
          toursLoading={toursLoading}
          onSubmit={handleCreateRequest}
          showPriority={false}
          title="Создать заявку вручную"
        />

        {/* Диалог для работы с заявкой */}
        <Dialog
          open={dialogOpen}
          onClose={() => setDialogOpen(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>
            <Stack direction="row" justifyContent="space-between" alignItems="center">
              <Typography variant="h6">Заявка #{selectedRequest?.id}</Typography>
              <IconButton onClick={() => setDialogOpen(false)}>
                <Close />
              </IconButton>
            </Stack>
          </DialogTitle>
          <DialogContent>
            {selectedRequest && (
              <Box>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Информация о заявке
                </Typography>
                <Stack spacing={2} sx={{ mb: 3 }}>
                  <Typography><strong>Тур:</strong> {selectedRequest.tourName}</Typography>
                  <Typography><strong>Клиент:</strong> {selectedRequest.userName}</Typography>
                  <Typography><strong>Email:</strong> {selectedRequest.userEmail}</Typography>
                  <Typography><strong>Телефон:</strong> {selectedRequest.userPhone || '—'}</Typography>
                  <Typography><strong>Дата создания:</strong> {selectedRequest.createdAt ? formatDate(selectedRequest.createdAt) : '—'}</Typography>
                  {selectedRequest.comment && (
                    <Typography><strong>Комментарий клиента:</strong> {selectedRequest.comment}</Typography>
                  )}
                </Stack>

                {/* Изменение статуса (только для моих заявок) */}
                {selectedRequest.employeeId && (
                  <Box sx={{ mb: 3 }}>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Изменить статус
                    </Typography>
                    <FormControl fullWidth size="small">
                      <InputLabel>Статус</InputLabel>
                      <Select
                        value={newStatus}
                        label="Статус"
                        onChange={(e) => setNewStatus(e.target.value)}
                      >
                        <MenuItem value="NEW">Новая</MenuItem>
                        <MenuItem value="IN_PROGRESS">В обработке</MenuItem>
                        <MenuItem value="COMPLETED">Завершена</MenuItem>
                        <MenuItem value="CANCELLED">Отменена</MenuItem>
                      </Select>
                    </FormControl>
                    <Button
                      variant="contained"
                      onClick={handleUpdateStatus}
                      sx={{ mt: 1 }}
                    >
                      Обновить статус
                    </Button>
                  </Box>
                )}

                {/* Комментарии */}
                <Box>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Комментарии
                  </Typography>
                  <Stack spacing={2} sx={{ mb: 2, maxHeight: 200, overflowY: 'auto' }}>
                    {comments.length === 0 ? (
                      <Typography variant="body2" color="text.secondary">
                        Нет комментариев
                      </Typography>
                    ) : (
                      comments.map((c, index) => (
                        <Paper key={index} sx={{ p: 2, bgcolor: 'grey.50' }}>
                          <Typography variant="body2">{c.comment}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            {c.createdAt ? formatDate(c.createdAt) : ''}
                            {c.employeeName && ` — ${c.employeeName}`}
                          </Typography>
                        </Paper>
                      ))
                    )}
                  </Stack>
                  <TextField
                    fullWidth
                    multiline
                    rows={3}
                    label="Добавить комментарий"
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    size="small"
                  />
                  <Button
                    variant="outlined"
                    onClick={handleAddComment}
                    sx={{ mt: 1 }}
                    disabled={!comment.trim()}
                  >
                    Добавить комментарий
                  </Button>
                </Box>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Закрыть</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
}

