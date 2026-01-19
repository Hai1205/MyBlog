import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export const ProfileSkeleton = () => {
  return (
    <div className="flex justify-center items-center min-h-screen p-4">
      <Card className="w-full max-w-xl shadow-lg border rounded-2xl p-6">
        <CardHeader className="text-center">
          <Skeleton className="h-8 w-32 mx-auto mb-4" />

          <CardContent className="flex flex-col items-center space-y-4">
            {/* Avatar Skeleton */}
            <Skeleton className="w-28 h-28 rounded-full" />

            {/* Username Skeleton */}
            <div className="w-full space-y-2 text-center">
              <Skeleton className="h-5 w-40 mx-auto" />
            </div>

            {/* Summary Skeleton */}
            <div className="w-full space-y-2 text-center">
              <Skeleton className="h-4 w-24 mx-auto" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-5/6 mx-auto" />
              <Skeleton className="h-4 w-4/6 mx-auto" />
            </div>

            {/* Social Media Icons Skeleton */}
            <div className="flex gap-4 mt-3">
              <Skeleton className="w-6 h-6 rounded" />
              <Skeleton className="w-6 h-6 rounded" />
              <Skeleton className="w-6 h-6 rounded" />
            </div>
          </CardContent>
        </CardHeader>
      </Card>
    </div>
  );
};