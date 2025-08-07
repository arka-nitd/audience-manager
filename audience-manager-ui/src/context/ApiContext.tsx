import React, { createContext, useContext, ReactNode } from 'react';
import axios, { AxiosInstance } from 'axios';

// API Configuration
const API_BASE_URL = process.env.REACT_APP_API_URL || '/api/v1';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for logging
apiClient.interceptors.request.use(
  (config) => {
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('API Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => {
    console.log(`API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('API Response Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// Types
export interface SegmentRule {
  eventType: string;
  attribute: 'count' | 'sum';
  operator: string;
  value: string;
}

export interface SegmentDependency {
  independentSegmentId: string;
  logicalOperator: string;
}

export interface CreateSegmentRequest {
  name: string;
  description?: string;
  type: 'INDEPENDENT' | 'DERIVED';
  windowMinutes?: number;
  logicalExpression?: string;
  rules?: SegmentRule[];
  dependencies?: SegmentDependency[];
}

export interface SegmentResponse {
  id: string;
  name: string;
  description?: string;
  type: 'INDEPENDENT' | 'DERIVED';
  logicalExpression?: string;
  windowMinutes?: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  rules?: SegmentRule[];
  dependencies?: SegmentDependency[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

// API Service
class ApiService {
  // Segment operations
  async createSegment(request: CreateSegmentRequest): Promise<SegmentResponse> {
    const response = await apiClient.post<SegmentResponse>('/segments', request);
    return response.data;
  }

  async getSegments(
    page: number = 0,
    size: number = 20,
    type?: 'INDEPENDENT' | 'DERIVED',
    active?: boolean
  ): Promise<PageResponse<SegmentResponse>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (type) params.append('type', type);
    if (active !== undefined) params.append('active', active.toString());

    const response = await apiClient.get<PageResponse<SegmentResponse>>(`/segments?${params}`);
    return response.data;
  }

  async getSegmentById(id: string): Promise<SegmentResponse> {
    const response = await apiClient.get<SegmentResponse>(`/segments/${id}`);
    return response.data;
  }

  async searchSegments(query: string): Promise<SegmentResponse[]> {
    const response = await apiClient.get<SegmentResponse[]>(`/segments/search?q=${encodeURIComponent(query)}`);
    return response.data;
  }

  async getIndependentSegments(): Promise<SegmentResponse[]> {
    const response = await apiClient.get<SegmentResponse[]>('/segments/independent');
    return response.data;
  }

  async updateSegment(id: string, request: Partial<CreateSegmentRequest>): Promise<SegmentResponse> {
    const response = await apiClient.put<SegmentResponse>(`/segments/${id}`, request);
    return response.data;
  }

  async updateSegmentStatus(id: string, active: boolean): Promise<SegmentResponse> {
    const response = await apiClient.patch<SegmentResponse>(`/segments/${id}/status`, { active });
    return response.data;
  }

  async deleteSegment(id: string): Promise<void> {
    await apiClient.delete(`/segments/${id}`);
  }

  async getHealthCheck(): Promise<{ status: string; timestamp: string }> {
    const response = await apiClient.get('/segments/health');
    return response.data;
  }
}

// Context
interface ApiContextType {
  apiService: ApiService;
  isHealthy: boolean;
  checkHealth: () => Promise<void>;
}

const ApiContext = createContext<ApiContextType | undefined>(undefined);

export const useApi = (): ApiContextType => {
  const context = useContext(ApiContext);
  if (!context) {
    throw new Error('useApi must be used within an ApiProvider');
  }
  return context;
};

// Provider component
interface ApiProviderProps {
  children: ReactNode;
}

export const ApiProvider: React.FC<ApiProviderProps> = ({ children }) => {
  const [isHealthy, setIsHealthy] = React.useState<boolean>(true);
  const apiService = React.useMemo(() => new ApiService(), []);

  const checkHealth = React.useCallback(async () => {
    try {
      await apiService.getHealthCheck();
      setIsHealthy(true);
    } catch (error) {
      console.error('Health check failed:', error);
      setIsHealthy(false);
    }
  }, [apiService]);

  React.useEffect(() => {
    checkHealth();
    const interval = setInterval(checkHealth, 30000); // Check every 30 seconds
    return () => clearInterval(interval);
  }, [checkHealth]);

  const value: ApiContextType = {
    apiService,
    isHealthy,
    checkHealth,
  };

  return (
    <ApiContext.Provider value={value}>
      {children}
    </ApiContext.Provider>
  );
};