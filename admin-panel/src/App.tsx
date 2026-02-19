import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import ToursPage from './pages/ToursPage';
import RequestsPage from './pages/RequestsPage';
import FlightsPage from './pages/FlightsPage';
import AnalyticsPage from './pages/AnalyticsPage';
import EmployeesPage from './pages/EmployeesPage';
import EmployeeDashboard from './pages/EmployeeDashboard';
import ClientsPage from './pages/ClientsPage';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route 
              path="dashboard" 
              element={
                <ProtectedRoute requireAdmin>
                  <Dashboard />
                </ProtectedRoute>
              } 
            />
            <Route path="analytics" element={<AnalyticsPage />} />
            <Route path="tours" element={<ToursPage />} />
            <Route path="requests" element={<RequestsPage />} />
            <Route path="flights" element={<FlightsPage />} />
            <Route 
              path="employees" 
              element={
                <ProtectedRoute requireAdmin>
                  <EmployeesPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="clients" 
              element={
                <ProtectedRoute requireAdmin>
                  <ClientsPage />
                </ProtectedRoute>
              } 
            />
            <Route path="employee-dashboard" element={<EmployeeDashboard />} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;

