export function TableSkeleton() {
  return (
    <div className="w-full animate-pulse">
      {[1, 2, 3, 4, 5].map((index) => (
        <div
          key={index}
          className="flex items-center space-x-4 py-4 border-b border-gray-200"
        >
          <div className="h-4 w-4 bg-gray-200 rounded" />
          <div className="h-8 w-8 bg-gray-200 rounded-full" />
          <div className="h-9 w-9 bg-gray-200 rounded-md" />
          <div className="flex-1 space-y-2">
            <div className="h-4 w-1/4 bg-gray-200 rounded" />
            <div className="h-3 w-1/2 bg-gray-200 rounded" />
          </div>
          <div className="h-4 w-20 bg-gray-200 rounded" />
          <div className="h-4 w-24 bg-gray-200 rounded" />
          <div className="h-8 w-8 bg-gray-200 rounded" />
        </div>
      ))}
    </div>
  );
}
