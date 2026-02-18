import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Chip,
  Stack,
  Pagination,
  FormControlLabel,
  Switch,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
} from '@mui/material';
import {
  Add,
  Edit,
  Visibility,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ru } from 'date-fns/locale';
import { employeesApi, EmployeeDto, EmployeeSalesDto } from '../api/employees';

export default function EmployeesPage() {
  const [employees, setEmployees] = useState<EmployeeDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showDialog, setShowDialog] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<EmployeeDto | null>(null);
  const [showSalesDialog, setShowSalesDialog] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState<EmployeeDto | null>(null);
  const [sales, setSales] = useState<EmployeeSalesDto | null>(null);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
  const [activeFilter, setActiveFilter] = useState<boolean | undefined>(undefined);

  const [formData, setFormData] = useState<EmployeeDto>({
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    active: true,
  });

  useEffect(() => {
    loadEmployees();
  }, [page, activeFilter]);

  const loadEmployees = async () => {
    try {
      setLoading(true);
      const data = await employeesApi.getAll(activeFilter, page, 20);
      setEmployees(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Error loading employees:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingEmployee(null);
    setFormData({
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      active: true,
    });
    setShowDialog(true);
  };

  const handleEdit = (employee: EmployeeDto) => {
    setEditingEmployee(employee);
    setFormData({
      ...employee,
      password: '', // Не показываем пароль при редактировании
    });
    setShowDialog(true);
  };

  const handleSave = async () => {
    try {
      if (editingEmployee) {
        await employeesApi.update(editingEmployee.id!, formData);
      } else {
        await employeesApi.create(formData);
      }
      setShowDialog(false);
      loadEmployees();
    } catch (error) {
      console.error('Error saving employee:', error);
      alert('Ошибка при сохранении сотрудника');
    }
  };

  const handleViewSales = async (employee: EmployeeDto) => {
    setSelectedEmployee(employee);
    setStartDate(null);
    setEndDate(null);
    await loadSales(employee.id!);
    setShowSalesDialog(true);
  };

  const loadSales = async (employeeId: number) => {
    try {
      const start = startDate ? startDate.toISOString().split('T')[0] : undefined;
      const end = endDate ? endDate.toISOString().split('T')[0] : undefined;
      const salesData = await employeesApi.getSales(employeeId, start, end);
      setSales(salesData);
    } catch (error) {
      console.error('Error loading sales:', error);
    }
  };

  const handleSalesDateChange = async () => {
    if (selectedEmployee) {
      await loadSales(selectedEmployee.id!);
    }
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ru}>
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4">Сотрудники</Typography>
          <Stack direction="row" spacing={2}>
            <FormControl size="small" sx={{ minWidth: 150 }}>
              <InputLabel>Статус</InputLabel>
              <Select
                value={activeFilter === undefined ? 'all' : activeFilter ? 'active' : 'inactive'}
                label="Статус"
                onChange={(e) => {
                  const value = e.target.value;
                  setActiveFilter(value === 'all' ? undefined : value === 'active');
                  setPage(0);
                }}
              >
                <MenuItem value="all">Все</MenuItem>
                <MenuItem value="active">Активные</MenuItem>
                <MenuItem value="inactive">Неактивные</MenuItem>
              </Select>
            </FormControl>
            <Button variant="contained" startIcon={<Add />} onClick={handleCreate}>
              Добавить сотрудника
            </Button>
          </Stack>
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Имя</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Телефон</TableCell>
                <TableCell>Дата найма</TableCell>
                <TableCell>Статус</TableCell>
                <TableCell align="right">Действия</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    Загрузка...
                  </TableCell>
                </TableRow>
              ) : employees.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    Нет сотрудников
                  </TableCell>
                </TableRow>
              ) : (
                employees.map((employee) => (
                  <TableRow key={employee.id}>
                    <TableCell>{employee.firstName} {employee.lastName}</TableCell>
                    <TableCell>{employee.email}</TableCell>
                    <TableCell>{employee.phone || '-'}</TableCell>
                    <TableCell>
                      {employee.hireDate ? new Date(employee.hireDate).toLocaleDateString('ru-RU') : '-'}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={employee.active ? 'Активен' : 'Неактивен'}
                        color={employee.active ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        size="small"
                        onClick={() => handleViewSales(employee)}
                        title="Просмотр продаж"
                      >
                        <Visibility />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleEdit(employee)}
                        title="Редактировать"
                      >
                        <Edit />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {totalPages > 1 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
            <Pagination
              count={totalPages}
              page={page + 1}
              onChange={(_, value) => setPage(value - 1)}
            />
          </Box>
        )}

        {/* Диалог создания/редактирования сотрудника */}
        <Dialog open={showDialog} onClose={() => setShowDialog(false)} maxWidth="sm" fullWidth>
          <DialogTitle>
            {editingEmployee ? 'Редактировать сотрудника' : 'Добавить сотрудника'}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Имя пользователя"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                required
                disabled={!!editingEmployee}
                fullWidth
              />
              <TextField
                label="Пароль"
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required={!editingEmployee}
                fullWidth
              />
              <TextField
                label="Имя"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                required
                fullWidth
              />
              <TextField
                label="Фамилия"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                required
                fullWidth
              />
              <TextField
                label="Email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                required
                disabled={!!editingEmployee}
                fullWidth
              />
              <TextField
                label="Телефон"
                value={formData.phone || ''}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                fullWidth
              />
              {editingEmployee && (
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.active ?? true}
                      onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    />
                  }
                  label="Активен"
                />
              )}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowDialog(false)}>Отмена</Button>
            <Button onClick={handleSave} variant="contained">
              Сохранить
            </Button>
          </DialogActions>
        </Dialog>

        {/* Диалог просмотра продаж */}
        <Dialog open={showSalesDialog} onClose={() => setShowSalesDialog(false)} maxWidth="md" fullWidth>
          <DialogTitle>
            Продажи сотрудника: {selectedEmployee?.firstName} {selectedEmployee?.lastName}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 2 }}>
              <Stack direction="row" spacing={2}>
                <DatePicker
                  label="Начало периода"
                  value={startDate}
                  onChange={(newValue) => {
                    setStartDate(newValue);
                  }}
                  slotProps={{ textField: { size: 'small', fullWidth: true } }}
                />
                <DatePicker
                  label="Конец периода"
                  value={endDate}
                  onChange={(newValue) => {
                    setEndDate(newValue);
                  }}
                  slotProps={{ textField: { size: 'small', fullWidth: true } }}
                />
                <Button variant="outlined" onClick={handleSalesDateChange} sx={{ mt: 1 }}>
                  Применить
                </Button>
              </Stack>
              
              {sales && (
                <Box>
                  <Typography variant="h6" gutterBottom>Статистика продаж</Typography>
                  <Typography>Всего продаж: <strong>{sales.totalSales}</strong></Typography>
                  <Typography>Общая выручка: <strong>{sales.totalRevenue.toFixed(2)} ₽</strong></Typography>
                  {sales.startDate && sales.endDate && (
                    <Typography variant="caption" color="text.secondary">
                      Период: {new Date(sales.startDate).toLocaleDateString('ru-RU')} - {new Date(sales.endDate).toLocaleDateString('ru-RU')}
                    </Typography>
                  )}
                </Box>
              )}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowSalesDialog(false)}>Закрыть</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </LocalizationProvider>
  );
}

