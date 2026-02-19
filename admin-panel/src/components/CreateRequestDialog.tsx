import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Stack,
  Alert,
  Autocomplete,
  Typography,
  Box,
} from '@mui/material';
import { Add } from '@mui/icons-material';

export interface NewRequestFormData {
  tourId: number;
  userName: string;
  userEmail: string;
  userPhone: string;
  priority: string;
  comment: string;
}

export interface TourOption {
  id: number;
  name: string;
  price: number;
  destinationCity: string;
}

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess: (message: string) => void;
  tours: TourOption[];
  toursLoading?: boolean;
  onSubmit: (data: NewRequestFormData) => Promise<void>;
  /** Показывать поле «Приоритет» (по умолч. true) */
  showPriority?: boolean;
  title?: string;
}

const emptyForm: NewRequestFormData = {
  tourId: 0,
  userName: '',
  userEmail: '',
  userPhone: '',
  priority: 'NORMAL',
  comment: '',
};

const priorityLabels: Record<string, string> = {
  NORMAL: 'Обычный',
  HIGH: 'Высокий',
  URGENT: 'Срочный',
};

export default function CreateRequestDialog({
  open,
  onClose,
  onSuccess,
  tours,
  toursLoading = false,
  onSubmit,
  showPriority = true,
  title = 'Создать заявку вручную',
}: Props) {
  const [form, setForm] = useState<NewRequestFormData>(emptyForm);
  const [selectedTour, setSelectedTour] = useState<TourOption | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleClose = () => {
    setForm(emptyForm);
    setSelectedTour(null);
    setError('');
    onClose();
  };

  const handleSubmit = async () => {
    if (!form.tourId) {
      setError('Выберите тур');
      return;
    }
    if (!form.userName.trim()) {
      setError('Введите имя клиента');
      return;
    }
    if (!form.userEmail.trim()) {
      setError('Введите email клиента');
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.userEmail)) {
      setError('Некорректный email');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await onSubmit(form);
      setForm(emptyForm);
      setSelectedTour(null);
      onSuccess('Заявка успешно создана');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при создании заявки');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}

          {/* Тур */}
          <Autocomplete
            options={tours}
            loading={toursLoading}
            getOptionLabel={(t) =>
              `${t.name} — ${t.destinationCity} (${t.price.toLocaleString('ru-RU')} ₽)`
            }
            value={selectedTour}
            onChange={(_, value) => {
              setSelectedTour(value);
              setForm({ ...form, tourId: value?.id ?? 0 });
            }}
            renderOption={(props, t) => (
              <Box component="li" {...props}>
                <Box>
                  <Typography variant="body2" fontWeight={500}>{t.name}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {t.destinationCity} · {t.price.toLocaleString('ru-RU')} ₽
                  </Typography>
                </Box>
              </Box>
            )}
            renderInput={(params) => (
              <TextField {...params} label="Тур *" size="small" />
            )}
          />

          {/* Данные клиента */}
          <Stack direction="row" spacing={2}>
            <TextField
              label="Имя клиента *"
              value={form.userName}
              onChange={(e) => setForm({ ...form, userName: e.target.value })}
              size="small"
              fullWidth
            />
          </Stack>

          <TextField
            label="Email клиента *"
            type="email"
            value={form.userEmail}
            onChange={(e) => setForm({ ...form, userEmail: e.target.value })}
            size="small"
            fullWidth
          />

          <TextField
            label="Телефон клиента"
            value={form.userPhone}
            onChange={(e) => setForm({ ...form, userPhone: e.target.value })}
            size="small"
            fullWidth
            placeholder="+7 (999) 000-00-00"
          />

          {/* Приоритет */}
          {showPriority && (
            <FormControl size="small" fullWidth>
              <InputLabel>Приоритет</InputLabel>
              <Select
                value={form.priority}
                label="Приоритет"
                onChange={(e) => setForm({ ...form, priority: e.target.value })}
              >
                {Object.entries(priorityLabels).map(([value, label]) => (
                  <MenuItem key={value} value={value}>{label}</MenuItem>
                ))}
              </Select>
            </FormControl>
          )}

          {/* Комментарий */}
          <TextField
            label="Комментарий"
            value={form.comment}
            onChange={(e) => setForm({ ...form, comment: e.target.value })}
            multiline
            rows={3}
            size="small"
            fullWidth
            placeholder="Дополнительные пожелания клиента..."
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>
          Отмена
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading}
          startIcon={<Add />}
        >
          {loading ? 'Создание...' : 'Создать заявку'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
