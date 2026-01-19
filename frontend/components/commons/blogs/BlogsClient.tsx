"use client";

import { useState, useMemo, useEffect } from "react";
import Loading from "../layout/Loading";
import { useBlogStore } from "@/stores/blogStore";
import BlogCard from "./BlogCard";
import { Search } from "lucide-react";
import { usePagination } from "@/hooks/use-pagination";
import { PaginationControls } from "@/components/commons/layout/pagination/PaginationControls";
import { blogCategories } from "../admin/blogDashboard/constant";

const BlogsClient = () => {
  const { isLoading, blogs, fetchAllBlogsInBackground } = useBlogStore();
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string>("all");

  console.log("BlogsClient render: ", { blogs });
  useEffect(() => {
    // Fetch in background to update cache
    fetchAllBlogsInBackground();
  }, []);

  // Pagination
  const { paginationData, paginationState, setPage, updateTotalElements } =
    usePagination({
      initialPage: 1,
      initialPageSize: 8,
    });

  // Filter blogs based on search term and category
  const filteredBlogs = useMemo(() => {
    const blogList = blogs || [];
    const filtered = blogList.filter((blog) => {
      const matchesSearch =
        searchTerm === "" ||
        blog.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        blog.description.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesCategory =
        selectedCategory === "all" || blog.category === selectedCategory;

      const isVisible = blog.isVisibility === true;

      return matchesSearch && matchesCategory && isVisible;
    });

    // Update total elements for pagination
    updateTotalElements(filtered.length);

    return filtered;
  }, [blogs, searchTerm, selectedCategory, updateTotalElements]);

  // Paginate the filtered blogs
  const paginatedBlogs = useMemo(() => {
    const startIndex = (paginationState.page - 1) * paginationState.pageSize;
    const endIndex = startIndex + paginationState.pageSize;
    return filteredBlogs.slice(startIndex, endIndex);
  }, [filteredBlogs, paginationState.page, paginationState.pageSize]);

  return (
    <div>
      {isLoading ? (
        <Loading />
      ) : (
        <div className="container mx-auto px-4">
          {/* Filter Section */}
          <div className="mb-8 space-y-4">
            <div className="flex flex-col md:flex-row gap-4">
              {/* Search Input */}
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-5 w-5" />
                <input
                  type="text"
                  placeholder="Search blogs by title or description..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-card text-card-foreground"
                />
              </div>

              {/* Category Filter */}
              <div className="md:w-64">
                <select
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                  className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-card text-card-foreground"
                >
                  {blogCategories.map((category) => (
                    <option key={category.value} value={category.value}>
                      {category.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Results Counter */}
            <div className="text-sm text-gray-600 dark:text-gray-400">
              Showing {filteredBlogs.length} of {blogs?.length || 0} blogs
            </div>
          </div>

          {/* Blogs Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3 mb-8">
            {paginatedBlogs.length === 0 ? (
              <div className="col-span-full text-center py-12">
                <p className="text-gray-500 dark:text-gray-400 text-lg">
                  No blogs found matching your criteria
                </p>
              </div>
            ) : (
              paginatedBlogs.map((blog) => (
                <BlogCard key={blog.id} blog={blog} />
              ))
            )}
          </div>

          {/* Pagination */}
          {filteredBlogs.length > 0 && (
            <div className="mb-16">
              <PaginationControls
                paginationData={paginationData}
                onPageChange={(page) => {
                  setPage(page);
                  window.scrollTo({ top: 0, behavior: "smooth" });
                }}
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default BlogsClient;
