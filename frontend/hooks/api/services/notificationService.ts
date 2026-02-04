import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";

// DTO interfaces
export interface SendNotificationDTO {
    recipientId: string;
    type: string;
    content: string;
    blogId?: string;
}

export interface MarkAsReadDTO {
    notificationIds: string[];
}

/**
 * Notification Service - Pure API calls without state management
 * All functions return Promise<IApiResponse<T>>
 */
export const notificationService = {
    /**
     * Get all notifications for current user (with pagination)
     */
    getAllNotifications: async (
        params?: IPageable
    ): Promise<IApiResponse<IPageResponse<INotification>>> => {
        const queryParams = new URLSearchParams();
        if (params?.page !== undefined) queryParams.append("page", params.page.toString());
        if (params?.size !== undefined) queryParams.append("size", params.size.toString());
        if (params?.sort) queryParams.append("sort", params.sort);

        return await handleRequest<IPageResponse<INotification>>(
            EHttpType.GET,
            `/notifications${queryParams.toString() ? `?${queryParams.toString()}` : ""}`
        );
    },

    /**
     * Get unread notifications count
     */
    getUnreadCount: async (): Promise<IApiResponse<number>> => {
        return await handleRequest<number>(
            EHttpType.GET,
            "/notifications/unread/count"
        );
    },

    /**
     * Mark notifications as read
     */
    markAsRead: async (notificationIds: string[]): Promise<IApiResponse> => {
        return await handleRequest(
            EHttpType.PUT,
            "/notifications/mark-read",
            { notificationIds }
        );
    },

    /**
     * Mark all notifications as read
     */
    markAllAsRead: async (): Promise<IApiResponse> => {
        return await handleRequest(
            EHttpType.PUT,
            "/notifications/mark-all-read"
        );
    },

    /**
     * Delete a notification
     */
    deleteNotification: async (notificationId: string): Promise<IApiResponse> => {
        return await handleRequest(
            EHttpType.DELETE,
            `/notifications/${notificationId}`
        );
    },

    /**
     * Delete all notifications
     */
    deleteAllNotifications: async (): Promise<IApiResponse> => {
        return await handleRequest(
            EHttpType.DELETE,
            "/notifications"
        );
    },

    /**
     * Send a notification (for testing or admin purposes)
     */
    sendNotification: async (
        data: SendNotificationDTO
    ): Promise<IApiResponse<INotification>> => {
        return await handleRequest<INotification>(
            EHttpType.POST,
            "/notifications/send",
            { data }
        );
    },
};
