import apiClient from './client';
import { EmployeeDto, EmployeeSalesDto } from './employees';
import { ClientRequestDto, Page } from '../types';

export const employeeApi = {
  getProfile: async (): Promise<EmployeeDto> => {
    const response = await apiClient.get('/employee/profile');
    return response.data;
  },

  getMyRequests: async (status?: string, page: number = 0, size: number = 20): Promise<Page<ClientRequestDto>> => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await apiClient.get(`/employee/requests?${params.toString()}`);
    return response.data;
  },

  getAvailableRequests: async (status?: string, page: number = 0, size: number = 20): Promise<Page<ClientRequestDto>> => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await apiClient.get(`/employee/requests/available?${params.toString()}`);
    return response.data;
  },

  takeRequest: async (requestId: number): Promise<ClientRequestDto> => {
    const response = await apiClient.patch(`/employee/requests/${requestId}/take`);
    return response.data;
  },

  updateRequestStatus: async (requestId: number, status: string): Promise<ClientRequestDto> => {
    const response = await apiClient.patch(`/employee/requests/${requestId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  addComment: async (requestId: number, comment: string, isInternal: boolean = false): Promise<any> => {
    const response = await apiClient.post(`/employee/requests/${requestId}/comments`, {
      comment,
      isInternal,
    });
    return response.data;
  },

  getComments: async (requestId: number, isInternal?: boolean): Promise<any[]> => {
    const params = new URLSearchParams();
    if (isInternal !== undefined) params.append('isInternal', isInternal.toString());
    
    const response = await apiClient.get(`/employee/requests/${requestId}/comments?${params.toString()}`);
    return response.data;
  },

  createRequest: async (data: {
    tourId: number;
    userName: string;
    userEmail: string;
    userPhone?: string;
    priority?: string;
    comment?: string;
  }): Promise<ClientRequestDto> => {
    const response = await apiClient.post<ClientRequestDto>('/employee/requests', data);
    return response.data;
  },

  getActiveTours: async (): Promise<Array<{ id: number; name: string; price: number; destinationCity: string }>> => {
    const response = await apiClient.get('/tours', { params: { page: 0, size: 200 } });
    return response.data.content ?? [];
  },

  getMySales: async (startDate?: string, endDate?: string): Promise<EmployeeSalesDto> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await apiClient.get(`/employee/sales?${params.toString()}`);
    return response.data;
  },
};

