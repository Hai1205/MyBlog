// ScrollToBottomButton.tsx
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ArrowDown } from "lucide-react";

interface ScrollToBottomButtonProps {
  show: boolean;
  newMessagePreview: string | null;
  onClick: () => void;
}

export const ScrollToBottomButton = ({
  show,
  newMessagePreview,
  onClick,
}: ScrollToBottomButtonProps) => {
  if (!show) return null;

  return (
    <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2">
      {newMessagePreview ? (
        <Card
          className="px-4 py-2 cursor-pointer shadow-lg hover:shadow-xl transition-shadow bg-card border-2 border-primary"
          onClick={onClick}
        >
          <div className="flex items-center gap-2">
            <div className="flex-1">
              <p className="text-sm font-medium mb-1">Tin nhắn mới</p>
              <p className="text-xs text-muted-foreground">
                {newMessagePreview}
              </p>
            </div>
            <ArrowDown className="w-5 h-5 text-primary" />
          </div>
        </Card>
      ) : (
        <Button
          size="icon"
          className="rounded-full shadow-lg hover:shadow-xl transition-shadow"
          onClick={onClick}
        >
          <ArrowDown className="w-5 h-5" />
        </Button>
      )}
    </div>
  );
};