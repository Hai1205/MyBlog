"use client";

import { useState } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Heart,
  UserPlus,
  FileText,
  X,
  Check,
  Trash2,
  Bell,
} from "lucide-react";
import { cn, formatDateAgo } from "@/lib/utils";
import { ENotificationType } from "@/types/enum";
import { mockNoti } from "@/services/mockData";

export default function NotificationsClient() {
  const [notifications, setNotifications] = useState<INotification[]>(mockNoti);

  const [activeTab, setActiveTab] = useState<string>("all");

  // Get notification icon and color based on type
  const getNotificationIcon = (type: ENotificationType) => {
    switch (type) {
      case ENotificationType.FOLLOW:
        return <UserPlus className="w-5 h-5" />;
      case ENotificationType.LIKE:
        return <Heart className="w-5 h-5" />;
      case ENotificationType.NEW_BLOG:
        return <FileText className="w-5 h-5" />;
    }
  };

  const getNotificationColor = (type: ENotificationType) => {
    switch (type) {
      case ENotificationType.FOLLOW:
        return "bg-primary text-primary-foreground";
      case ENotificationType.LIKE:
        return "bg-destructive text-destructive-foreground";
      case ENotificationType.NEW_BLOG:
        return "bg-secondary text-secondary-foreground";
    }
  };

  // Format time
  const formatTime = (date: Date) => {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return "Just now";
    if (minutes < 60) return `${minutes} minutes ago`;
    if (hours < 24) return `${hours} hours ago`;
    if (days < 7) return `${days} days ago`;
    return date.toLocaleDateString("en-US");
  };

  // Filter notifications
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
    setNotifications((prev) =>
      prev.map((notification) => ({ ...notification, isRead: true })),
    );
  };

  // Delete all notifications
  const handleDeleteAll = () => {
    if (confirm("Are you sure you want to delete all notifications?")) {
      setNotifications([]);
    }
  };

  // Mark single as read
  const handleMarkAsRead = (id: string) => {
    setNotifications((prev) =>
      prev.map((notification) =>
        notification.id === id
          ? { ...notification, isRead: true }
          : notification,
      ),
    );
  };

  // Delete single notification
  const handleDeleteNotification = (id: string) => {
    setNotifications((prev) =>
      prev.filter((notification) => notification.id !== id),
    );
  };

  const filteredNotifications = getFilteredNotifications();
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-background">
      <div className="max-w-4xl mx-auto p-6">
        {/* Header */}
        <div className="mb-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-primary/10">
                <Bell className="w-6 h-6 text-primary" />
              </div>
              <div>
                <h1 className="text-2xl font-bold">Notifications</h1>
                {unreadCount > 0 && (
                  <p className="text-sm text-muted-foreground">
                    You have {unreadCount} unread notifications
                  </p>
                )}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={handleMarkAllAsRead}
                disabled={unreadCount === 0}
                className="gap-2"
              >
                <Check className="w-4 h-4" />
                Mark all as read
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={handleDeleteAll}
                disabled={notifications.length === 0}
                className="gap-2 text-destructive hover:text-destructive"
              >
                <Trash2 className="w-4 h-4" />
                Delete all
              </Button>
            </div>
          </div>

          {/* Tabs */}
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="w-full justify-start">
              <TabsTrigger value="all" className="gap-2">
                All
                {notifications.length > 0 && (
                  <Badge variant="secondary" className="ml-1">
                    {notifications.length}
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="follow" className="gap-2">
                <UserPlus className="w-4 h-4" />
                Follows
                {notifications.filter(
                  (n) => n.type === ENotificationType.FOLLOW,
                ).length > 0 && (
                  <Badge variant="secondary" className="ml-1">
                    {
                      notifications.filter(
                        (n) => n.type === ENotificationType.FOLLOW,
                      ).length
                    }
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="like" className="gap-2">
                <Heart className="w-4 h-4" />
                Likes
                {notifications.filter((n) => n.type === ENotificationType.LIKE)
                  .length > 0 && (
                  <Badge variant="secondary" className="ml-1">
                    {
                      notifications.filter(
                        (n) => n.type === ENotificationType.LIKE,
                      ).length
                    }
                  </Badge>
                )}
              </TabsTrigger>
              <TabsTrigger value="new_blog" className="gap-2">
                <FileText className="w-4 h-4" />
                New Posts
                {notifications.filter(
                  (n) => n.type === ENotificationType.NEW_BLOG,
                ).length > 0 && (
                  <Badge variant="secondary" className="ml-1">
                    {
                      notifications.filter(
                        (n) => n.type === ENotificationType.NEW_BLOG,
                      ).length
                    }
                  </Badge>
                )}
              </TabsTrigger>
            </TabsList>
          </Tabs>
        </div>

        {/* Notifications List */}
        {filteredNotifications.length === 0 ? (
          <Card className="p-12">
            <div className="text-center">
              <div className="mb-4 inline-flex items-center justify-center w-16 h-16 rounded-full bg-muted">
                <Bell className="w-8 h-8 text-muted-foreground" />
              </div>
              <h3 className="text-lg font-semibold mb-2">No notifications</h3>
              <p className="text-muted-foreground">
                {activeTab === "all"
                  ? "You have no notifications yet"
                  : `No notifications of this type`}
              </p>
            </div>
          </Card>
        ) : (
          <ScrollArea className="h-[calc(100vh-16rem)]">
            <div className="space-y-2 pr-4">
              {filteredNotifications.map((notification) => (
                <Card
                  key={notification.id}
                  className={cn(
                    "p-4 transition-colors hover:bg-accent/50",
                    !notification.isRead && "bg-accent/30 border-primary/30",
                  )}
                >
                  <div className="flex items-start gap-4">
                    {/* Icon */}
                    <div
                      className={cn(
                        "p-2 rounded-full shrink-0",
                        getNotificationColor(notification.type),
                      )}
                    >
                      {getNotificationIcon(notification.type)}
                    </div>

                    {/* Avatar */}
                    <Avatar className="shrink-0">
                      <AvatarImage src={notification.actor.avatarUrl} />
                      <AvatarFallback className="bg-primary text-primary-foreground">
                        {notification.actor.username.charAt(0).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>

                    {/* Content */}
                    <div className="flex-1 min-w-0">
                      <div className="mb-1">
                        <span className="font-semibold">
                          {notification.actor.username}
                        </span>{" "}
                        <span className="text-muted-foreground">
                          {notification.content}
                        </span>
                        {!notification.isRead && (
                          <Badge
                            className="ml-2 bg-primary text-primary-foreground"
                            variant="default"
                          >
                            Mới
                          </Badge>
                        )}
                      </div>

                      {notification.blogTitle && (
                        <p className="text-sm text-primary font-medium mb-2 truncate">
                          "{notification.blogTitle}"
                        </p>
                      )}

                      <p className="text-xs text-muted-foreground">
                        {formatDateAgo(notification.createdAt)}
                      </p>

                      {/* Actions for unread */}
                      {!notification.isRead && (
                        <div className="mt-2 flex gap-2">
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleMarkAsRead(notification.id)}
                            className="h-7 text-xs"
                          >
                            Đánh dấu đã đọc
                          </Button>
                        </div>
                      )}
                    </div>

                    {/* Delete Button */}
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleDeleteNotification(notification.id)}
                      className="shrink-0 text-muted-foreground hover:text-destructive"
                    >
                      <X className="w-4 h-4" />
                    </Button>
                  </div>
                </Card>
              ))}
            </div>
          </ScrollArea>
        )}
      </div>
    </div>
  );
}
