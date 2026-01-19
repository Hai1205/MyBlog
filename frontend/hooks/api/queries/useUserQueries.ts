import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { queryKeys } from "@/lib/queryClient";
import { userService, IUserDataResponse } from "../services/userService";
import { IApiResponse } from "@/lib/axiosInstance";

/**
 * User Queries - for GET requests
 */

/**
 * Get all users with automatic caching
 */
export const useAllUsersQuery = (
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IUserDataResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.users.lists(),
        queryFn: userService.getAllUsers,
        enabled: options?.enabled ?? true,
        staleTime: 3 * 60 * 1000, // 3 minutes
    });
};

/**
 * Get single user by ID
 */
export const useUserQuery = (
    userId: string,
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IUserDataResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.users.detail(userId),
        queryFn: () => userService.getUser(userId),
        enabled: (options?.enabled ?? true) && !!userId,
        staleTime: 5 * 60 * 1000, // 5 minutes
    });
};
