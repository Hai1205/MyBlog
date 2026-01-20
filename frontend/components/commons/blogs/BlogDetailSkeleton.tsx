import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export const BlogDetailSkeleton = () => {
  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* Main Blog Card Skeleton */}
      <Card>
        <CardHeader>
          {/* Title Skeleton */}
          <Skeleton className="h-10 w-3/4 mb-4" />

          {/* Author Section Skeleton */}
          <div className="flex items-center gap-2 mt-2">
            <Skeleton className="w-8 h-8 rounded-full" />
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-10 w-10 rounded-md ml-3" />
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Thumbnail Skeleton */}
          <Skeleton className="w-full h-64 rounded-lg" />

          {/* Description Skeleton */}
          <div className="space-y-2">
            <Skeleton className="h-5 w-full" />
            <Skeleton className="h-5 w-5/6" />
          </div>

          {/* Content Skeleton */}
          <div className="space-y-3 mt-4">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-4/5" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-5/6" />
          </div>
        </CardContent>
      </Card>

      {/* Comment Input Card Skeleton */}
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-4 w-32 mb-2" />
          <div className="flex items-center gap-2">
            <Skeleton className="h-10 flex-1 rounded-md" />
            <Skeleton className="h-10 w-10 rounded-md shrink-0" />
          </div>
        </CardContent>
      </Card>

      {/* Comments List Card Skeleton */}
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-40" />
        </CardHeader>
        <CardContent className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="border-b py-2 flex items-center gap-3">
              <div className="flex-1 space-y-2">
                <div className="flex items-center gap-2">
                  <Skeleton className="w-8 h-8 rounded-full" />
                  <Skeleton className="h-4 w-24" />
                </div>
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-3 w-20" />
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
};