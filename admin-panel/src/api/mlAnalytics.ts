import apiClient from './client';

export interface MlAnalytics {
  period: string;
  totalRequests: number;
  totalRevenue: number;
  averageRequestValue: number;
  topDestinations: Array<{
    destination: string;
    count: number;
    revenue: number;
  }>;
  trends: Array<{
    date: string;
    requests: number;
    revenue: number;
  }>;
}

export interface MlDashboard {
  totalRequests: number;
  totalRevenue: number;
  averageRequestValue: number;
  nextMonthPredictedRevenue?: number;
  revenueByDestination?: Array<{
    destination: string;
    predictedRevenue: number;
    predictedDemand: number;
    currentRevenue: number;
    trend: string;
  }>;
  requestsByStatus: Record<string, number>;
  topDestinations: Array<{
    destination: string;
    count: number;
  }>;
  recentTrends: Array<{
    date: string;
    value: number;
  }>;
}

export interface TourCluster {
  cluster_id: number;
  cluster_type: string;
  description: string;
  tours: Array<{
    tour_id: number;
    tour_name: string;
    destination: string;
    price: number;
    duration: number;
  }>;
  avg_price: number;
  avg_duration: number;
  total_popularity: number;
  avg_conversion: number;
}

export interface ModelMetrics {
  destination: string;
  linear_r2: number;
  random_forest_r2: number;
  gradient_boosting_r2: number;
  ensemble_r2: number;
  ensemble_mae: number;
  ensemble_rmse: number;
  avg_r2: number;
  weights: {
    linear: number;
    random_forest: number;
    gradient_boosting: number;
  };
}

export interface MlForecast {
  destination?: string;
  forecast: Array<{
    date: string;
    predictedDemand: number;
    predictedRevenue?: number;
    confidence: number;
  }>;
  totalPredictedRevenue?: number;
  destinationBreakdown?: Array<{
    destination: string;
    predictedRevenue: number;
    predictedDemand: number;
    trend: string;
  }>;
  recommendations: string[];
}

export interface MlForecastTableRow {
  destination: string;
  current_demand_per_week: number;
  predicted_demand_per_week: number;
  change_percent: number;
  trend: string;
  confidence: number;
  recommendation: string;
}

export interface MlHealth {
  ml_service: string;
  status: string;
}

export const mlAnalyticsApi = {
  getFullAnalytics: async (period: string = 'month'): Promise<MlAnalytics> => {
    const response = await apiClient.get(`/admin/analytics?period=${period}`);
    return response.data;
  },

  getDashboard: async (): Promise<MlDashboard> => {
    const response = await apiClient.get('/admin/analytics/dashboard');
    return response.data;
  },

  getStatistics: async (days: number = 30): Promise<any> => {
    const response = await apiClient.get(`/admin/analytics/statistics?days=${days}`);
    return response.data;
  },

  getForecast: async (destination?: string): Promise<MlForecast> => {
    try {
      const url = destination 
        ? `/admin/analytics/forecast?destination=${encodeURIComponent(destination)}`
        : '/admin/analytics/forecast';
      console.log('Fetching forecast for destination:', destination || 'all');
      const response = await apiClient.get(url);
      console.log('Forecast API response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching forecast:', error);
      throw error;
    }
  },

  checkHealth: async (): Promise<MlHealth> => {
    const response = await apiClient.get('/admin/analytics/health');
    return response.data;
  },

  getTourClusters: async (nClusters: number = 3): Promise<TourCluster[]> => {
    const response = await apiClient.get(`/admin/analytics/clusters?n_clusters=${nClusters}`);
    return response.data;
  },

  getModelMetrics: async (): Promise<ModelMetrics[]> => {
    const response = await apiClient.get('/admin/analytics/model-metrics');
    return response.data;
  },

  getAnomalousTours: async (): Promise<any[]> => {
    const response = await apiClient.get('/admin/analytics/anomalies');
    return response.data;
  },

  getSeasonalTrends: async (months: number = 12): Promise<any[]> => {
    const response = await apiClient.get(`/admin/analytics/trends?months=${months}`);
    return response.data;
  },

  getAllDestinations: async (): Promise<string[]> => {
    const response = await apiClient.get('/admin/analytics/all-destinations');
    return response.data;
  },

  getForecastTable: async (): Promise<MlForecastTableRow[]> => {
    try {
      console.log('Fetching forecast table');
      const response = await apiClient.get('/admin/analytics/forecast/table');
      console.log('Forecast table API response:', response.data);
      return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
      console.error('Error fetching forecast table:', error);
      return [];
    }
  },
};
