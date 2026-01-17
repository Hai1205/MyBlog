import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export default function BlogsSkeleton() {
  return (
    <div className="min-h-screen flex items-center justify-center py-12">
      <div className="container max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-8">
          {/* Page Header Skeleton */}
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div>
              <Skeleton className="h-10 w-48 mb-2" />
              <Skeleton className="h-5 w-96" />
            </div>
            <Skeleton className="h-10 w-32 rounded-md" />
          </div>

          {/* User Blogs Section Skeleton */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <Skeleton className="h-7 w-32" />
              <Skeleton className="h-9 w-24" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3].map((i) => (
                <Card
                  key={i}
                  className="group overflow-hidden border-border/50 hover:shadow-xl transition-all duration-300 animate-pulse"
                >
                  <div className="relative aspect-3/4 bg-muted">
                    <Skeleton className="absolute inset-0" />
                  </div>
                  <CardHeader className="space-y-3">
                    <Skeleton className="h-6 w-3/4" />
                    <Skeleton className="h-4 w-1/2" />
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Skeleton className="h-9 flex-1" />
                      <Skeleton className="h-9 w-9" />
                      <Skeleton className="h-9 w-9" />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>

          {/* Template Blogs Section Skeleton */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <Skeleton className="h-7 w-40" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {[1, 2, 3, 4].map((i) => (
                <Card
                  key={i}
                  className="group overflow-hidden border-border/50 hover:shadow-xl transition-all duration-300 animate-pulse"
                >
                  <div className="relative aspect-3/4 bg-muted">
                    <Skeleton className="absolute inset-0" />
                  </div>
                  <CardHeader className="space-y-3">
                    <Skeleton className="h-5 w-2/3" />
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center gap-2">
                      <Skeleton className="h-9 flex-1" />
                      <Skeleton className="h-9 w-9" />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
