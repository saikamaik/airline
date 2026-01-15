import apiClient from './client';
import { EmployeeDto, EmployeeSalesDto } from './employees';
import { ClientRequestDto, Page } from './requests';

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

  getMySales: async (startDate?: string, endDate?: string): Promise<EmployeeSalesDto> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await apiClient.get(`/employee/sales?${params.toString()}`);
    return response.data;
  },
};

