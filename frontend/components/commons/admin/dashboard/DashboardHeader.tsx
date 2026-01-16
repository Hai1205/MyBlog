import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";

interface DashboardHeaderProps {
  title: string;
  onCreateClick?: () => void;
  createButtonText?: string;
  children?: React.ReactNode;
}

export const DashboardHeader = ({
  title,
  onCreateClick,
  createButtonText = "Create",
  children,
}: DashboardHeaderProps) => {
  return (
    <div className="flex items-center justify-between pb-6 border-b border-border/50 mb-6">
      <div className="space-y-1">
        <h2 className="text-3xl font-bold tracking-tight bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
          {title}
        </h2>
        <p className="text-sm text-muted-foreground">
          Quản lý và theo dõi dữ liệu của bạn
        </p>
      </div>

      <div className="flex items-center gap-3">
        {children}
        {onCreateClick && (
          <Button
            size="sm"
            className="h-9 gap-2 px-4 bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg shadow-primary/30 transition-all duration-200 hover:shadow-xl hover:shadow-primary/40 hover:scale-105"
            onClick={onCreateClick}
          >
            <Plus className="h-4 w-4" />
            {createButtonText}
          </Button>
        )}
      </div>
    </div>
  );
};
