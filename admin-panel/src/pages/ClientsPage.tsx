import { useEffect, useState, useCallback } from 'react';
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
  Tooltip,
  InputAdornment,
  Alert,
  Snackbar,
  Avatar,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Star,
  StarBorder,
  Search,
  Person,
} from '@mui/icons-material';
import { clientsApi, ClientDto } from '../api/clientsApi';

const emptyForm: ClientDto = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  birthDate: '',
  notes: '',
  vipStatus: false,
  active: true,
};

export default function ClientsPage() {
  const [clients, setClients] = useState<ClientDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [vipFilter, setVipFilter] = useState<string>('all');

  // Dialog state
  const [showDialog, setShowDialog] = useState(false);
  const [editingClient, setEditingClient] = useState<ClientDto | null>(null);
  const [formData, setFormData] = useState<ClientDto>(emptyForm);
  const [formError, setFormError] = useState('');

  // Delete dialog
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [deletingClient, setDeletingClient] = useState<ClientDto | null>(null);

  // Snackbar
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  const loadClients = useCallback(async () => {
    try {
      setLoading(true);
      const vip = vipFilter === 'vip' ? true : vipFilter === 'regular' ? false : undefined;
      const data = await clientsApi.getAll(search || undefined, vip, page, 20);
      setClients(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (error) {
      console.error('Error loading clients:', error);
      showSnackbar('Ошибка при загрузке клиентов', 'error');
    } finally {
      setLoading(false);
    }
  }, [page, search, vipFilter]);

  useEffect(() => {
    loadClients();
  }, [loadClients]);

  const showSnackbar = (message: string, severity: 'success' | 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  // ─── Create / Edit ────────────────────────────────────────────────────────
  const handleCreate = () => {
    setEditingClient(null);
    setFormData(emptyForm);
    setFormError('');
    setShowDialog(true);
  };

  const handleEdit = (client: ClientDto) => {
    setEditingClient(client);
    setFormData({
      ...client,
      birthDate: client.birthDate ? client.birthDate.split('T')[0] : '',
    });
    setFormError('');
    setShowDialog(true);
  };

  const handleSave = async () => {
    if (!formData.firstName.trim() || !formData.lastName.trim() || !formData.email.trim()) {
      setFormError('Имя, фамилия и email обязательны');
      return;
    }
    try {
      if (editingClient) {
        await clientsApi.update(editingClient.id!, formData);
        showSnackbar('Клиент успешно обновлён', 'success');
      } else {
        await clientsApi.create(formData);
        showSnackbar('Клиент успешно создан', 'success');
      }
      setShowDialog(false);
      loadClients();
    } catch (error) {
      console.error('Error saving client:', error);
      setFormError('Ошибка при сохранении. Проверьте данные.');
    }
  };

  // ─── VIP toggle ──────────────────────────────────────────────────────────
  const handleToggleVip = async (client: ClientDto) => {
    try {
      await clientsApi.update(client.id!, { ...client, vipStatus: !client.vipStatus });
      showSnackbar(
        !client.vipStatus ? 'VIP-статус присвоен' : 'VIP-статус снят',
        'success'
      );
      loadClients();
    } catch (error) {
      console.error('Error toggling VIP:', error);
      showSnackbar('Ошибка при изменении VIP-статуса', 'error');
    }
  };

  // ─── Delete ───────────────────────────────────────────────────────────────
  const handleDeleteClick = (client: ClientDto) => {
    setDeletingClient(client);
    setShowDeleteDialog(true);
  };

  const handleDeleteConfirm = async () => {
    if (!deletingClient) return;
    try {
      await clientsApi.delete(deletingClient.id!);
      showSnackbar('Клиент удалён', 'success');
      setShowDeleteDialog(false);
      loadClients();
    } catch (error) {
      console.error('Error deleting client:', error);
      showSnackbar('Ошибка при удалении клиента', 'error');
    }
  };

  // ─── Search ───────────────────────────────────────────────────────────────
  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSearch(searchInput);
    setPage(0);
  };

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4">Клиенты</Typography>
          <Typography variant="body2" color="text.secondary">
            Всего: {totalElements}
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />} onClick={handleCreate}>
          Добавить клиента
        </Button>
      </Box>

      {/* Filters */}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 3 }}>
        <Box component="form" onSubmit={handleSearchSubmit} sx={{ display: 'flex', gap: 1, flexGrow: 1 }}>
          <TextField
            size="small"
            placeholder="Поиск по имени, фамилии или email..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            sx={{ flexGrow: 1 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Search fontSize="small" />
                </InputAdornment>
              ),
            }}
          />
          <Button type="submit" variant="outlined" size="small">
            Найти
          </Button>
          {search && (
            <Button
              size="small"
              onClick={() => { setSearch(''); setSearchInput(''); setPage(0); }}
            >
              Сбросить
            </Button>
          )}
        </Box>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>VIP-статус</InputLabel>
          <Select
            value={vipFilter}
            label="VIP-статус"
            onChange={(e) => { setVipFilter(e.target.value); setPage(0); }}
          >
            <MenuItem value="all">Все клиенты</MenuItem>
            <MenuItem value="vip">Только VIP</MenuItem>
            <MenuItem value="regular">Обычные</MenuItem>
          </Select>
        </FormControl>
      </Stack>

      {/* Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Клиент</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Телефон</TableCell>
              <TableCell>Дата рождения</TableCell>
              <TableCell align="center">VIP</TableCell>
              <TableCell align="center">Статус</TableCell>
              <TableCell align="center">Заявок</TableCell>
              <TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} align="center">Загрузка...</TableCell>
              </TableRow>
            ) : clients.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Stack alignItems="center" spacing={1} py={3}>
                    <Person sx={{ fontSize: 48, color: 'text.disabled' }} />
                    <Typography color="text.secondary">Клиенты не найдены</Typography>
                  </Stack>
                </TableCell>
              </TableRow>
            ) : (
              clients.map((client) => (
                <TableRow key={client.id} hover>
                  <TableCell>
                    <Stack direction="row" spacing={1.5} alignItems="center">
                      <Avatar sx={{ width: 34, height: 34, bgcolor: client.vipStatus ? 'warning.main' : 'primary.main', fontSize: 14 }}>
                        {client.firstName?.[0]}{client.lastName?.[0]}
                      </Avatar>
                      <Box>
                        <Typography variant="body2" fontWeight={500}>
                          {client.firstName} {client.lastName}
                        </Typography>
                        {client.notes && (
                          <Typography variant="caption" color="text.secondary" noWrap sx={{ maxWidth: 180, display: 'block' }}>
                            {client.notes}
                          </Typography>
                        )}
                      </Box>
                    </Stack>
                  </TableCell>
                  <TableCell>{client.email}</TableCell>
                  <TableCell>{client.phone || '—'}</TableCell>
                  <TableCell>
                    {client.birthDate
                      ? new Date(client.birthDate).toLocaleDateString('ru-RU')
                      : '—'}
                  </TableCell>
                  <TableCell align="center">
                    <Tooltip title={client.vipStatus ? 'Снять VIP-статус' : 'Присвоить VIP-статус'}>
                      <IconButton
                        size="small"
                        onClick={() => handleToggleVip(client)}
                        color={client.vipStatus ? 'warning' : 'default'}
                      >
                        {client.vipStatus ? <Star /> : <StarBorder />}
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                  <TableCell align="center">
                    <Chip
                      label={client.active ? 'Активен' : 'Неактивен'}
                      color={client.active ? 'success' : 'default'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="center">
                    <Chip
                      label={client.totalRequests ?? 0}
                      variant="outlined"
                      size="small"
                      color={client.totalRequests && client.totalRequests > 0 ? 'primary' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Редактировать">
                      <IconButton size="small" onClick={() => handleEdit(client)}>
                        <Edit />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Удалить">
                      <IconButton size="small" color="error" onClick={() => handleDeleteClick(client)}>
                        <Delete />
                      </IconButton>
                    </Tooltip>
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

      {/* ─── Create / Edit Dialog ─────────────────────────────────────────── */}
      <Dialog open={showDialog} onClose={() => setShowDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingClient ? 'Редактировать клиента' : 'Добавить клиента'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {formError && <Alert severity="error">{formError}</Alert>}
            <Stack direction="row" spacing={2}>
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
            </Stack>
            <TextField
              label="Email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              required
              fullWidth
            />
            <TextField
              label="Телефон"
              value={formData.phone || ''}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              fullWidth
            />
            <TextField
              label="Дата рождения"
              type="date"
              value={formData.birthDate || ''}
              onChange={(e) => setFormData({ ...formData, birthDate: e.target.value })}
              fullWidth
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="Заметки"
              value={formData.notes || ''}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              fullWidth
              multiline
              rows={3}
            />
            <Stack direction="row" spacing={3}>
              <FormControlLabel
                control={
                  <Switch
                    checked={formData.vipStatus ?? false}
                    onChange={(e) => setFormData({ ...formData, vipStatus: e.target.checked })}
                    color="warning"
                  />
                }
                label={
                  <Stack direction="row" spacing={0.5} alignItems="center">
                    <Star fontSize="small" color={formData.vipStatus ? 'warning' : 'disabled'} />
                    <span>VIP-статус</span>
                  </Stack>
                }
              />
              <FormControlLabel
                control={
                  <Switch
                    checked={formData.active ?? true}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                  />
                }
                label="Активен"
              />
            </Stack>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDialog(false)}>Отмена</Button>
          <Button onClick={handleSave} variant="contained">
            Сохранить
          </Button>
        </DialogActions>
      </Dialog>

      {/* ─── Delete Confirm Dialog ────────────────────────────────────────── */}
      <Dialog open={showDeleteDialog} onClose={() => setShowDeleteDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Удалить клиента?</DialogTitle>
        <DialogContent>
          <Typography>
            Вы уверены, что хотите удалить клиента{' '}
            <strong>{deletingClient?.firstName} {deletingClient?.lastName}</strong>?
            Это действие необратимо.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteDialog(false)}>Отмена</Button>
          <Button onClick={handleDeleteConfirm} variant="contained" color="error">
            Удалить
          </Button>
        </DialogActions>
      </Dialog>

      {/* ─── Snackbar ─────────────────────────────────────────────────────── */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          variant="filled"
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
