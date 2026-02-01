import { client as heyClient } from './generated/client.gen';
import { refresh } from './generated/sdk.gen';
import { getAccessToken, getRefreshToken, saveSession, clearSession } from '../auth/session';
import type { RefreshResponse2 } from './generated/types.gen';
import axios from 'axios';

let isRefreshing = false;
let refreshPromise: Promise<string | null> | null = null;

const publicEndpointRules = [
  { method: 'GET', pattern: /^\/posts$/ },
  { method: 'GET', pattern: /^\/posts\/[^/]+\/comments$/ },
  { method: 'GET', pattern: /^\/health$/ },
  { method: 'POST', pattern: /^\/auth\/login$/ },
  { method: 'POST', pattern: /^\/auth\/register$/ },
  { method: 'POST', pattern: /^\/auth\/refresh$/ },
];

const normalizePath = (config: axios.AxiosRequestConfig): string => {
  const baseURL = config.baseURL ?? '';
  const url = config.url ?? '';
  let path = '';

  try {
    if (url.startsWith('http')) {
      path = new URL(url).pathname;
    } else if (baseURL.startsWith('http')) {
      path = new URL(url, baseURL).pathname;
    } else {
      const merged = `${baseURL}${url.startsWith('/') ? '' : '/'}${url}`;
      path = merged;
    }
  } catch {
    path = url;
  }

  if (!path.startsWith('/')) path = `/${path}`;
  if (path.startsWith('/api/')) path = path.slice(4);
  if (path === '/api') path = '/';
  const queryIndex = path.indexOf('?');
  if (queryIndex >= 0) path = path.slice(0, queryIndex);
  return path;
};

const isPublicRequest = (config: axios.AxiosRequestConfig): boolean => {
  const method = (config.method ?? 'get').toUpperCase();
  const path = normalizePath(config);
  return publicEndpointRules.some((rule) => rule.method === method && rule.pattern.test(path));
};

/**
 * Refresh access token using refresh token
 */
async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    clearSession();
    return null;
  }

  try {
    const response = await refresh({
      body: { refreshToken },
    });
    const data = response.data as RefreshResponse2 | undefined;
    if (!data?.accessToken || !data?.refreshToken) {
      clearSession();
      return null;
    }

    // Save new tokens
    saveSession({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      user: null, // User data stays the same
    });

    return data.accessToken;
  } catch (error) {
    // Refresh failed, clear session
    clearSession();
    return null;
  }
}

export const configureApiClient = () => {
  // Ensure the generated Hey API client uses the same baseURL as nginx/vite proxy.
  // This avoids hard-coding http://localhost:8080/api in the generated client.
  heyClient.setConfig({
    baseURL: '/api',
  });

  // Get the underlying axios instance from the Hey API client
  const axiosInstance = heyClient.instance;

  // Add request interceptor to attach access token (skip public endpoints)
  axiosInstance.interceptors.request.use((config) => {
    if (isPublicRequest(config)) {
      if (config.headers) {
        delete (config.headers as Record<string, string>).Authorization;
        delete (config.headers as Record<string, string>).authorization;
      }
      return config;
    }

    const token = getAccessToken();
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  // Add response interceptor to handle 401 and retry with refresh
  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      // If 401 and not already retried
      if (error.response?.status === 401 && !originalRequest._retry && !isPublicRequest(originalRequest)) {
        originalRequest._retry = true;

        // Single-flight refresh
        if (!isRefreshing) {
          isRefreshing = true;
          refreshPromise = refreshAccessToken();
        }

        const newToken = await refreshPromise;
        isRefreshing = false;
        refreshPromise = null;

        if (newToken) {
          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return axiosInstance.request(originalRequest);
        }

        // Refresh failed, redirect to login or show message
        // For now, just reject
        return Promise.reject(error);
      }

      return Promise.reject(error);
    }
  );
};
