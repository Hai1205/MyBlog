import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { DataTable } from "../adminTable/DataTable";
import { Pencil, Key, Trash2 } from "lucide-react";
import { PaginationData } from "@/components/commons/layout/pagination/PaginationControls";

interface BlogTableProps {
  blogs: IBlog[];
  isLoading: boolean;
  onUpdate?: (blog: IBlog) => void;
  onDelete?: (blog: IBlog) => void;
  paginationData?: PaginationData;
  onPageChange?: (page: number) => void;
  showPagination?: boolean;
}

const getCategoryColor = (category: string) => {
  switch (category) {
    case "technology":
      return "bg-blue-500";
    case "health":
      return "bg-green-500";
    case "finance":
      return "bg-emerald-500";
    case "travel":
      return "bg-purple-500";
    case "education":
      return "bg-orange-500";
    case "entertainment":
      return "bg-pink-500";
    case "study":
      return "bg-indigo-500";
    default:
      return "bg-gray-500";
  }
};

export const BlogTable = ({
  blogs,
  isLoading,
  onUpdate,
  onDelete,
  paginationData,
  onPageChange,
  showPagination = false,
}: BlogTableProps) => {
  const columns = [
    {
      header: "No",
      accessor: (_: IBlog, index: number) => {
        const baseIndex = paginationData
          ? (paginationData.currentPage - 1) * paginationData.pageSize
          : 0;
        return baseIndex + index + 1;
      },
    },
    {
      header: "Title",
      accessor: (blog: IBlog) => (
        <div className="inline-flex items-center justify-center gap-2">
          <span className="capitalize">{blog.title}</span>
        </div>
      ),
    },
    {
      header: "Description",
      accessor: (blog: IBlog) => (
        <div className="inline-flex items-center justify-center gap-2">
          <span className="capitalize">{blog.description}</span>
        </div>
      ),
    },
    {
      header: "Category",
      accessor: (blog: IBlog) => (
        <div className="inline-flex items-center justify-center gap-2">
          <span
            className={`h-2 w-2 rounded-full ${getCategoryColor(blog.category)}`}
          />
          <span className="capitalize">{blog.category}</span>
        </div>
      ),
    },
  ];

  const actions = [];

  if (onUpdate) {
    actions.push({
      label: "Update",
      onClick: onUpdate,
      icon: Pencil,
    });
  }

  if (onDelete) {
    actions.push({
      label: "Delete",
      onClick: onDelete,
      icon: Trash2,
      className: "hover:bg-destructive/10 hover:text-destructive",
    });
  }

  return (
    <DataTable
      data={blogs}
      isLoading={isLoading}
      columns={columns}
      actions={actions}
      emptyMessage="No blogs found"
      showPagination={showPagination}
      paginationData={paginationData}
      onPageChange={onPageChange}
    />
  );
};
