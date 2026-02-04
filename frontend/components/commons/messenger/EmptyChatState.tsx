// EmptyChatState.tsx
import { Send } from "lucide-react";

export const EmptyChatState = () => {
  return (
    <div className="flex-1 flex items-center justify-center">
      <div className="text-center">
        <div className="mb-4 inline-flex items-center justify-center w-20 h-20 rounded-full bg-primary/10">
          <Send className="w-10 h-10 text-primary" />
        </div>
        <h2 className="text-2xl font-semibold mb-2">Xin chọn cuộc hội thoại</h2>
        <p className="text-muted-foreground">
          Chọn một cuộc hội thoại từ danh sách bên trái để bắt đầu nhắn tin
        </p>
      </div>
    </div>
  );
};
