"use client";

import { useBlogStore } from "@/stores/blogStore";
import BlogCard from "./BlogCard";
import { useEffect } from "react";
import { useAuthStore } from "@/stores/authStore";
import BlogsSkeleton from "./BlogsSkeleton";

const SavedBlogClient = () => {
  const { userAuth } = useAuthStore();
  const {
    fetchSavedBlogsInBackground,
    unsaveBlog,
    savedBlogs,
    isLoadingSavedBlogs,
  } = useBlogStore();

  useEffect(() => {
    if (userAuth) {
      fetchSavedBlogsInBackground(userAuth.id);
      console.log("Fetching saved Blogs for user:", userAuth.id);
    }
  }, [userAuth]);

  const handleUnsave = async (blogId: string) => {
    if (!userAuth) return;
    await unsaveBlog(blogId, userAuth.id);
  };

  if (isLoadingSavedBlogs) {
    return <BlogsSkeleton />;
  }

  return (
    <div className="container mx-auto px-4">
      <h1 className="text-3xl font-bold mt-2">Saved Blogs</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
        {savedBlogs.length > 0 ? (
          savedBlogs.map((blog) => {
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
