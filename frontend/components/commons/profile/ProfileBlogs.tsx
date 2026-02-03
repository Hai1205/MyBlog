"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { useUserBlogsQuery } from "@/hooks/api/queries/useBlogQueries";
import {
  useDeleteBlogMutation,
  useDuplicateBlogMutation,
} from "@/hooks/api/mutations/useBlogMutations";
import { useBlogStore } from "@/stores/blogStore";
import { BlogsSkeleton } from "../blogs/BlogsSkeleton";
import { BlogsPageHeader } from "../blogs/BlogsPageHeader";
import { UserBlogsSection } from "../blogs/UserBlogsSection";
import { ConfirmationDialog } from "../layout/ConfirmationDialog";

interface ProfileBlogsProps {
  user: IUser;
  userAuth: IUser;
}

export const ProfileBlogs = ({ user, userAuth }: ProfileBlogsProps) => {
  const { profileBlogs, setBlogToEdit, setProfileBlogs, removeFromProfileBlogs } =
    useBlogStore();

  const { mutate: deleteBlog } = useDeleteBlogMutation();
  const { data: userBlogsResponse, isLoading } = useUserBlogsQuery(
    userAuth?.id || "",
  );
  const { mutateAsync: duplicateBlogAsync, isSuccess } =
    useDuplicateBlogMutation();

  const isMyBlogs = useMemo(() => {
    return user.id === userAuth.id;
  }, [user.id, userAuth.id]);

  useEffect(() => {
    const blogs = userBlogsResponse?.data?.blogs;
    setProfileBlogs(blogs || []);
  }, [userBlogsResponse?.data?.blogs, setProfileBlogs]);

  const router = useRouter();

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [blogToDelete, setBlogToDelete] = useState<string | null>(null);

  // Pagination for user Blogs
  const [userBlogsPage, setUserBlogsPage] = useState(1);
  const userBlogsPageSize = 12;
  const userBlogsTotalPages = Math.ceil(
    (profileBlogs.length || 0) / userBlogsPageSize,
  );
  const userBlogsStartIndex = (userBlogsPage - 1) * userBlogsPageSize;
  const userBlogsEndIndex = userBlogsStartIndex + userBlogsPageSize;
  const userBlogsPagination = {
    paginationState: { page: userBlogsPage, pageSize: userBlogsPageSize },
    paginationData: {
      totalElements: profileBlogs.length || 0,
      totalPages: userBlogsTotalPages,
      currentPage: userBlogsPage,
      pageSize: userBlogsPageSize,
      hasNext: userBlogsPage < userBlogsTotalPages,
      hasPrevious: userBlogsPage > 1,
    },
    setPage: setUserBlogsPage,
  };

  // Paginate user Blogs in memory
  const paginatedUserBlogs = useMemo(() => {
    return (profileBlogs || []).slice(userBlogsStartIndex, userBlogsEndIndex);
  }, [profileBlogs, userBlogsStartIndex, userBlogsEndIndex]);

  const handleCreate = async () => {
    setBlogToEdit(null);
    router.push("/blogs/new");
  };

  const handleEdit = (blog: IBlog) => {
    setBlogToEdit(blog);
    router.push(`/blogs/edit/${blog.id}`);
  };

  const handleDuplicate = async (blogId: string) => {
    const response = await duplicateBlogAsync({
      blogId,
      userId: userAuth?.id || "",
    });

    const blogDuplicated = response?.data?.blog;
    if (isSuccess && blogDuplicated) {
      setBlogToEdit(blogDuplicated);
      router.push(`/blogs/edit/${blogDuplicated.id}`);
    }
  };

  const handleDelete = (blogId: string) => {
    setBlogToDelete(blogId);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!blogToDelete) return;

    deleteBlog({ blogId: blogToDelete });
    removeFromProfileBlogs(blogToDelete);
    setDeleteDialogOpen(false);
    setBlogToDelete(null);
  };

  useEffect(() => {
    if (!userAuth) {
      router.push("/auth/login");
    }
  }, [userAuth, router]);

  // Show loading only when there's no cached data
  if (isLoading) {
    return <BlogsSkeleton />;
  }

  return (
    <div className="min-h-screen flex items-center justify-center py-12 relative">
      <div className="container max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-8">
          <BlogsPageHeader isMyBlogs={isMyBlogs} onCreateNew={handleCreate} />

          <UserBlogsSection
            isMyBlogs={isMyBlogs}
            blogs={paginatedUserBlogs}
            onCreateNew={handleCreate}
            onUpdate={handleEdit}
            onDuplicate={handleDuplicate}
            onDelete={handleDelete}
            isLoading={isLoading}
            showPagination={(profileBlogs.length || 0) > 12}
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
