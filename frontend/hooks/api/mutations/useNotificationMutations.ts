import { useMutation, UseMutationResult, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import {
    notificationService,
    SendNotificationDTO,
} from "../services/notificationService";
import { IApiResponse } from "@/lib/axiosInstance";
import { queryKeys } from "@/lib/queryClient";

/**
 * Notification Mutations - for POST/PUT/DELETE operations
 * Each mutation includes cache invalidation
 */

/**
 * Mark notifications as read mutation
 */
export const useMarkAsReadMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    string[]
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: notificationService.markAsRead,
        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                // Invalidate notifications queries to refetch
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.list });
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.unreadCount });
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to mark as read";
            toast.error(message);
        },
    });
};

/**
 * Mark all notifications as read mutation
 */
export const useMarkAllAsReadMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    void
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: notificationService.markAllAsRead,
        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "All notifications marked as read");
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.list });
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.unreadCount });
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to mark all as read";
            toast.error(message);
        },
    });
};

/**
 * Delete notification mutation
 */
export const useDeleteNotificationMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    string
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: notificationService.deleteNotification,
        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Notification deleted");
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.list });
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.unreadCount });
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to delete notification";
            toast.error(message);
        },
    });
};

/**
 * Delete all notifications mutation
 */
export const useDeleteAllNotificationsMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    void
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: notificationService.deleteAllNotifications,
        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "All notifications deleted");
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.list });
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.unreadCount });
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to delete all notifications";
            toast.error(message);
        },
    });
};

/**
 * Send notification mutation (for testing/admin)
 */
export const useSendNotificationMutation = (): UseMutationResult<
    IApiResponse<INotification>,
    Error,
    SendNotificationDTO
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: notificationService.sendNotification,
        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Notification sent successfully");
                queryClient.invalidateQueries({ queryKey: queryKeys.notifications.list });
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to send notification";
            toast.error(message);
        },
    });
};
