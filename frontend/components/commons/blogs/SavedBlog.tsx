"use client";

import { useBlogStore } from "@/stores/blogStore";
import BlogCard from "./BlogCard";
import Loading from "../layout/Loading";

const SavedBlogClient = () => {
  const { blogs, savedBlogs } = useBlogStore();

  if (!blogs || !savedBlogs) {
    return <Loading />;
  }

  const filteredBlogs = blogs.filter((blog) =>
    savedBlogs.some((saved) => saved.id === blog.id.toString())
  );

  return (
    <div className="container mx-auto px-4">
      <h1 className="text-3xl font-bold mt-2">Saved Blogs</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
        {filteredBlogs.length > 0 ? (
          filteredBlogs.map((blog) => {
            return <BlogCard key={blog.id} blog={blog} />;
          })
        ) : (
          <p>No saved blogs yet!</p>
        )}
      </div>
    </div>
  );
};

export default SavedBlogClient;
