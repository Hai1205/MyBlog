// NotificationsTabs.tsx
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Heart, UserPlus, FileText } from "lucide-react";
import { ENotificationType } from "@/types/enum";

interface NotificationsTabsProps {
  activeTab: string;
  onTabChange: (value: string) => void;
  notifications: INotification[];
}

export const NotificationsTabs = ({
  activeTab,
  onTabChange,
  notifications,
}: NotificationsTabsProps) => {
  const followCount = notifications.filter(
    (n) => n.type === ENotificationType.FOLLOW,
  ).length;
  const likeCount = notifications.filter(
    (n) => n.type === ENotificationType.LIKE,
  ).length;
  const newBlogCount = notifications.filter(
    (n) => n.type === ENotificationType.NEW_BLOG,
  ).length;

  return (
    <Tabs value={activeTab} onValueChange={onTabChange}>
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
          {followCount > 0 && (
            <Badge variant="secondary" className="ml-1">
              {followCount}
            </Badge>
          )}
        </TabsTrigger>
        <TabsTrigger value="like" className="gap-2">
          <Heart className="w-4 h-4" />
          Likes
          {likeCount > 0 && (
            <Badge variant="secondary" className="ml-1">
              {likeCount}
            </Badge>
          )}
        </TabsTrigger>
        <TabsTrigger value="new_blog" className="gap-2">
          <FileText className="w-4 h-4" />
          New Posts
          {newBlogCount > 0 && (
            <Badge variant="secondary" className="ml-1">
              {newBlogCount}
            </Badge>
          )}
        </TabsTrigger>
      </TabsList>
    </Tabs>
  );
}
