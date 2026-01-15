import apiClient from './client';
import { AuthRequest, AuthResponse } from '../types';

export const authApi = {
  login: async (credentials: AuthRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    localStorage.removeItem('roles');
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('jwt_token');
  },

  hasRole: (role: string): boolean => {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    return roles.includes(role);
  },

  getCurrentUser: (): string | null => {
    return localStorage.getItem('username');
  }
};

