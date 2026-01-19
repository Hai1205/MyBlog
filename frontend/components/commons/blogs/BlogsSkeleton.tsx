import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

interface BlogsSkeletonProps {
  showHeader?: boolean;
  showFilters?: boolean;
  cardsCount?: number;
  columns?: {
    sm?: number;
    md?: number;
    lg?: number;
  };
  showActionButtons?: boolean;
  showAuthorInfo?: boolean;
}

export const BlogsSkeleton = ({
  showHeader = true,
  showFilters = false,
  cardsCount = 6,
  columns = { sm: 1, md: 2, lg: 3 },
  showActionButtons = true,
  showAuthorInfo = true,
}: BlogsSkeletonProps) => {
  const gridCols = `grid-cols-${columns.sm || 1} md:grid-cols-${columns.md || 2} lg:grid-cols-${columns.lg || 3}`;

  return (
    <div className="min-h-screen py-12">
      <div className="container max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-8">
          {/* Page Header Skeleton */}
          {showHeader && (
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <Skeleton className="h-10 w-48 mb-2" />
                <Skeleton className="h-5 w-96 max-w-full" />
              </div>
              <Skeleton className="h-10 w-32 rounded-md" />
            </div>
          )}

          {/* Filters Skeleton */}
          {showFilters && (
            <div className="flex flex-wrap items-center gap-4">
              <Skeleton className="h-10 w-32" />
              <Skeleton className="h-10 w-40" />
              <Skeleton className="h-10 w-36" />
            </div>
          )}

          {/* Blog Cards Grid Skeleton */}
          <div className={`grid ${gridCols} gap-6`}>
            {Array.from({ length: cardsCount }).map((_, i) => (
              <Card
                key={i}
                className="group overflow-hidden border-border/50 hover:shadow-xl transition-all duration-300 animate-pulse"
              >
                {/* Thumbnail Skeleton */}
                <div className="relative aspect-4/3 bg-muted">
                  <Skeleton className="absolute inset-0" />
                </div>

                <CardHeader className="space-y-3">
                  {/* Title Skeleton */}
                  <Skeleton className="h-6 w-3/4" />

                  {/* Author Info Skeleton */}
                  {showAuthorInfo && (
                    <div className="flex items-center gap-2">
                      <Skeleton className="h-8 w-8 rounded-full" />
                      <Skeleton className="h-4 w-24" />
                    </div>
                  )}
                </CardHeader>

                <CardContent>
                  {/* Action Buttons Skeleton */}
                  {showActionButtons && (
                    <div className="flex items-center gap-2">
                      <Skeleton className="h-9 flex-1" />
                      <Skeleton className="h-9 w-9" />
                      <Skeleton className="h-9 w-9" />
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Pagination Skeleton */}
          <div className="flex justify-center items-center gap-2 mt-6">
            <Skeleton className="h-9 w-9" />
            <Skeleton className="h-9 w-9" />
            <Skeleton className="h-9 w-9" />
            <Skeleton className="h-9 w-9" />
            <Skeleton className="h-9 w-9" />
          </div>
        </div>
      </div>
    </div>
  );
};
