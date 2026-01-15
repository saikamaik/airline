import { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  TablePagination,
  TextField,
  Stack,
  Button,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ru } from 'date-fns/locale';
import { requestsApi } from '../api/requests';
import { employeesApi } from '../api/employees';
import { ClientRequestDto } from '../types';
import { EmployeeDto } from '../api/employees';

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

const priorityLabels: Record<string, string> = {
  NORMAL: 'Обычный',
  HIGH: 'Высокий',
  URGENT: 'Срочный',
};

const priorityColors: Record<string, 'default' | 'warning' | 'error'> = {
  NORMAL: 'default',
  HIGH: 'warning',
  URGENT: 'error',
};

export default function RequestsPage() {
  const [requests, setRequests] = useState<ClientRequestDto[]>([]);
  const [employees, setEmployees] = useState<EmployeeDto[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filterStatus, setFilterStatus] = useState<string>('');
  const [filterPriority, setFilterPriority] = useState<string>('');
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

  useEffect(() => {
    loadRequests();
    loadEmployees();
  }, [page, rowsPerPage]);

  const loadEmployees = async () => {
    try {
      const data = await employeesApi.getAll(true, 0, 1000);
      setEmployees(data.content);
    } catch (error) {
      console.error('Error loading employees:', error);
    }
  };

  const loadRequests = async () => {
    setLoading(true);
    try {
      const start = startDate ? startDate.toISOString().split('T')[0] : undefined;
      const end = endDate ? endDate.toISOString().split('T')[0] : undefined;
      
      const response = await requestsApi.getAll(
        page,
        rowsPerPage,
        filterStatus || undefined,
        filterPriority || undefined,
        start,
        end
      );
      setRequests(response.content);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Error loading requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApplyFilters = () => {
    setPage(0);
    loadRequests();
  };

  const handleResetFilters = () => {
    setFilterStatus('');
    setFilterPriority('');
    setStartDate(null);
    setEndDate(null);
    setPage(0);
    setTimeout(() => loadRequests(), 0);
  };

  const handleStatusChange = async (id: number, newStatus: string, employeeId?: number) => {
    try {
      await requestsApi.updateStatus(id, newStatus, employeeId);
      loadRequests();
    } catch (error) {
      console.error('Error updating status:', error);
    }
  };

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ru-RU');
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ru}>
      <Box>
        <Typography variant="h4" gutterBottom>
          Заявки клиентов
        </Typography>

        <Paper sx={{ p: 2, mb: 2 }}>
          <Typography variant="subtitle1" gutterBottom>
            Фильтры
          </Typography>
          <Stack direction="row" spacing={2} flexWrap="wrap" alignItems="center">
            <FormControl size="small" sx={{ minWidth: 200 }}>
              <InputLabel>Статус</InputLabel>
              <Select
                value={filterStatus}
                label="Статус"
                onChange={(e) => setFilterStatus(e.target.value)}
              >
                <MenuItem value="">Все статусы</MenuItem>
                <MenuItem value="NEW">Новые</MenuItem>
                <MenuItem value="IN_PROGRESS">В обработке</MenuItem>
                <MenuItem value="COMPLETED">Завершённые</MenuItem>
                <MenuItem value="CANCELLED">Отменённые</MenuItem>
              </Select>
            </FormControl>

            <FormControl size="small" sx={{ minWidth: 200 }}>
              <InputLabel>Приоритет</InputLabel>
              <Select
                value={filterPriority}
                label="Приоритет"
                onChange={(e) => setFilterPriority(e.target.value)}
              >
                <MenuItem value="">Все приоритеты</MenuItem>
                <MenuItem value="NORMAL">Обычный</MenuItem>
                <MenuItem value="HIGH">Высокий</MenuItem>
                <MenuItem value="URGENT">Срочный</MenuItem>
              </Select>
            </FormControl>

            <DatePicker
              label="Дата с"
              value={startDate}
              onChange={(newValue) => setStartDate(newValue)}
              slotProps={{ textField: { size: 'small' } }}
            />

            <DatePicker
              label="Дата по"
              value={endDate}
              onChange={(newValue) => setEndDate(newValue)}
              slotProps={{ textField: { size: 'small' } }}
            />

            <Button
              variant="contained"
              onClick={handleApplyFilters}
              disabled={loading}
            >
              Применить
            </Button>

            <Button
              variant="outlined"
              onClick={handleResetFilters}
              disabled={loading}
            >
              Сбросить
            </Button>
          </Stack>
        </Paper>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Тур</TableCell>
                <TableCell>Клиент</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Телефон</TableCell>
                <TableCell>Сотрудник</TableCell>
                <TableCell>Дата</TableCell>
                <TableCell>Статус</TableCell>
                <TableCell>Приоритет</TableCell>
                <TableCell>Действия</TableCell>
              </TableRow>
            </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  Загрузка...
                </TableCell>
              </TableRow>
            ) : requests.length === 0 ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  Нет заявок
                </TableCell>
              </TableRow>
            ) : (
              requests.map((request) => (
                <TableRow key={request.id}>
                  <TableCell>{request.id}</TableCell>
                  <TableCell>{request.tourName}</TableCell>
                  <TableCell>{request.userName}</TableCell>
                  <TableCell>{request.userEmail}</TableCell>
                  <TableCell>{request.userPhone || '—'}</TableCell>
                  <TableCell>{request.employeeName || '—'}</TableCell>
                  <TableCell>{request.createdAt ? formatDate(request.createdAt) : '—'}</TableCell>
                  <TableCell>
                    <Chip
                      label={statusLabels[request.status || 'NEW']}
                      color={statusColors[request.status || 'NEW']}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={priorityLabels[request.priority || 'NORMAL']}
                      color={priorityColors[request.priority || 'NORMAL']}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1, flexDirection: 'column', minWidth: 200 }}>
                      <Select
                        size="small"
                        value={request.status || 'NEW'}
                        onChange={(e) => handleStatusChange(request.id!, e.target.value)}
                        fullWidth
                      >
                        <MenuItem value="NEW">Новая</MenuItem>
                        <MenuItem value="IN_PROGRESS">В обработке</MenuItem>
                        <MenuItem value="COMPLETED">Завершена</MenuItem>
                        <MenuItem value="CANCELLED">Отменена</MenuItem>
                      </Select>
                      {(request.status === 'IN_PROGRESS' || request.status === 'COMPLETED') && (
                        <Select
                          size="small"
                          value={request.employeeId?.toString() || ''}
                          displayEmpty
                          onChange={(e) => {
                            const employeeId = e.target.value ? parseInt(e.target.value) : undefined;
                            handleStatusChange(request.id!, request.status || 'NEW', employeeId);
                          }}
                          fullWidth
                        >
                          <MenuItem value="">
                            <em>Не назначен</em>
                          </MenuItem>
                          {employees.map((emp) => (
                            <MenuItem key={emp.id} value={emp.id!.toString()}>
                              {emp.firstName} {emp.lastName}
                            </MenuItem>
                          ))}
                        </Select>
                      )}
                    </Box>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={totalElements}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          labelRowsPerPage="Строк на странице:"
        />
        </TableContainer>
      </Box>
    </LocalizationProvider>
  );
}

