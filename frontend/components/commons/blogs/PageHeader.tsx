"use client";

import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";

interface PageHeaderProps {
  onCreateNew: () => void;
}

export default function PageHeader({ onCreateNew }: PageHeaderProps) {
  return (
    <div className="flex items-center justify-between">
      <div>
        <h1 className="text-3xl font-bold bg-linear-to-br from-primary to-secondary bg-clip-text text-transparent">
          My Blogs
        </h1>
        <p className="text-muted-foreground">
          Manage all your blogs in one place
        </p>
      </div>
      <div className="flex gap-3">
        <Button
          onClick={onCreateNew}
          className="bg-linear-to-br from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg shadow-primary/30 hover:shadow-xl hover:shadow-primary/40 transition-all duration-200 hover:scale-105"
        >
          <Plus className="mr-2 h-4 w-4" />
          Create New Blog
        </Button>
      </div>
    </div>
  );
}
