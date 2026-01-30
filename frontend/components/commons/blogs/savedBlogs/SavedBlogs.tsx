"use client";

import { useAuthStore } from "@/stores/authStore";
import { useSavedBlogsQuery } from "@/hooks/api/queries/useBlogQueries";
import { useUnsaveBlogMutation } from "@/hooks/api/mutations/useBlogMutations";
import { BlogsSkeleton } from "../BlogsSkeleton";
import { BlogsPageHeader } from "../BlogsPageHeader";
import { UserBlogsSection } from "../UserBlogsSection";
import { useRouter } from "next/dist/client/components/navigation";
import { useEffect, useMemo, useState } from "react";
import { useBlogStore } from "@/stores/blogStore";

const SavedBlogsClient = () => {
  const { userAuth } = useAuthStore();
  const { savedBlogs, removeFromSavedBlogs, setSavedBlogs } = useBlogStore();

  const { data: savedBlogsResponse, isLoading } = useSavedBlogsQuery(
    userAuth?.id || "",
  );
  const { mutate: unsaveBlog } = useUnsaveBlogMutation();

  useEffect(() => {
    const blogs = savedBlogsResponse?.data?.blogs;
    setSavedBlogs(blogs || []);
  }, [savedBlogsResponse?.data?.blogs, setSavedBlogs]);

  const [savedBlogsPage, setSavedBlogsPage] = useState(1);
  const savedBlogsPageSize = 12;
  const savedBlogsTotalPages = Math.ceil(
    (savedBlogs.length || 0) / savedBlogsPageSize,
  );
  const savedBlogsStartIndex = (savedBlogsPage - 1) * savedBlogsPageSize;
  const savedBlogsEndIndex = savedBlogsStartIndex + savedBlogsPageSize;
  const savedBlogsPagination = {
    paginationState: { page: savedBlogsPage, pageSize: savedBlogsPageSize },
    paginationData: {
      totalElements: savedBlogs.length || 0,
      totalPages: savedBlogsTotalPages,
      currentPage: savedBlogsPage,
      pageSize: savedBlogsPageSize,
      hasNext: savedBlogsPage < savedBlogsTotalPages,
      hasPrevious: savedBlogsPage > 1,
    },
    setPage: setSavedBlogsPage,
  };
  const paginatedSavedBlogs = useMemo(() => {
    return (savedBlogs || []).slice(savedBlogsStartIndex, savedBlogsEndIndex);
  }, [savedBlogs, savedBlogsStartIndex, savedBlogsEndIndex]);

  const router = useRouter();

  const handleUnsave = async (blogId: string) => {
    if (!userAuth) return;
    unsaveBlog({ blogId, userId: userAuth.id });
    removeFromSavedBlogs(blogId);
  };

  const handleGoHome = async () => {
    router.push("/");
  };

  if (isLoading) {
    return <BlogsSkeleton />;
  }

  return (
    <div className="min-h-screen flex items-center justify-center py-12 relative">
      <div className="container max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-8">
          <BlogsPageHeader />

          <UserBlogsSection
            blogs={paginatedSavedBlogs}
            onGoHome={handleGoHome}
            onUnsave={handleUnsave}
            isLoading={isLoading}
            showPagination={(savedBlogs.length || 0) > 12}
            paginationData={savedBlogsPagination.paginationData}
            onPageChange={(page) => {
              savedBlogsPagination.setPage(page);
              window.scrollTo({ top: 0, behavior: "smooth" });
            }}
            // onPageSizeChange={savedBlogsPagination.setPageSize}
          />
        </div>
      </div>
    </div>
  );
};

export default SavedBlogsClient;
