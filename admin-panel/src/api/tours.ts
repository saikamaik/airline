import apiClient from './client';
import { TourDto, Page } from '../types';

export const toursApi = {
  getAll: async (page = 0, size = 20, destination?: string, minPrice?: number, maxPrice?: number): Promise<Page<TourDto>> => {
    const params: any = { page, size };
    if (destination) params.destination = destination;
    if (minPrice !== undefined) params.minPrice = minPrice;
    if (maxPrice !== undefined) params.maxPrice = maxPrice;
    
    const response = await apiClient.get<Page<TourDto>>('/admin/tours', { params });
    return response.data;
  },

  create: async (tour: TourDto): Promise<TourDto> => {
    const response = await apiClient.post<TourDto>('/admin/tours', tour);
    return response.data;
  },

  update: async (id: number, tour: TourDto): Promise<TourDto> => {
    const response = await apiClient.put<TourDto>(`/admin/tours/${id}`, tour);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/admin/tours/${id}`);
  },
};

