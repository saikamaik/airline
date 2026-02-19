import apiClient from './client';

export interface ClientDto {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  birthDate?: string;
  notes?: string;
  vipStatus?: boolean;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
  totalRequests?: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const clientsApi = {
  getAll: async (
    search?: string,
    vipStatus?: boolean,
    page: number = 0,
    size: number = 20
  ): Promise<Page<ClientDto>> => {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (vipStatus !== undefined) params.append('vipStatus', vipStatus.toString());
    params.append('page', page.toString());
    params.append('size', size.toString());

    const response = await apiClient.get(`/admin/clients?${params.toString()}`);
    return response.data;
  },

  getById: async (id: number): Promise<ClientDto> => {
    const response = await apiClient.get(`/admin/clients/${id}`);
    return response.data;
  },

  create: async (client: ClientDto): Promise<ClientDto> => {
    const response = await apiClient.post('/admin/clients', client);
    return response.data;
  },

  update: async (id: number, client: ClientDto): Promise<ClientDto> => {
    const response = await apiClient.put(`/admin/clients/${id}`, client);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/admin/clients/${id}`);
  },

  setVipStatus: async (id: number, vip: boolean): Promise<ClientDto> => {
    const client = await clientsApi.getById(id);
    return clientsApi.update(id, { ...client, vipStatus: vip });
  },
};
