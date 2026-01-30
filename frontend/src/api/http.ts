import axios from 'axios';

/**
 * API client configuration
 * Base URL: /api (proxied by nginx in production, by vite dev server in development)
 */
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor: Add Authorization header if token exists
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor: Handle errors uniformly
 */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;
      
      // Handle 401 Unauthorized - could trigger refresh token flow here
      if (status === 401) {
        // TODO: Implement automatic token refresh
        console.error('Unauthorized - please log in again');
      }
      
      // Extract error message from standard ErrorResponse format
      const errorMessage = data?.message || 'An error occurred';
      console.error(`API Error [${status}]:`, errorMessage);
      
      // Re-throw with consistent format
      error.message = errorMessage;
      error.errorCode = data?.errorCode;
    } else if (error.request) {
      // Request was made but no response received
      console.error('Network error:', error.message);
      error.message = 'Network error - please check your connection';
    } else {
      // Something else happened
      console.error('Error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
