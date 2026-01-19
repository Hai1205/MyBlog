import { Card, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export const TableDashboardSkeleton = () => {
  return (
    <div className="space-y-4">
      {/* Header Skeleton */}
      <div className="flex items-center justify-between">
        <Skeleton className="h-9 w-52" />
        <Skeleton className="h-10 w-32 rounded-md" />
      </div>

      {/* Table Card Skeleton */}
      <Card className="border-border/50 shadow-lg bg-linear-to-br from-card to-card/80 backdrop-blur-sm">
        <CardHeader className="pb-4 border-b border-border/30">
          <div className="flex items-center justify-between">
            <Skeleton className="h-6 w-32" />

            <div className="flex items-center gap-3">
              {/* Search Skeleton */}
              <Skeleton className="h-9 w-64 rounded-md" />

              {/* Refresh Button Skeleton */}
              <Skeleton className="h-9 w-24 rounded-md" />

              {/* Filter Button Skeleton */}
              <Skeleton className="h-9 w-20 rounded-md" />
            </div>
          </div>
        </CardHeader>

        {/* Table Skeleton */}
        <div className="p-6">
          {/* Table Header */}
          <div className="grid grid-cols-5 gap-4 pb-4 border-b border-border/30 mb-4">
            {[1, 2, 3, 4, 5].map((i) => (
              <Skeleton key={i} className="h-4 w-full" />
            ))}
          </div>

          {/* Table Rows */}
          <div className="space-y-3">
            {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
              <div
                key={i}
                className="grid grid-cols-5 gap-4 py-3 border-b border-border/10 animate-pulse"
              >
                <div className="flex items-center gap-3">
                  <Skeleton className="h-10 w-10 rounded-full" />
                  <Skeleton className="h-4 w-32" />
                </div>
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-20" />
                <Skeleton className="h-4 w-24" />
                <div className="flex items-center gap-2">
                  <Skeleton className="h-8 w-8 rounded" />
                  <Skeleton className="h-8 w-8 rounded" />
                  <Skeleton className="h-8 w-8 rounded" />
                </div>
              </div>
            ))}
          </div>

          {/* Pagination Skeleton */}
          <div className="flex items-center justify-between mt-6 pt-4 border-t border-border/30">
            <Skeleton className="h-4 w-40" />
            <div className="flex items-center gap-2">
              <Skeleton className="h-9 w-24 rounded-md" />
              <Skeleton className="h-9 w-9 rounded-md" />
              <Skeleton className="h-9 w-9 rounded-md" />
              <Skeleton className="h-9 w-24 rounded-md" />
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};
