// NotificationsHeader.tsx
import { Button } from "@/components/ui/button";
import { Bell, Check, Trash2 } from "lucide-react";

interface NotificationsHeaderProps {
  unreadCount: number;
  onMarkAllAsRead: () => void;
  onDeleteAll: () => void;
  totalNotifications: number;
}

export const NotificationsHeader = ({
  unreadCount,
  onMarkAllAsRead,
  onDeleteAll,
  totalNotifications,
}: NotificationsHeaderProps) => {
  return (
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
            onClick={onMarkAllAsRead}
            disabled={unreadCount === 0}
            className="gap-2"
          >
            <Check className="w-4 h-4" />
            Mark all as read
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={onDeleteAll}
            disabled={totalNotifications === 0}
            className="gap-2 text-destructive hover:text-destructive"
          >
            <Trash2 className="w-4 h-4" />
            Delete all
          </Button>
        </div>
      </div>
    </div>
  );
}
