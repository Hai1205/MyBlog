"use client";

import { BlogCard } from "./BlogCard";
import { useAuthStore } from "@/stores/authStore";
import { BlogsSkeleton } from "./BlogsSkeleton";
import { useSavedBlogsQuery } from "@/hooks/api/queries/useBlogQueries";
import { useUnsaveBlogMutation } from "@/hooks/api/mutations/useBlogMutations";

const SavedBlogClient = () => {
  const { userAuth } = useAuthStore();

  const { data: savedBlogsResponse, isLoading } = useSavedBlogsQuery(
    userAuth?.id || "",
  );
  const { mutate: unsaveBlog } = useUnsaveBlogMutation();

  const savedBlogs = savedBlogsResponse?.data?.blogs || [];

  const handleUnsave = async (blogId: string) => {
    if (!userAuth) return;
    unsaveBlog({ blogId, userId: userAuth.id });
  };

  if (isLoading) {
    return <BlogsSkeleton />;
  }

  return (
    <div className="container mx-auto px-4">
      <h1 className="text-3xl font-bold mt-2">Saved Blogs</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
        {(savedBlogs.length || 0) > 0 ? (
          savedBlogs.map((blog: IBlog) => {
            return (
              <BlogCard
                key={blog.id}
                blog={blog}
                onUnsave={() => handleUnsave(blog.id)}
              />
            );
          })
        ) : (
          <p>No saved blogs yet!</p>
        )}
      </div>
    </div>
  );
};

export default SavedBlogClient;
