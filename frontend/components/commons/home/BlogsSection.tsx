"use client";

import { useState, useMemo, useEffect } from "react";
import { BlogCard } from "../blogs/BlogCard";
import { Search } from "lucide-react";
import { usePagination } from "@/hooks/use-pagination";
import { PaginationControls } from "@/components/commons/layout/pagination/PaginationControls";
import { useAllBlogsQuery } from "@/hooks/api/queries/useBlogQueries";
import { BlogsSkeleton } from "../blogs/BlogsSkeleton";
import { categorySelection } from "../admin/blogDashboard/BlogDashboardClient";

const BlogsSection = () => {
  const { data: blogsResponse, isLoading } = useAllBlogsQuery(true);
  const blogs = blogsResponse?.data?.blogs || [];

  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string>("all");

  const categories = [
    { value: "all", label: "All Categories" },
    ...categorySelection,
  ];

  // Pagination
  const { paginationData, paginationState, setPage, updateTotalElements } =
    usePagination({
      initialPage: 1,
      initialPageSize: 8,
    });

  const filteredBlogs = useMemo(() => {
    const filtered = blogs.filter((blog: IBlog) => {
      const matchesSearch =
        searchTerm === "" ||
        blog.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        blog.description.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesCategory =
        selectedCategory === "all" || blog.category === selectedCategory;

      return matchesSearch && matchesCategory;
    });

    return filtered;
  }, [blogs, searchTerm, selectedCategory]);

  // Update pagination total whenever filteredBlogs changes
  useEffect(() => {
    updateTotalElements(filteredBlogs.length);
  }, [filteredBlogs.length, updateTotalElements]);

  // Paginate the filtered blogs
  const paginatedBlogs = useMemo(() => {
    const startIndex = (paginationState.page - 1) * paginationState.pageSize;
    const endIndex = startIndex + paginationState.pageSize;
    return filteredBlogs.slice(startIndex, endIndex);
  }, [filteredBlogs, paginationState.page, paginationState.pageSize]);

  return (
    <div>
      {isLoading ? (
        <BlogsSkeleton />
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
                  {categories.map((category) => (
                    <option key={category.value} value={category.value}>
                      {category.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Results Counter */}
            <div className="text-sm text-gray-600 dark:text-gray-400">
              Showing {filteredBlogs.length} of {blogs.length || 0} blogs
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
              paginatedBlogs.map((blog: IBlog) => (
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

export default BlogsSection;
