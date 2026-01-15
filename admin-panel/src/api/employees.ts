import apiClient from './client';

export interface EmployeeDto {
  id?: number;
  username: string;
  password?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  hireDate?: string;
  active?: boolean;
  userId?: number;
}

export interface EmployeeSalesDto {
  employeeId: number;
  employeeName: string;
  employeeEmail: string;
  totalSales: number;
  totalRevenue: number;
  startDate?: string;
  endDate?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const employeesApi = {
  getAll: async (active?: boolean, page: number = 0, size: number = 20): Promise<Page<EmployeeDto>> => {
    const params = new URLSearchParams();
    if (active !== undefined) params.append('active', active.toString());
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await apiClient.get(`/admin/employees?${params.toString()}`);
    return response.data;
  },

  getById: async (id: number): Promise<EmployeeDto> => {
    const response = await apiClient.get(`/admin/employees/${id}`);
    return response.data;
  },

  create: async (employee: EmployeeDto): Promise<EmployeeDto> => {
    const response = await apiClient.post('/admin/employees', employee);
    return response.data;
  },

  update: async (id: number, employee: EmployeeDto): Promise<EmployeeDto> => {
    const response = await apiClient.put(`/admin/employees/${id}`, employee);
    return response.data;
  },

  getSales: async (id: number, startDate?: string, endDate?: string): Promise<EmployeeSalesDto> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await apiClient.get(`/admin/employees/${id}/sales?${params.toString()}`);
    return response.data;
  },

  getAllSales: async (startDate?: string, endDate?: string, page: number = 0, size: number = 20): Promise<Page<EmployeeSalesDto>> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await apiClient.get(`/admin/employees/sales?${params.toString()}`);
    return response.data;
  },
};

