import { Navigate } from 'react-router-dom';
import { authApi } from '../api/auth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAdmin?: boolean;
}

export default function ProtectedRoute({ children, requireAdmin = false }: ProtectedRouteProps) {
  const isAuth = authApi.isAuthenticated();
  const hasAdmin = authApi.hasRole('ROLE_ADMIN');
  
  console.log('=== ProtectedRoute: Проверка доступа', { isAuth, hasAdmin, requireAdmin });
  
  if (!isAuth) {
    console.log('=== ProtectedRoute: Не авторизован, редирект на /login');
    return <Navigate to="/login" replace />;
  }

  if (requireAdmin && !hasAdmin) {
    console.log('=== ProtectedRoute: Требуется админ, но нет роли, редирект на /dashboard');
    return <Navigate to="/dashboard" replace />;
  }

  console.log('=== ProtectedRoute: Доступ разрешён');
  return <>{children}</>;
}

