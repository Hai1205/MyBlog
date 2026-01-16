import * as React from "react";
import * as DialogPrimitive from "@radix-ui/react-dialog";
import { X, LucideIcon } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { cn } from "@/lib/utils";
import DialogFooter from "./DialogFooter";

interface EnhancedDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  icon?: LucideIcon;
  children: React.ReactNode;
  className?: string;
  showCloseButton?: boolean;
  isLoading: boolean;
  onResolveContact: () => void;
  isDisabled?: boolean;
}

const EnhancedDialog = React.forwardRef<
  React.ElementRef<typeof DialogPrimitive.Content>,
  EnhancedDialogProps
>(
  (
    {
      isOpen,
      onOpenChange,
      title,
      description,
      icon: Icon,
      children,
      className,
      showCloseButton = true,
      isLoading,
      onResolveContact,
      isDisabled,
    },
    ref
  ) => {
    return (
      <DialogPrimitive.Root open={isOpen} onOpenChange={onOpenChange}>
        <DialogPrimitive.Portal>
          <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0" />

          <AnimatePresence>
            {isOpen && (
              <DialogPrimitive.Content
                ref={ref}
                className={cn(
                  "fixed left-[50%] top-[50%] z-50 grid w-full max-w-lg translate-x-[-50%] translate-y-[-50%] gap-4 border border-border bg-background p-6 shadow-lg duration-200 sm:rounded-xl",
                  className
                )}
                asChild
              >
                <motion.div
                  initial={{ opacity: 0, scale: 0.95, y: "-50%", x: "-50%" }}
                  animate={{ opacity: 1, scale: 1, y: "-50%", x: "-50%" }}
                  exit={{ opacity: 0, scale: 0.95, y: "-50%", x: "-50%" }}
                  transition={{ duration: 0.2, ease: "easeOut" }}
                  className="rounded-xl bg-white dark:bg-gray-900"
                >
                  {showCloseButton && (
                    <DialogPrimitive.Close className="absolute right-4 top-4 rounded-sm opacity-70 ring-offset-background transition-opacity hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:pointer-events-none text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
                      <X className="h-4 w-4" />
                      <span className="sr-only">Close</span>
                    </DialogPrimitive.Close>
                  )}

                  <header className="flex flex-col space-y-1.5 text-center sm:text-left">
                    <div className="flex items-center gap-3">
                      {Icon && (
                        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                          <Icon className="h-5 w-5 text-primary" />
                        </div>
                      )}
                      <div>
                        <DialogPrimitive.Title className="text-lg font-semibold leading-none tracking-tight text-gray-900 dark:text-white">
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

                  <div className="py-2">{children}</div>

                  <DialogFooter
                    isLoading={isLoading}
                    isDisabled={isDisabled}
                    onOpenChange={onOpenChange}
                    onResolveContact={onResolveContact}
                  />
                </motion.div>
              </DialogPrimitive.Content>
            )}
          </AnimatePresence>
        </DialogPrimitive.Portal>
      </DialogPrimitive.Root>
    );
  }
);

EnhancedDialog.displayName = "EnhancedDialog";

export { EnhancedDialog };
