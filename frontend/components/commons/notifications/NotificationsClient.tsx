"use client";

import { useState, useMemo } from "react";
import { ENotificationType } from "@/types/enum";
import { Heart, UserPlus, FileText, WifiOff, Wifi } from "lucide-react";
import { NotificationsHeader } from "./NotificationsHeader";
import { NotificationsTabs } from "./NotificationsTabs";
import { NotificationsList } from "./NotificationsList";
import { useSSENotifications } from "@/hooks/useSSENotifications";
import { useGetNotifications } from "@/hooks/api/queries/useNotificationQueries";
import {
  useMarkAsReadMutation,
  useMarkAllAsReadMutation,
  useDeleteNotificationMutation,
  useDeleteAllNotificationsMutation,
} from "@/hooks/api/mutations/useNotificationMutations";
import { useAuthStore } from "@/stores/authStore";

export const getNotificationIcon = (
  type: ENotificationType,
): JSX.Element | undefined => {
  switch (type) {
    case ENotificationType.FOLLOW:
      return <UserPlus className="w-5 h-5" />;
    case ENotificationType.LIKE:
      return <Heart className="w-5 h-5" />;
    case ENotificationType.NEW_BLOG:
      return <FileText className="w-5 h-5" />;
    default:
      return undefined;
  }
};

export const getNotificationColor = (type: ENotificationType): string => {
  switch (type) {
    case ENotificationType.FOLLOW:
      return "bg-primary text-primary-foreground";
    case ENotificationType.LIKE:
      return "bg-destructive text-destructive-foreground";
    case ENotificationType.NEW_BLOG:
      return "bg-secondary text-secondary-foreground";
    default:
      return "";
  }
};

export default function NotificationsClient() {
  const [activeTab, setActiveTab] = useState<string>("all");
  const { userAuth } = useAuthStore();

  // SSE Connection for real-time notifications
  const { isConnected, connectionError } = useSSENotifications();

  // Fetch notifications with React Query
  const {
    data: notificationsResponse,
    isLoading,
    error,
  } = useGetNotifications(
    { page: 0, size: 100 },
    !!userAuth, // Only fetch if user is authenticated
  );

  // Mutations
  const markAsReadMutation = useMarkAsReadMutation();
  const markAllAsReadMutation = useMarkAllAsReadMutation();
  const deleteNotificationMutation = useDeleteNotificationMutation();
  const deleteAllNotificationsMutation = useDeleteAllNotificationsMutation();

  // Extract notifications from response
  const notifications = useMemo(() => {
    return notificationsResponse?.data?.content || [];
  }, [notificationsResponse]);

  // Filter notifications based on active tab
  const getFilteredNotifications = () => {
    switch (activeTab) {
      case "follow":
        return notifications.filter((n) => n.type === ENotificationType.FOLLOW);
      case "like":
        return notifications.filter((n) => n.type === ENotificationType.LIKE);
      case "new_blog":
        return notifications.filter(
          (n) => n.type === ENotificationType.NEW_BLOG,
        );
      default:
        return notifications;
    }
  };

  // Mark all as read
  const handleMarkAllAsRead = () => {
    markAllAsReadMutation.mutate();
  };

  // Delete all notifications
  const handleDeleteAll = () => {
    if (confirm("Are you sure you want to delete all notifications?")) {
      deleteAllNotificationsMutation.mutate();
    }
  };

  // Mark single as read
  const handleMarkAsRead = (id: string) => {
    markAsReadMutation.mutate([id]);
  };

  // Delete single notification
  const handleDeleteNotification = (id: string) => {
    deleteNotificationMutation.mutate(id);
  };

  const filteredNotifications = getFilteredNotifications();
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-background">
      <div className="max-w-4xl mx-auto p-6">
        {/* SSE Connection Status */}
        <div className="mb-4 flex items-center justify-end gap-2 text-sm">
          {isConnected ? (
            <div className="flex items-center gap-2 text-green-600">
              <Wifi className="w-4 h-4" />
              <span>Connected</span>
            </div>
          ) : (
            <div className="flex items-center gap-2 text-red-600">
              <WifiOff className="w-4 h-4" />
              <span>{connectionError || "Disconnected"}</span>
            </div>
          )}
        </div>

        <NotificationsHeader
          unreadCount={unreadCount}
          onMarkAllAsRead={handleMarkAllAsRead}
          onDeleteAll={handleDeleteAll}
          totalNotifications={notifications.length}
        />

        <NotificationsTabs
          activeTab={activeTab}
          onTabChange={setActiveTab}
          notifications={notifications}
        />

        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
          </div>
        ) : error ? (
          <div className="text-center py-12">
            <p className="text-destructive">Failed to load notifications</p>
          </div>
        ) : (
          <NotificationsList
            notifications={filteredNotifications}
            activeTab={activeTab}
            onMarkAsRead={handleMarkAsRead}
            onDelete={handleDeleteNotification}
          />
        )}
      </div>
    </div>
  );
}
