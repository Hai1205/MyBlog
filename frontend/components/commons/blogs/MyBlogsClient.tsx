"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import ConfirmationDialog from "../layout/ConfirmationDialog";
import { toast } from "react-toastify";
import { useBlogStore } from "@/stores/blogStore";
import UserBlogsSection from "./UserBlogsSection";
import PageHeader from "./PageHeader";
import BlogsSkeleton from "./BlogsSkeleton";

export default function MyBlogsClient() {
  const { userAuth } = useAuthStore();
  const {
    deleteBlog,
    fetchUserBlogsInBackground,
    userBlogs,
    isLoadingUserBlogs,
  } = useBlogStore();

  const router = useRouter();
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [blogToDelete, setCvToDelete] = useState<string | null>(null);

  // Pagination for user Blogs
  const [userBlogsPage, setUserBlogsPage] = useState(1);
  const userBlogsPageSize = 12;
  const userBlogsTotalPages = Math.ceil(userBlogs.length / userBlogsPageSize);
  const userBlogsStartIndex = (userBlogsPage - 1) * userBlogsPageSize;
  const userBlogsEndIndex = userBlogsStartIndex + userBlogsPageSize;

  const userBlogsPagination = {
    paginationState: { page: userBlogsPage, pageSize: userBlogsPageSize },
    paginationData: {
      totalElements: userBlogs.length,
      totalPages: userBlogsTotalPages,
      currentPage: userBlogsPage,
      pageSize: userBlogsPageSize,
      hasNext: userBlogsPage < userBlogsTotalPages,
      hasPrevious: userBlogsPage > 1,
    },
    setPage: setUserBlogsPage,
  };

  // Paginate user Blogs in memory
  const paginatedUserBlogs = userBlogs.slice(
    userBlogsStartIndex,
    userBlogsEndIndex,
  );

  useEffect(() => {
    if (userAuth) {
      fetchUserBlogsInBackground(userAuth.id);
      console.log("Fetching user Blogs for user:", userAuth.id);
    }
  }, [userAuth]);

  const handleCreate = async () => {
    router.push("/blogs/new");
  };

  const handleEdit = (blog: IBlog) => {
    router.push(`/blogs/edit/${blog.id}`);
  };

  const handleDelete = (blogId: string) => {
    setCvToDelete(blogId);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!blogToDelete) return;

    toast.success("Blog deleted successfully!");
    await deleteBlog(blogToDelete);
    setDeleteDialogOpen(false);
    setCvToDelete(null);
  };

  useEffect(() => {
    if (!userAuth) {
      router.push("/auth/login");
    }
  }, [userAuth]);

  // Show loading only when there's no cached data
  if (isLoadingUserBlogs) {
    return <BlogsSkeleton />;
  }

  return (
    <div className="min-h-screen flex items-center justify-center py-12 relative">
      <div className="container max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-8">
          <PageHeader onCreateNew={handleCreate} />

          <UserBlogsSection
            userBlogs={paginatedUserBlogs}
            onCreateNew={handleCreate}
            onUpdate={handleEdit}
            onDelete={handleDelete}
            isLoading={isLoadingUserBlogs}
            showPagination={userBlogs.length > 12}
            paginationData={userBlogsPagination.paginationData}
            onPageChange={(page) => {
              userBlogsPagination.setPage(page);
              window.scrollTo({ top: 0, behavior: "smooth" });
            }}
            // onPageSizeChange={userBlogsPagination.setPageSize}
          />
        </div>
      </div>

      <ConfirmationDialog
        open={deleteDialogOpen}
        description={
          "This action cannot be undone. This will permanently delete your blog and remove it from our servers."
        }
        onOpenChange={setDeleteDialogOpen}
        onConfirm={handleDeleteConfirm}
        isDestructive={true}
      />
    </div>
  );
}
