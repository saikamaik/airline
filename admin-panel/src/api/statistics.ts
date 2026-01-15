import apiClient from './client';

export interface DestinationStat {
  destination: string;
  tourCount: number;
  requestCount: number;
}

export interface RequestByDate {
  date: string;
  count: number;
}

export interface Statistics {
  totalTours: number;
  activeTours: number;
  totalRequests: number;
  newRequests: number;
  requestsByStatus: Record<string, number>;
  topDestinations: DestinationStat[];
  avgTourPrice: number;
  minTourPrice: number;
  maxTourPrice: number;
  requestsByDate: RequestByDate[];
}

export const statisticsApi = {
  getStatistics: async (startDate?: string, endDate?: string): Promise<Statistics> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const url = `/admin/statistics${params.toString() ? '?' + params.toString() : ''}`;
    const response = await apiClient.get(url);
    return response.data;
  },
  
  exportToCsv: async (startDate?: string, endDate?: string): Promise<Blob> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const url = `/admin/statistics/export/csv${params.toString() ? '?' + params.toString() : ''}`;
    const response = await apiClient.get(url, {
      responseType: 'blob',
    });
    return response.data;
  },
};

