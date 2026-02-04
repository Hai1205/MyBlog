import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { X } from "lucide-react";
import { cn, formatDateAgo } from "@/lib/utils";
import { getNotificationColor, getNotificationIcon } from "./NotificationsClient";

interface NotificationItemProps {
  notification: INotification;
  onMarkAsRead: (id: string) => void;
  onDelete: (id: string) => void;
}

export const NotificationItem = ({
  notification,
  onMarkAsRead,
  onDelete,
}: NotificationItemProps) => {
  return (
    <Card
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
            <span className="font-semibold">{notification.actor.username}</span>{" "}
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

          {notification?.blog?.title && (
            <p className="text-sm text-primary font-medium mb-2 truncate">
              "{notification.blog.title}"
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
                onClick={() => onMarkAsRead(notification.id)}
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
          onClick={() => onDelete(notification.id)}
          className="shrink-0 text-muted-foreground hover:text-destructive"
        >
          <X className="w-4 h-4" />
        </Button>
      </div>
    </Card>
  );
}
