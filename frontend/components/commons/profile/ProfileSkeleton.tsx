export const ProfileSkeleton = () => {
  return (
    <div className="flex flex-col w-full">
      {/* Header Skeleton */}
      <div className="bg-linear-to-b from-primary to-secondary p-6">
        <div className="flex flex-col md:flex-row items-center gap-6">
          {/* Avatar Skeleton */}
          <div className="w-32 h-32 md:w-40 md:h-40 rounded-full bg-gray-800 animate-pulse" />

          <div className="flex-1 text-center md:text-left space-y-4">
            {/* Name Skeleton */}
            <div className="h-8 w-48 bg-gray-800 rounded animate-pulse" />

            {/* Username Skeleton */}
            <div className="h-4 w-32 bg-gray-800 rounded animate-pulse" />

            {/* Bio Skeleton */}
            <div className="h-16 w-full max-w-2xl bg-gray-800 rounded animate-pulse" />

            {/* Stats Skeleton */}
            <div className="flex justify-center md:justify-start gap-4">
              <div className="h-6 w-24 bg-gray-800 rounded animate-pulse" />
              <div className="h-6 w-24 bg-gray-800 rounded animate-pulse" />
            </div>

            {/* Social Links Skeleton */}
            <div className="flex gap-2 justify-center md:justify-start">
              {[1, 2, 3, 4].map((i) => (
                <div
                  key={i}
                  className="h-8 w-8 bg-gray-800 rounded-full animate-pulse"
                />
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Tabs Skeleton */}
      <div className="container mx-auto px-4 py-6">
        <div className="flex border-b border-gray-800 mb-6">
          <div className="h-8 w-24 bg-gray-800 rounded animate-pulse mr-4" />
          <div className="h-8 w-24 bg-gray-800 rounded animate-pulse" />
        </div>

        {/* Content Skeleton */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-48 bg-gray-800 rounded animate-pulse" />
          ))}
        </div>
      </div>
    </div>
  );
}