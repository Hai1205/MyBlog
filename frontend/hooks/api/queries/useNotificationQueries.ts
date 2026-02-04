import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { notificationService } from "../services/notificationService";
import { IApiResponse } from "@/lib/axiosInstance";
import { queryKeys } from "@/lib/queryClient";

/**
 * Notification Queries - for GET operations
 * Uses React Query for caching, refetching, and state management
 */

/**
 * Get all notifications with pagination
 */
export const useGetNotifications = (
    params?: IPageable,
    enabled: boolean = true
): UseQueryResult<IApiResponse<IPageResponse<INotification>>, Error> => {
    return useQuery({
        queryKey: [...queryKeys.notifications.list, params],
        queryFn: () => notificationService.getAllNotifications(params),
        enabled,
        staleTime: 1000 * 30, // 30 seconds
        gcTime: 1000 * 60 * 5, // 5 minutes (formerly cacheTime)
    });
};

/**
 * Get unread notifications count
 */
export const useGetUnreadCount = (
    enabled: boolean = true
): UseQueryResult<IApiResponse<number>, Error> => {
    return useQuery({
        queryKey: queryKeys.notifications.unreadCount,
        queryFn: () => notificationService.getUnreadCount(),
        enabled,
        staleTime: 1000 * 30, // 30 seconds
        gcTime: 1000 * 60 * 5, // 5 minutes
        refetchInterval: 1000 * 60, // Refetch every minute
    });
};
