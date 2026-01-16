"use client";

import { useCallback, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { useBlogStore } from "@/stores/blogStore";
import { toast } from "react-toastify";
import { DashboardHeader } from "@/components/commons/admin/dashboard/DashboardHeader";
import { TableSearch } from "@/components/commons/admin/adminTable/TableSearch";
import ConfirmationDialog from "@/components/commons/layout/ConfirmationDialog";
import TableDashboardSkeleton from "../adminTable/TableDashboardSkeleton";
import { BlogFilter } from "./BlogFilter";
import { BlogTable } from "./BlogTable";

export type BlogFilterType = "category";
export interface IBlogFilter {
  category: string[];
  [key: string]: string[];
}

const blogInitialFilters: IBlogFilter = {
  category: [],
};

export default function BlogDashboardClient() {
  const router = useRouter();
  const {
    blogsTable,
    fetchAllBlogsInBackground,
    deleteBlog,
    getAllBlogs,
  } = useBlogStore();

  const [searchQuery, setSearchQuery] = useState("");

  const [activeFilters, setActiveFilters] =
    useState<IBlogFilter>(blogInitialFilters);
  const [filteredBlogs, setFilteredBlogs] = useState<IBlog[]>([]);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const totalPages = Math.ceil(filteredBlogs.length / pageSize);
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;

  const paginationState = { page: currentPage, pageSize: pageSize };
  const paginationData = {
    totalElements: filteredBlogs.length,
    totalPages: totalPages,
    currentPage: currentPage,
    pageSize: pageSize,
    hasNext: currentPage < totalPages,
    hasPrevious: currentPage > 1,
  };

  const setPage = (page: number) => {
    setCurrentPage(page);
  };

  useEffect(() => {
    // Fetch in background to update cache
    fetchAllBlogsInBackground();
  }, []);

  const filterData = useCallback(
    (query: string, filters: { category: string[] }) => {
      let results = [...blogsTable];

      if (query.trim()) {
        const searchTerms = query.toLowerCase().trim();
        results = results.filter(
          (blog) =>
            blog.title.toLowerCase().includes(searchTerms) ||
            blog.description.toLowerCase().includes(searchTerms)
        );
      }

      if (filters.category.length > 0) {
        results = results.filter((blog) =>
          filters.category.includes(blog.category || "")
        );
      }

      setFilteredBlogs(results);
    },
    [blogsTable]
  );

  useEffect(() => {
    filterData(searchQuery, activeFilters);
  }, [blogsTable, searchQuery, activeFilters, filterData]);

  // Update pagination when filtered blogsTable change
  useEffect(() => {
    paginationData.totalElements = filteredBlogs.length;
    paginationData.totalPages = Math.ceil(
      filteredBlogs.length / paginationState.pageSize
    );
  }, [filteredBlogs.length, paginationState.pageSize]);

  // Paginate filtered blogsTable
  const paginatedBlogs = filteredBlogs.slice(
    (paginationState.page - 1) * paginationState.pageSize,
    paginationState.page * paginationState.pageSize
  );

  const handleSearch = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();

      filterData(searchQuery, activeFilters);
    },
    [searchQuery, activeFilters, filterData]
  );

  const toggleFilter = (value: string, type: BlogFilterType) => {
    setActiveFilters((prev) => {
      const updated = { ...prev };
      if (updated[type]?.includes(value)) {
        updated[type] = updated[type].filter((item) => item !== value);
      } else {
        updated[type] = [...(updated[type] || []), value];
      }
      return updated;
    });
  };

  const clearFilters = () => {
    setActiveFilters(blogInitialFilters);
    setSearchQuery("");
    setFilteredBlogs(blogsTable);
    closeMenuMenuFilters();
  };

  const applyFilters = () => {
    filterData(searchQuery, activeFilters);
    closeMenuMenuFilters();
  };

  const handleRefresh = () => {
    setActiveFilters(blogInitialFilters);
    setSearchQuery("");
    getAllBlogs();
  };

  const [openMenuFilters, setOpenMenuFilters] = useState(false);
  const closeMenuMenuFilters = () => setOpenMenuFilters(false);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [blogToDelete, setBlogToDelete] = useState<IBlog | null>(null);

  const handleUpdate = async (blog: IBlog) => {
    router.push(`/blogs/edit/${blog.id}`);
  };

  const handleCreate = async () => {
    router.push("/blogs/new");
  };

  const handleDelete = (blog: IBlog) => {
    setBlogToDelete(blog);
    setDeleteDialogOpen(true);
  };

  const handleDialogClose = (open: boolean) => {
    if (!open) {
      setDeleteDialogOpen(false);
      setBlogToDelete(null);
    }
  };

  const handleDialogConfirm = async () => {
    if (!blogToDelete) return;

    toast.success("Blog deleted successfully!");
    setDeleteDialogOpen(false);
    setBlogToDelete(null);
    await deleteBlog(blogToDelete.id);
  };

  if (blogsTable === null) {
    return <TableDashboardSkeleton />;
  }

  return (
    <div className="space-y-4">
      <DashboardHeader
        title="Blog Dashboard"
        onCreateClick={handleCreate}
        createButtonText="Create Blog"
      />

      <div className="space-y-4">
        <Card className="border-border/50 shadow-lg bg-linear-to-br from-card to-card/80 backdrop-blur-sm">
          <CardHeader className="pb-4 border-b border-border/30">
            <div className="flex items-center justify-between">
              <CardTitle />

              <div className="flex items-center gap-3">
                <TableSearch
                  handleSearch={handleSearch}
                  searchQuery={searchQuery}
                  setSearchQuery={setSearchQuery}
                  placeholder="Search Blogs..."
                />

                <Button
                  variant="secondary"
                  size="sm"
                  className="h-9 gap-2 px-4 bg-linear-to-br from-secondary/80 to-secondary hover:from-secondary hover:to-secondary/90 shadow-md hover:shadow-lg hover:shadow-secondary/20 transition-all duration-200 hover:scale-105"
                  onClick={async () => {
                    handleRefresh();
                  }}
                >
                  <RefreshCw className="h-4 w-4" />
                  Refresh
                </Button>

                <BlogFilter
                  openMenuFilters={openMenuFilters}
                  setOpenMenuFilters={setOpenMenuFilters}
                  activeFilters={activeFilters}
                  toggleFilter={toggleFilter}
                  clearFilters={clearFilters}
                  applyFilters={applyFilters}
                  closeMenuMenuFilters={closeMenuMenuFilters}
                />
              </div>
            </div>
          </CardHeader>

          <BlogTable
            blogs={paginatedBlogs}
            isLoading={false}
            onUpdate={handleUpdate}
            onDelete={handleDelete}
            showPagination={filteredBlogs.length > 10}
            paginationData={paginationData}
            onPageChange={setPage}
          />
        </Card>
      </div>

      <ConfirmationDialog
        open={deleteDialogOpen}
        onOpenChange={handleDialogClose}
        title={"Delete Blog"}
        description={
          "This action cannot be undone. This will permanently delete the blog and remove it from our servers."
        }
        confirmText={"Delete"}
        cancelText="Cancel"
        isDestructive={true}
        onConfirm={handleDialogConfirm}
      />
    </div>
  );
}
