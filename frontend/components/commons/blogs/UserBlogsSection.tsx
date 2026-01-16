"use client";

import EmptyState from "@/components/commons/blogs/EmptyState";
import {
  GridPagination,
  PaginationData,
} from "@/components/commons/layout/pagination/GridPagination";
import BlogCard from "./BlogCard";

interface Props {
  userBlogs: IBlog[];
  onCreateNew: () => void;
  onUpdate: (blog: IBlog) => void;
  onDelete: (cvId: string) => void;
  isLoading?: boolean;
  paginationData?: PaginationData;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  showPagination?: boolean;
}

export default function UserBlogsSection({
  userBlogs,
  onCreateNew,
  onUpdate,
  onDelete,
  isLoading = false,
  paginationData,
  onPageChange,
  onPageSizeChange,
  showPagination = false,
}: Props) {
  return (
    <div className="space-y-6">
      {/* Loading state */}
      {isLoading && (
        <div className="flex justify-center items-center min-h-100">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
      )}

      {/* Empty state */}
      {!isLoading && userBlogs.length === 0 && (
        <EmptyState onCreateNew={onCreateNew} />
      )}

      {/* Blog Grid */}
      {!isLoading && userBlogs.length > 0 && (
        <>
          <div className="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {userBlogs.map((blog) => (
              <BlogCard
                key={blog.id}
                blog={blog}
                // onUpdate={onUpdate}
                // onDelete={onDelete}
              />
            ))}
          </div>

          {/* Pagination Controls */}
          {showPagination && paginationData && onPageChange && (
            <GridPagination
              paginationData={paginationData}
              onPageChange={onPageChange}
              onPageSizeChange={onPageSizeChange}
              showPageSizeSelector={!!onPageSizeChange}
              pageSizeOptions={[8, 12, 16, 24]}
            />
          )}
        </>
      )}
    </div>
  );
}
