import { useQuery, useQueryClient } from '@tanstack/vue-query';
import { getProfile } from '../api/generated/sdk.gen';
import type { UserProfile } from '../api/generated/types.gen';
import { getAccessToken, clearSession } from '../auth/session';

/**
 * Query key for /me endpoint
 */
export const meQueryKey = ['me'];

/**
 * Query hook for fetching current user
 */
export function useMeQuery() {
  const queryClient = useQueryClient();

  return useQuery<UserProfile | null>({
    queryKey: meQueryKey,
    queryFn: async () => {
      const token = getAccessToken();
      if (!token) {
        return null;
      }

      try {
        const response = await getProfile();
        return response.data as UserProfile;
      } catch (error) {
        // If 401, clear session
        if ((error as any)?.response?.status === 401) {
          clearSession();
          queryClient.clear();
        }
        return null;
      }
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    retry: false,
  });
}

/**
 * Invalidate /me query
 */
export function invalidateMeQuery(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: meQueryKey });
}

/**
 * Clear /me query cache
 */
export function clearMeQuery(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.setQueryData(meQueryKey, null);
}
