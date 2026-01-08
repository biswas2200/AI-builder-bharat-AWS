import axios, { AxiosInstance, AxiosError, AxiosResponse } from 'axios';
import { ApiError } from '../types/api';

// Create axios instance with base configuration
const apiClient: AxiosInstance = axios.create({
  baseURL: process.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000, // 10 second timeout
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth tokens, logging, etc.
apiClient.interceptors.request.use(
  (config) => {
    // Log requests in development
    if (process.env.NODE_ENV === 'development') {
      console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
    }
    
    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling and logging
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log successful responses in development
    if (process.env.NODE_ENV === 'development') {
      console.log(`API Response: ${response.status} ${response.config.url}`);
    }
    return response;
  },
  (error: AxiosError) => {
    // Enhanced error handling
    const apiError: ApiError = {
      message: 'An unexpected error occurred',
      status: 500,
      timestamp: new Date().toISOString(),
    };

    if (error.response) {
      // Server responded with error status
      apiError.status = error.response.status;
      apiError.path = error.config?.url;
      
      // Extract error message from response
      if (error.response.data && typeof error.response.data === 'object') {
        const responseData = error.response.data as any;
        apiError.message = responseData.message || responseData.error || 'Server error';
      } else {
        apiError.message = `HTTP ${error.response.status}: ${error.response.statusText}`;
      }
    } else if (error.request) {
      // Network error - no response received
      apiError.message = 'Network error - please check your connection';
      apiError.status = 0;
    } else {
      // Request setup error
      apiError.message = error.message || 'Request configuration error';
    }

    // Log errors in development
    if (process.env.NODE_ENV === 'development') {
      console.error('API Error:', apiError);
    }

    // Handle specific error cases
    switch (apiError.status) {
      case 401:
        // Unauthorized - clear auth token and redirect to login
        localStorage.removeItem('authToken');
        // Could dispatch logout action here if using Redux/Context
        break;
      case 403:
        // Forbidden - user doesn't have permission
        apiError.message = 'You do not have permission to perform this action';
        break;
      case 404:
        // Not found
        apiError.message = 'The requested resource was not found';
        break;
      case 422:
        // Validation error
        apiError.message = 'Validation failed - please check your input';
        break;
      case 429:
        // Rate limiting
        apiError.message = 'Too many requests - please try again later';
        break;
      case 500:
        // Server error
        apiError.message = 'Internal server error - please try again later';
        break;
    }

    return Promise.reject(apiError);
  }
);

// Helper function to handle API responses consistently
export const handleApiResponse = <T>(response: AxiosResponse<T>): T => {
  return response.data;
};

// Helper function to create query parameters
export const createQueryParams = (params: Record<string, any>): string => {
  const searchParams = new URLSearchParams();
  
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      if (Array.isArray(value)) {
        value.forEach(item => searchParams.append(key, String(item)));
      } else {
        searchParams.append(key, String(value));
      }
    }
  });
  
  return searchParams.toString();
};

export default apiClient;