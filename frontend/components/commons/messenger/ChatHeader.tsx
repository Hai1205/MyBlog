// ChatHeader.tsx
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Phone, Video, Info, MoreVertical } from "lucide-react";

interface ChatHeaderProps {
  selectedConversation: IConversation;
}

export const ChatHeader = ({ selectedConversation }: ChatHeaderProps) => {
  return (
    <div className="h-16 border-b border-border flex items-center justify-between px-6">
      <div className="flex items-center gap-3">
        <div className="relative">
          <Avatar>
            <AvatarImage src={selectedConversation.participant.avatarUrl} />
            <AvatarFallback className="bg-primary text-primary-foreground">
              {selectedConversation.participant.username
                .charAt(0)
                .toUpperCase()}
            </AvatarFallback>
          </Avatar>
          {selectedConversation.participant.isOnline && (
            <span className="absolute bottom-0 right-0 w-3 h-3 bg-secondary border-2 border-background rounded-full" />
          )}
        </div>
        <div>
          <h2 className="font-semibold">
            {selectedConversation.participant.username}
          </h2>
          <p className="text-xs text-muted-foreground">
            {selectedConversation.participant.isOnline
              ? "Đang hoạt động"
              : "Không hoạt động"}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <Button variant="ghost" size="icon">
          <Phone className="w-5 h-5" />
        </Button>
        <Button variant="ghost" size="icon">
          <Video className="w-5 h-5" />
        </Button>
        <Button variant="ghost" size="icon">
          <Info className="w-5 h-5" />
        </Button>
        <Button variant="ghost" size="icon">
          <MoreVertical className="w-5 h-5" />
        </Button>
      </div>
    </div>
  );
};
