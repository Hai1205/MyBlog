import { useEffect, useRef, useCallback, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/authStore";
import { SERVER_URL } from "@/services/constants";
import { queryKeys } from "@/lib/queryClient";

interface SSENotificationEvent {
    id: string;
    type: string;
    authorId: string;
    actor: IUser;
    content: string;
    blogId?: string;
    blog?: IBlog;
    createdAt: string;
    isRead: boolean;
}

/**
 * Custom hook to manage SSE (Server-Sent Events) connection for real-time notifications
 * Based on the pattern from the SSE demo but integrated with React Query and the app's architecture
 */
export const useSSENotifications = () => {
    const queryClient = useQueryClient();
    const { userAuth } = useAuthStore();
    const eventSourceRef = useRef<EventSource | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    const [connectionError, setConnectionError] = useState<string>("");

    /**
     * Handle incoming notification from SSE
     */
    const handleNotification = useCallback(
        (notification: SSENotificationEvent) => {
            // Invalidate queries to refetch latest data
            queryClient.invalidateQueries({
                queryKey: queryKeys.notifications.list
            });
            queryClient.invalidateQueries({
                queryKey: queryKeys.notifications.unreadCount
            });

            // Show browser notification if permission granted
            if (typeof window !== "undefined" && Notification.permission === "granted") {
                const notificationTitle = getNotificationTitle(notification);
                const notificationBody = notification.content;

                new Notification(notificationTitle, {
                    body: notificationBody,
                    icon: notification.actor?.avatarUrl || "/images/default-avatar.png",
                    tag: notification.id, // Prevent duplicate notifications
                    requireInteraction: false,
                });
            }
        },
        [queryClient]
    );

    /**
     * Get notification title based on type
     */
    const getNotificationTitle = (notification: SSENotificationEvent): string => {
        const actorName = notification.actor?.username || "Someone";

        switch (notification.type) {
            case "follow":
                return `${actorName} followed you`;
            case "like":
                return `${actorName} liked your blog`;
            case "new_blog":
                return `${actorName} published a new blog`;
            default:
                return "New Notification";
        }
    };

    /**
     * Connect to SSE endpoint
     */
    const connect = useCallback(() => {
        if (!userAuth?.id) {
            console.log("No user authenticated, skipping SSE connection");
            return;
        }

        // Close existing connection if any
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
        }

        try {
            const sseUrl = `${SERVER_URL}/api/v1/notifications/subscribe/${userAuth.id}`;
            console.log("Connecting to SSE:", sseUrl);

            const eventSource = new EventSource(sseUrl, {
                withCredentials: true,
            });

            eventSourceRef.current = eventSource;

            // Handle connection opened
            eventSource.onopen = () => {
                console.log("SSE connection opened");
                setIsConnected(true);
                setConnectionError("");
            };

            // Handle "connected" event
            eventSource.addEventListener("connected", (event) => {
                console.log("SSE Connected event:", event.data);
            });

            // Handle "notification" event
            eventSource.addEventListener("notification", (event) => {
                try {
                    const notification: SSENotificationEvent = JSON.parse(event.data);
                    console.log("Received notification:", notification);
                    handleNotification(notification);
                } catch (error) {
                    console.error("Error parsing notification:", error);
                }
            });

            // Handle errors
            eventSource.onerror = (error) => {
                console.error("SSE error:", error);
                setIsConnected(false);
                setConnectionError("Connection lost. Attempting to reconnect...");

                // EventSource will automatically try to reconnect
                // Close and cleanup if reconnection fails repeatedly
                if (eventSource.readyState === EventSource.CLOSED) {
                    console.log("SSE connection closed");
                    eventSource.close();
                }
            };
        } catch (error) {
            console.error("Error creating SSE connection:", error);
            setConnectionError("Failed to establish connection");
        }
    }, [userAuth?.id, handleNotification]);

    /**
     * Disconnect from SSE
     */
    const disconnect = useCallback(() => {
        if (eventSourceRef.current) {
            console.log("Closing SSE connection");
            eventSourceRef.current.close();
            eventSourceRef.current = null;
            setIsConnected(false);
        }
    }, []);

    /**
     * Request browser notification permission
     */
    const requestNotificationPermission = useCallback(async () => {
        if (typeof window === "undefined" || !("Notification" in window)) {
            console.log("Browser notifications not supported");
            return;
        }

        if (Notification.permission === "default") {
            try {
                const permission = await Notification.requestPermission();
                console.log("Notification permission:", permission);
            } catch (error) {
                console.error("Error requesting notification permission:", error);
            }
        }
    }, []);

    /**
     * Effect to manage SSE connection lifecycle
     */
    useEffect(() => {
        // Only connect if user is authenticated
        if (userAuth?.id) {
            // Request notification permission on mount
            requestNotificationPermission();

            // Establish SSE connection
            connect();

            // Cleanup on unmount or user change
            return () => {
                disconnect();
            };
        } else {
            // Disconnect if user logs out
            disconnect();
        }
    }, [userAuth?.id, connect, disconnect, requestNotificationPermission]);

    return {
        isConnected,
        connectionError,
        connect,
        disconnect,
        requestNotificationPermission,
    };
};
