import type { CreateClientConfig } from './generated/client.gen';

const ACCESS_TOKEN_KEY = 'accessToken';

const getAccessToken = (): string => {
  try {
    return localStorage.getItem(ACCESS_TOKEN_KEY) ?? '';
  } catch {
    return '';
  }
};

// Used by @hey-api/client-axios when runtimeConfigPath is configured.
// This runs before the generated client instance is initialized.
export const createClientConfig: CreateClientConfig = (config) => ({
  ...config,
  baseURL: '/api',
  auth: () => getAccessToken(),
});
