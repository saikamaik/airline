import { useState } from 'react';
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
  TextField,
  Grid,
  TablePagination,
} from '@mui/material';
import { Search } from '@mui/icons-material';
import { flightsApi } from '../api/flights';
import { FlightDto, Page } from '../types';

export default function FlightsPage() {
  const [flights, setFlights] = useState<FlightDto[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [loading, setLoading] = useState(false);
  const [departure, setDeparture] = useState('');
  const [arrival, setArrival] = useState('');

  const handleSearch = async (pageNum = 1) => {
    if (!departure || !arrival) {
      alert('Введите коды аэропортов');
      return;
    }

    setLoading(true);
    try {
      const response = await flightsApi.search(departure, arrival, pageNum);
      setFlights(response.content);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Error searching flights:', error);
      alert('Ошибка поиска рейсов');
    } finally {
      setLoading(false);
    }
  };

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
    handleSearch(newPage + 1);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
    handleSearch(1);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ru-RU');
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Рейсы
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Аэропорт вылета (код)"
              value={departure}
              onChange={(e) => setDeparture(e.target.value.toUpperCase())}
              placeholder="SVO"
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Аэропорт прилёта (код)"
              value={arrival}
              onChange={(e) => setArrival(e.target.value.toUpperCase())}
              placeholder="LED"
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Button
              fullWidth
              variant="contained"
              startIcon={<Search />}
              onClick={() => {
                setPage(0);
                handleSearch(1);
              }}
              disabled={loading}
            >
              {loading ? 'Поиск...' : 'Найти'}
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {flights.length > 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Рейс</TableCell>
                <TableCell>Вылет</TableCell>
                <TableCell>Прилёт</TableCell>
                <TableCell>Плановое время вылета</TableCell>
                <TableCell>Плановое время прилёта</TableCell>
                <TableCell>Статус</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {flights.map((flight, index) => (
                <TableRow key={index}>
                  <TableCell>{flight.flightNo}</TableCell>
                  <TableCell>{flight.departureAirportCode}</TableCell>
                  <TableCell>{flight.arrivalAirportCode}</TableCell>
                  <TableCell>{formatDate(flight.scheduledDeparture)}</TableCell>
                  <TableCell>{formatDate(flight.scheduledArrival)}</TableCell>
                  <TableCell>{flight.status}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {flights.length > 0 && (
            <TablePagination
              component="div"
              count={totalElements}
              page={page}
              onPageChange={handleChangePage}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={handleChangeRowsPerPage}
              labelRowsPerPage="Строк на странице:"
              rowsPerPageOptions={[5, 10, 20]}
            />
          )}
        </TableContainer>
      )}

      {!loading && flights.length === 0 && (
        <Paper sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">
            Введите коды аэропортов и нажмите "Найти"
          </Typography>
        </Paper>
      )}
    </Box>
  );
}

