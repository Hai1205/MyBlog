import { Button } from "@/components/ui/button";
import { CheckCircle } from "lucide-react";

interface IContactFooterProps {
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onResolveContact: () => void;
  isDisabled?: boolean;
}

export default function DialogFooter({
  isLoading,
  onOpenChange,
  onResolveContact,
  isDisabled,
}: IContactFooterProps) {
  return (
    <footer className="flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2">
      <div className="flex justify-between items-center w-full">
        <Button
          variant="outline"
          onClick={() => onOpenChange(false)}
          className="border-gray-300 text-gray-700 hover:bg-gray-100 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-700"
        >
          Đóng
        </Button>

        <Button
          variant="default"
          onClick={onResolveContact}
          className="bg-green-600 hover:bg-green-700 text-white shadow-md hover:shadow-lg transition-all"
          disabled={
            isLoading || isDisabled
          }
        >
          {isLoading ? (
            <span className="flex items-center gap-2">
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"></div>
              Đang xử lý...
            </span>
          ) : (
            <span className="flex items-center gap-2">
              <CheckCircle className="h-4 w-4" />
              Đánh dấu đã xử lý
            </span>
          )}
        </Button>
      </div>
    </footer>
  )
}
