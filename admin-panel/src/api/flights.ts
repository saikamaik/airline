import apiClient from './client';
import { FlightDto, Page } from '../types';

export const flightsApi = {
  getAll: async (page = 0, size = 100): Promise<Page<FlightDto>> => {
    const response = await apiClient.get<Page<FlightDto>>('/flights', {
      params: { page, size }
    });
    return response.data;
  },

  search: async (from: string, to: string, page = 1): Promise<Page<FlightDto>> => {
    const response = await apiClient.get<Page<FlightDto>>('/flights/search/by-airports', {
      params: { departure: from, arrival: to, page }
    });
    return response.data;
  },

  searchByAirports: async (departure: string, arrival: string, page = 1): Promise<Page<FlightDto>> => {
    const response = await apiClient.get<Page<FlightDto>>('/flights/search/by-airports', {
      params: { departure, arrival, page }
    });
    return response.data;
  },

  create: async (flight: FlightDto): Promise<void> => {
    await apiClient.post('/flights/add', flight);
  },

  update: async (flightId: number, flight: FlightDto): Promise<void> => {
    await apiClient.put(`/flights/update/${flightId}`, flight);
  },
};

