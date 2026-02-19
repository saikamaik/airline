import apiClient from './client';
import { ClientRequestDto, Page } from '../types';

export const requestsApi = {
  getAll: async (
    page = 0, 
    size = 20, 
    status?: string, 
    priority?: string,
    startDate?: string,
    endDate?: string
  ): Promise<Page<ClientRequestDto>> => {
    const params: any = { page, size };
    if (status) params.status = status;
    if (priority) params.priority = priority;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    
    const response = await apiClient.get<Page<ClientRequestDto>>('/admin/requests', { params });
    return response.data;
  },

  getById: async (id: number): Promise<ClientRequestDto> => {
    const response = await apiClient.get<ClientRequestDto>(`/admin/requests/${id}`);
    return response.data;
  },

  updateStatus: async (id: number, status: string, employeeId?: number): Promise<ClientRequestDto> => {
    const params: any = { status };
    if (employeeId) params.employeeId = employeeId;
    
    const response = await apiClient.patch<ClientRequestDto>(
      `/admin/requests/${id}/status`,
      null,
      { params }
    );
    return response.data;
  },

  create: async (data: {
    tourId: number;
    userName: string;
    userEmail: string;
    userPhone?: string;
    priority?: string;
    comment?: string;
  }): Promise<ClientRequestDto> => {
    const response = await apiClient.post<ClientRequestDto>('/admin/requests', data);
    return response.data;
  },

  getByTour: async (tourId: number, page = 0, size = 20): Promise<Page<ClientRequestDto>> => {
    const response = await apiClient.get<Page<ClientRequestDto>>(
      `/admin/requests/tour/${tourId}`,
      { params: { page, size } }
    );
    return response.data;
  },
};

