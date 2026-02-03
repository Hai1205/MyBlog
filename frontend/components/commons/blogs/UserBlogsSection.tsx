"use client";

import { BlogEmptyState } from "@/components/commons/blogs/BlogEmptyState";
import {
  GridPagination,
  PaginationData,
} from "@/components/commons/layout/pagination/GridPagination";
import { BlogCard } from "./BlogCard";

interface UserBlogsSectionProps {
  blogs: IBlog[];
  isMyBlogs?: boolean;
  onGoHome?: () => void;
  onUnsave?: (blogId: string) => void;
  onCreateNew?: () => void;
  onUpdate?: (blog: IBlog) => void;
  onDuplicate?: (blogId: string) => Promise<void>;
  onDelete?: (blogId: string) => void;
  isLoading?: boolean;
  paginationData?: PaginationData;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  showPagination?: boolean;
}

export const UserBlogsSection = ({
  blogs,
  isMyBlogs = false,
  onGoHome,
  onUnsave,
  onCreateNew,
  onUpdate,
  onDuplicate,
  onDelete,
  isLoading = false,
  paginationData,
  onPageChange,
  onPageSizeChange,
  showPagination = false,
}: UserBlogsSectionProps) => {
  return (
    <div className="space-y-6">
      {/* Loading state */}
      {isLoading && (
        <div className="flex justify-center items-center min-h-100">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
      )}

      {/* Empty state */}
      {!isLoading &&
        blogs.length === 0 &&
        (isMyBlogs ? (
          <BlogEmptyState isMyBlogs={isMyBlogs} onProcess={onCreateNew!} message="You haven't created any blogs yet. Blogs will appear here once you're created." />
        ) : (
          <BlogEmptyState onProcess={onGoHome!} message="You haven't saved any blogs yet. Blogs will appear here once they're saved." />
        ))}

      {/* Blog Grid */}
      {!isLoading && blogs.length > 0 && (
        <>
          <div className="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {blogs.map((blog) =>
              isMyBlogs ? (
                <BlogCard
                  key={blog.id}
                  blog={blog}
                  onUpdate={onUpdate}
                  onDuplicate={onDuplicate}
                  onDelete={onDelete}
                />
              ) : (
                <BlogCard key={blog.id} blog={blog} onUnsave={onUnsave} />
              ),
            )}
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
};
