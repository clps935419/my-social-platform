import { client as heyClient } from './generated/client.gen';

const ACCESS_TOKEN_KEY = 'accessToken';

const getAccessToken = (): string => {
  try {
    return localStorage.getItem(ACCESS_TOKEN_KEY) ?? '';
  } catch {
    return '';
  }
};

export const configureApiClient = () => {
  // Ensure the generated Hey API client uses the same baseURL as nginx/vite proxy.
  // This avoids hard-coding http://localhost:8080/api in the generated client.
  heyClient.setConfig({
    baseURL: '/api',
    auth: () => getAccessToken(),
  });
};
