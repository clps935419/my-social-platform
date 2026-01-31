import type { UserInfo } from '../api/generated/types.gen';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

export interface SessionState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserInfo | null;
}

/**
 * Load session from localStorage
 */
export function loadSession(): SessionState {
  try {
    return {
      accessToken: localStorage.getItem(ACCESS_TOKEN_KEY),
      refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
      user: JSON.parse(localStorage.getItem(USER_KEY) || 'null') as UserInfo | null,
    };
  } catch {
    return { accessToken: null, refreshToken: null, user: null };
  }
}

/**
 * Save session to localStorage
 */
export function saveSession(session: SessionState): void {
  try {
    if (session.accessToken) {
      localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
    } else {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
    }

    if (session.refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
    } else {
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    }

    if (session.user) {
      localStorage.setItem(USER_KEY, JSON.stringify(session.user));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  } catch {
    // Ignore storage errors
  }
}

/**
 * Clear session from localStorage
 */
export function clearSession(): void {
  try {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  } catch {
    // Ignore storage errors
  }
}

/**
 * Get access token
 */
export function getAccessToken(): string | null {
  try {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  } catch {
    return null;
  }
}

/**
 * Get refresh token
 */
export function getRefreshToken(): string | null {
  try {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  } catch {
    return null;
  }
}

/**
 * Get current user
 */
export function getCurrentUser(): UserInfo | null {
  try {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? (JSON.parse(userStr) as UserInfo) : null;
  } catch {
    return null;
  }
}
