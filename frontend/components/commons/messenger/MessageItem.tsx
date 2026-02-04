// MessageItem.tsx
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { cn } from "@/lib/utils";

interface MessageItemProps {
  message: IMessage;
  isOwn: boolean;
  showTimestamp: boolean;
  selectedConversation: IConversation;
  formatMessageTime: (date: Date) => string;
}

export const MessageItem = ({
  message,
  isOwn,
  showTimestamp,
  selectedConversation,
  formatMessageTime,
}: MessageItemProps) => {
  return (
    <div className="mb-4">
      {showTimestamp && (
        <div className="flex justify-center mb-4">
          <span className="text-xs text-muted-foreground bg-muted px-3 py-1 rounded-full">
            {formatMessageTime(new Date(message.createdAt))}
          </span>
        </div>
      )}
      <div className={cn("flex", isOwn ? "justify-end" : "justify-start")}>
        {!isOwn && (
          <Avatar className="w-8 h-8 mr-2">
            <AvatarImage src={selectedConversation.participant.avatarUrl} />
            <AvatarFallback className="bg-primary text-primary-foreground text-xs">
              {selectedConversation.participant.username
                .charAt(0)
                .toUpperCase()}
            </AvatarFallback>
          </Avatar>
        )}
        <div
          className={cn(
            "max-w-[70%] rounded-2xl px-4 py-2",
            isOwn
              ? "bg-primary text-primary-foreground"
              : "bg-secondary text-secondary-foreground",
          )}
        >
          <p className="text-sm wrap-break-word">{message.content}</p>
        </div>
      </div>
    </div>
  );
};