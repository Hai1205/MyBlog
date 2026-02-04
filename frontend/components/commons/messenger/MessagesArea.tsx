// MessagesArea.tsx
import { useRef } from "react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { MessageItem } from "./MessageItem";
import { ScrollToBottomButton } from "./ScrollToBottomButton";

interface MessagesAreaProps {
  messages: IMessage[];
  selectedConversation: IConversation;
  currentUserId: string;
  showScrollButton: boolean;
  newMessagePreview: string | null;
  onScroll: (event: React.UIEvent<HTMLDivElement>) => void;
  onScrollToBottom: () => void;
  formatMessageTime: (date: Date) => string;
}

export const MessagesArea = ({
  messages,
  selectedConversation,
  currentUserId,
  showScrollButton,
  newMessagePreview,
  onScroll,
  onScrollToBottom,
  formatMessageTime,
}: MessagesAreaProps) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  return (
    <div className="flex-1 relative">
      <ScrollArea className="h-full p-6" onScrollCapture={onScroll}>
        <div>
          {messages.map((message, index) => {
            const isOwn = message.senderId === currentUserId;
            const showTimestamp =
              index === 0 ||
              new Date(message.createdAt).getTime() -
                new Date(messages[index - 1].createdAt).getTime() >
                300000; // 5 minutes

            return (
              <MessageItem
                key={message.id}
                message={message}
                isOwn={isOwn}
                showTimestamp={showTimestamp}
                selectedConversation={selectedConversation}
                formatMessageTime={formatMessageTime}
              />
            );
          })}
          <div ref={messagesEndRef} data-messages-end />
        </div>
      </ScrollArea>

      <ScrollToBottomButton
        show={showScrollButton}
        newMessagePreview={newMessagePreview}
        onClick={onScrollToBottom}
      />
    </div>
  );
};
