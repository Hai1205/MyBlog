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
 * Get single user by getUserByIdentifier
 */
export const useUserQuery = (
    identifier: string,
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IUserDataResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.users.detail(identifier),
        queryFn: () => userService.getUserByIdentifier(identifier),
        enabled: (options?.enabled ?? true) && !!identifier,
        staleTime: 5 * 60 * 1000, // 5 minutes
    });
};
