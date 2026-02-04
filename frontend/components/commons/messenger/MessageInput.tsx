// MessageInput.tsx
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Send, Paperclip, Smile } from "lucide-react";

interface MessageInputProps {
  messageInput: string;
  onMessageChange: (value: string) => void;
  onSendMessage: () => void;
  onSimulateNewMessage: () => void;
}

export const MessageInput = ({
  messageInput,
  onMessageChange,
  onSendMessage,
  onSimulateNewMessage,
}: MessageInputProps) => {
  return (
    <div className="border-t border-border p-4">
      <div className="flex items-center gap-2">
        <Button variant="ghost" size="icon" className="shrink-0">
          <Paperclip className="w-5 h-5" />
        </Button>
        <Button variant="ghost" size="icon" className="shrink-0">
          <Smile className="w-5 h-5" />
        </Button>
        <Input
          placeholder="Nhập tin nhắn..."
          value={messageInput}
          onChange={(e) => onMessageChange(e.target.value)}
          onKeyPress={(e) => e.key === "Enter" && onSendMessage()}
          className="flex-1"
        />
        <Button onClick={onSendMessage} className="shrink-0">
          <Send className="w-5 h-5" />
        </Button>
      </div>

      {/* Demo Button to Simulate New Message */}
      <div className="mt-2 flex justify-center">
        <Button
          variant="outline"
          size="sm"
          onClick={onSimulateNewMessage}
          className="text-xs"
        >
          Demo: Nhận tin nhắn mới
        </Button>
      </div>
    </div>
  );
};