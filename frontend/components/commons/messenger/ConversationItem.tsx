// ConversationItem.tsx
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { cn, formatDateAgo } from "@/lib/utils";

interface ConversationItemProps {
  conversation: IConversation;
  isSelected: boolean;
  onClick: () => void;
  currentUserId: string;
}

export const ConversationItem = ({
  conversation,
  isSelected,
  onClick,
  currentUserId,
}: ConversationItemProps) => {
  return (
    <div
      onClick={onClick}
      className={cn(
        "flex items-center gap-3 p-4 cursor-pointer transition-colors hover:bg-accent",
        isSelected && "bg-accent",
      )}
    >
      <div className="relative">
        <Avatar>
          <AvatarImage src={conversation.participant.avatarUrl} />
          <AvatarFallback className="bg-primary text-primary-foreground">
            {conversation.participant.username.charAt(0).toUpperCase()}
          </AvatarFallback>
        </Avatar>
        {conversation.participant.isOnline && (
          <span className="absolute bottom-0 right-0 w-3 h-3 bg-secondary border-2 border-background rounded-full" />
        )}
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-1">
          <h3 className="font-semibold text-sm truncate">
            {conversation.participant.username}
          </h3>
          {conversation.lastMessage && (
            <span className="text-xs text-muted-foreground">
              {formatDateAgo(`${conversation.lastMessage.createdAt}`)}
            </span>
          )}
        </div>
        <div className="flex items-center justify-between">
          <p className="text-sm text-muted-foreground truncate">
            {conversation.lastMessage?.senderId === currentUserId && "Bạn: "}
            {conversation.lastMessage?.content || "Chưa có tin nhắn"}
          </p>
          {conversation.unreadCount > 0 && (
            <Badge className="bg-primary text-primary-foreground ml-2">
              {conversation.unreadCount}
            </Badge>
          )}
        </div>
      </div>
    </div>
  );
};
