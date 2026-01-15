import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  TablePagination,
  FormControlLabel,
  Checkbox,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Stack,
} from '@mui/material';
import { Edit, Delete, Add, FlightTakeoff, Close } from '@mui/icons-material';
import { toursApi } from '../api/tours';
import { flightsApi } from '../api/flights';
import { TourDto, FlightDto } from '../types';

export default function ToursPage() {
  const [tours, setTours] = useState<TourDto[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingTour, setEditingTour] = useState<TourDto | null>(null);
  const [availableFlights, setAvailableFlights] = useState<FlightDto[]>([]);
  const [searchFlightNo, setSearchFlightNo] = useState('');

  useEffect(() => {
    loadTours();
    loadFlights();
  }, [page, rowsPerPage]);

  const loadFlights = async () => {
    try {
      const response = await flightsApi.getAll(0, 100);
      setAvailableFlights(response.content || []);
    } catch (error) {
      console.error('Error loading flights:', error);
    }
  };

  const loadTours = async () => {
    setLoading(true);
    try {
      console.log('=== ToursPage: Загрузка туров, страница:', page, 'размер:', rowsPerPage);
      const response = await toursApi.getAll(page, rowsPerPage);
      console.log('=== ToursPage: Получен ответ:', response);
      setTours(response.content || []);
      setTotalElements(response.totalElements || 0);
    } catch (error) {
      console.error('=== ToursPage: Ошибка загрузки туров:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingTour({
      name: '',
      description: '',
      price: 0,
      durationDays: 1,
      imageUrl: '',
      destinationCity: '',
      active: true,
      flightIds: [],
    });
    setDialogOpen(true);
  };

  const handleAddFlight = (flightNo: string) => {
    if (!editingTour) return;
    
    const flight = availableFlights.find(f => f.flightNo === flightNo);
    if (!flight || !flight.flightId) return;
    
    if (!editingTour.flightIds) {
      editingTour.flightIds = [];
    }
    
    if (!editingTour.flightIds.includes(flight.flightId)) {
      setEditingTour({
        ...editingTour,
        flightIds: [...editingTour.flightIds, flight.flightId]
      });
    }
    setSearchFlightNo('');
  };

  const handleRemoveFlight = (flightId: number) => {
    if (!editingTour || !editingTour.flightIds) return;
    
    setEditingTour({
      ...editingTour,
      flightIds: editingTour.flightIds.filter(id => id !== flightId)
    });
  };

  const getFlightById = (flightId: number) => {
    return availableFlights.find(f => f.flightId === flightId);
  };

  const handleEdit = (tour: TourDto) => {
    setEditingTour(tour);
    setDialogOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Вы уверены, что хотите удалить этот тур?')) {
      try {
        await toursApi.delete(id);
        loadTours();
      } catch (error) {
        console.error('Error deleting tour:', error);
      }
    }
  };

  const handleSave = async () => {
    if (!editingTour) return;

    try {
      if (editingTour.id) {
        await toursApi.update(editingTour.id, editingTour);
      } else {
        await toursApi.create(editingTour);
      }
      setDialogOpen(false);
      setEditingTour(null);
      loadTours();
    } catch (error) {
      console.error('Error saving tour:', error);
    }
  };

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Управление турами</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={handleCreate}>
          Добавить тур
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Название</TableCell>
              <TableCell>Направление</TableCell>
              <TableCell>Цена</TableCell>
              <TableCell>Дней</TableCell>
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
            ) : tours.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  Нет туров
                </TableCell>
              </TableRow>
            ) : (
              tours.map((tour) => (
                <TableRow key={tour.id}>
                  <TableCell>{tour.id}</TableCell>
                  <TableCell>{tour.name}</TableCell>
                  <TableCell>{tour.destinationCity}</TableCell>
                  <TableCell>{tour.price.toLocaleString('ru-RU')} ₽</TableCell>
                  <TableCell>{tour.durationDays}</TableCell>
                  <TableCell>
                    <Chip
                      label={tour.active ? 'Активен' : 'Неактивен'}
                      color={tour.active ? 'success' : 'default'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => handleEdit(tour)}>
                      <Edit />
                    </IconButton>
                    <IconButton size="small" onClick={() => handleDelete(tour.id!)}>
                      <Delete />
                    </IconButton>
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

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{editingTour?.id ? 'Редактировать тур' : 'Новый тур'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Название"
            value={editingTour?.name || ''}
            onChange={(e) => setEditingTour({ ...editingTour!, name: e.target.value })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Описание"
            value={editingTour?.description || ''}
            onChange={(e) => setEditingTour({ ...editingTour!, description: e.target.value })}
            margin="normal"
            multiline
            rows={4}
          />
          <TextField
            fullWidth
            label="Цена"
            type="number"
            value={editingTour?.price || 0}
            onChange={(e) => setEditingTour({ ...editingTour!, price: parseFloat(e.target.value) })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Длительность (дней)"
            type="number"
            value={editingTour?.durationDays || 1}
            onChange={(e) => setEditingTour({ ...editingTour!, durationDays: parseInt(e.target.value) })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Город назначения"
            value={editingTour?.destinationCity || ''}
            onChange={(e) => setEditingTour({ ...editingTour!, destinationCity: e.target.value })}
            margin="normal"
          />
          <TextField
            fullWidth
            label="URL изображения"
            value={editingTour?.imageUrl || ''}
            onChange={(e) => setEditingTour({ ...editingTour!, imageUrl: e.target.value })}
            margin="normal"
          />
          
          <FormControlLabel
            control={
              <Checkbox
                checked={editingTour?.active || false}
                onChange={(e) => setEditingTour({ ...editingTour!, active: e.target.checked })}
              />
            }
            label="Активный (отображать в каталоге)"
            sx={{ mt: 2 }}
          />

          <Divider sx={{ my: 3 }} />

          <Typography variant="h6" gutterBottom>
            Авиарейсы
          </Typography>

          {editingTour?.flightIds && editingTour.flightIds.length > 0 && (
            <List dense>
              {editingTour.flightIds.map((flightId) => {
                const flight = getFlightById(flightId);
                if (!flight) return null;
                
                return (
                  <ListItem key={flightId}>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <FlightTakeoff fontSize="small" />
                          {flight.flightNo} | {flight.departureAirportCode} → {flight.arrivalAirportCode}
                        </Box>
                      }
                      secondary={new Date(flight.scheduledDeparture).toLocaleString('ru-RU')}
                    />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => handleRemoveFlight(flightId)} size="small">
                        <Close />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                );
              })}
            </List>
          )}

          <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
            <FormControl fullWidth size="small">
              <InputLabel>Добавить рейс</InputLabel>
              <Select
                value={searchFlightNo}
                label="Добавить рейс"
                onChange={(e) => setSearchFlightNo(e.target.value)}
              >
                <MenuItem value="">Выберите рейс</MenuItem>
                {availableFlights.map((flight) => (
                  <MenuItem key={flight.flightNo} value={flight.flightNo}>
                    {flight.flightNo} | {flight.departureAirportCode} → {flight.arrivalAirportCode} | {new Date(flight.scheduledDeparture).toLocaleDateString('ru-RU')}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Button
              variant="outlined"
              onClick={() => handleAddFlight(searchFlightNo)}
              disabled={!searchFlightNo}
              startIcon={<Add />}
            >
              Добавить
            </Button>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Отмена</Button>
          <Button onClick={handleSave} variant="contained">
            Сохранить
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

