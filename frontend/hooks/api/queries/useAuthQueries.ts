import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { queryKeys } from "@/lib/queryClient";
import { authService, IAuthDataResponse } from "../services/authService";
import { IApiResponse } from "@/lib/axiosInstance";

/**
 * Auth Queries - for GET requests and data fetching
 * Note: Auth typically doesn't have many GET queries
 * Most auth operations are mutations (POST/PATCH)
 */

/**
 * Refresh token query
 * This can be used in TokenRefresher component
 */
export const useRefreshTokenQuery = (
    options?: {
        enabled?: boolean;
        refetchInterval?: number;
    }
): UseQueryResult<IApiResponse, Error> => {
    return useQuery({
        queryKey: queryKeys.auth.refreshToken(),
        queryFn: authService.refreshToken,
        enabled: options?.enabled ?? false, // Disabled by default
        refetchInterval: options?.refetchInterval,
        retry: false, // Don't retry on failure
        staleTime: Infinity, // Never stale
    });
};

/**
 * Get current authenticated user
 * This would require a /auth/me endpoint in your backend
 * For now, we'll use Zustand's userAuth state
 */
export const useCurrentUserQuery = (
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IAuthDataResponse>, Error> => {
    // TODO: Implement when backend has /auth/me endpoint
    // return useQuery({
    //   queryKey: queryKeys.auth.user(),
    //   queryFn: authService.getCurrentUser,
    //   enabled: options?.enabled ?? true,
    //   staleTime: 5 * 60 * 1000, // 5 minutes
    // });

    throw new Error("useCurrentUserQuery not implemented - use Zustand authStore.userAuth");
};
