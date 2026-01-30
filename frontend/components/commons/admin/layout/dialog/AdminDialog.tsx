import * as DialogPrimitive from "@radix-ui/react-dialog";
import { X, Save, LucideIcon } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { ReactNode, useState } from "react";

interface AdminDialogProps<T> {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  icon?: LucideIcon;
  children: ReactNode;
  onSubmit: (data?: T) => void | Promise<void>;
  isCreateDialog: boolean;
  className?: string;
  showCloseButton?: boolean;
}

function AdminDialog<T>({
  isOpen,
  onOpenChange,
  title,
  description,
  icon: Icon,
  children,
  onSubmit,
  isCreateDialog,
  className,
  showCloseButton = true,
}: AdminDialogProps<T>) {
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const handleSubmit = async () => {
    setIsLoading(true);
    try {
      await Promise.resolve(onSubmit());
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <DialogPrimitive.Root open={isOpen} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/25 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0" />

        <AnimatePresence>
          {isOpen && (
            <DialogPrimitive.Content
              className={cn(
                "fixed left-[50%] top-[50%] z-50 flex w-full max-w-lg max-h-[90vh] translate-x-[-50%] translate-y-[-50%] bg-linear-to-br from-card to-card/80 backdrop-blur-sm border border-border/50 shadow-2xl rounded-2xl p-3 duration-200",
                className,
              )}
            >
              <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ duration: 0.2, ease: "easeOut" }}
                className="flex flex-col w-full rounded-2xl overflow-hidden"
              >
                {showCloseButton && (
                  <DialogPrimitive.Close className="absolute right-4 top-4 rounded-full p-1 bg-card/30 border border-border/30 text-muted-foreground hover:bg-card/40 hover:shadow-md transition">
                    <X className="h-4 w-4" />
                    <span className="sr-only">Close</span>
                  </DialogPrimitive.Close>
                )}

                {/* Header */}
                <header className="shrink-0 flex flex-col space-y-1 text-center sm:text-left pb-2 border-b border-border/30">
                  <div className="flex items-center gap-3">
                    {Icon && (
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <Icon className="h-5 w-5 text-primary" />
                      </div>
                    )}
                    <div>
                      <DialogPrimitive.Title className="text-lg font-semibold leading-none tracking-tight bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
                        {title}
                      </DialogPrimitive.Title>
                      {description && (
                        <DialogPrimitive.Description className="text-sm text-gray-600 dark:text-gray-400">
                          {description}
                        </DialogPrimitive.Description>
                      )}
                    </div>
                  </div>
                </header>

                {/* Content */}
                <ScrollArea className="flex-1 min-h-0">
                  <div className="py-2">{children}</div>
                </ScrollArea>

                {/* Footer */}
                <footer className="shrink-0 py-2 flex items-center justify-end gap-3 border-t border-border/30 bg-card/30 backdrop-blur-sm">
                  <Button
                    variant="outline"
                    onClick={() => onOpenChange(false)}
                    className="border-border/50 hover:bg-destructive/10 hover:text-destructive hover:border-destructive/50 transition-all duration-200"
                  >
                    Hủy
                  </Button>
                  <Button
                    onClick={handleSubmit}
                    disabled={isLoading}
                    className="bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg hover:shadow-xl hover:shadow-primary/30 transition-all duration-200"
                  >
                    {isLoading ? (
                      <span className="flex items-center gap-2">
                        <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                        Đang {isCreateDialog ? "tạo" : "lưu"}...
                      </span>
                    ) : (
                      <span className="flex items-center gap-2">
                        <Save className="h-4 w-4" />
                        {isCreateDialog ? "Tạo" : "Lưu"}
                      </span>
                    )}
                  </Button>
                </footer>
              </motion.div>
            </DialogPrimitive.Content>
          )}
        </AnimatePresence>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  );
}

export { AdminDialog };
