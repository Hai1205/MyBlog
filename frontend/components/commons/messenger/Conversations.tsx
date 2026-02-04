// ConversationsList.tsx
import { ScrollArea } from "@/components/ui/scroll-area";
import { ConversationItem } from "./ConversationItem";

interface ConversationsListProps {
  conversations: IConversation[];
  selectedConversation: IConversation | null;
  onSelectConversation: (conversation: IConversation) => void;
  currentUserId: string;
}

export const Conversations = ({
  conversations,
  selectedConversation,
  onSelectConversation,
  currentUserId,
}: ConversationsListProps) => {
  return (
    <ScrollArea className="flex-1">
      {conversations.length === 0 ? (
        <div className="flex items-center justify-center h-full text-muted-foreground">
          Không tìm thấy cuộc hội thoại
        </div>
      ) : (
        conversations.map((conversation) => (
          <ConversationItem
            key={conversation.id}
            conversation={conversation}
            isSelected={selectedConversation?.id === conversation.id}
            onClick={() => onSelectConversation(conversation)}
            currentUserId={currentUserId}
          />
        ))
      )}
    </ScrollArea>
  );
};
