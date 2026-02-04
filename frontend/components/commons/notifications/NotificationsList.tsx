// NotificationsList.tsx
import { ScrollArea } from "@/components/ui/scroll-area";
import { Card } from "@/components/ui/card";
import { Bell } from "lucide-react";
import { NotificationItem } from "./NotificationItem";

interface NotificationsListProps {
  notifications: INotification[];
  activeTab: string;
  onMarkAsRead: (id: string) => void;
  onDelete: (id: string) => void;
}

export const NotificationsList = ({
  notifications,
  activeTab,
  onMarkAsRead,
  onDelete,
}: NotificationsListProps) => {
  if (notifications.length === 0) {
    return (
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
    );
  }

  return (
    <ScrollArea className="h-[calc(100vh-16rem)]">
      <div className="space-y-2 pr-4">
        {notifications.map((notification) => (
          <NotificationItem
            key={notification.id}
            notification={notification}
            onMarkAsRead={onMarkAsRead}
            onDelete={onDelete}
          />
        ))}
      </div>
    </ScrollArea>
  );
}
