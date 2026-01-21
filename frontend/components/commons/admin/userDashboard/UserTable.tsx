import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { DataTable } from "../adminTable/DataTable";
import { Pencil, Key, Trash2, Eye } from "lucide-react";
import { PaginationData } from "@/components/commons/layout/pagination/PaginationControls";

interface UserTableProps {
  users: IUser[];
  isLoading: boolean;
  onUpdate?: (user: IUser) => void;
  onResetPassword?: (user: IUser) => void;
  onDelete?: (user: IUser) => void;
  onView?: (user: IUser) => void;
  paginationData?: PaginationData;
  onPageChange?: (page: number) => void;
  showPagination?: boolean;
}

const getStatusColor = (status: string) => {
  switch (status) {
    case "active":
      return "bg-green-500";
    case "banned":
      return "bg-red-500";
    case "pending":
      return "bg-yellow-500";
    default:
      return "bg-gray-500";
  }
};

const getRoleColor = (role: string) => {
  switch (role) {
    case "admin":
      return "bg-blue-500";
    case "user":
      return "bg-yellow-500";
    default:
      return "bg-gray-500";
  }
};

export const UserTable = ({
  users,
  isLoading,
  onUpdate,
  onDelete,
  onResetPassword,
  onView,
  paginationData,
  onPageChange,
  showPagination = false,
}: UserTableProps) => {
  const columns = [
    {
      header: "No",
      accessor: (_: IUser, index: number) => {
        // Calculate correct index based on current page
        const baseIndex = paginationData
          ? (paginationData.currentPage - 1) * paginationData.pageSize
          : 0;
        return baseIndex + index + 1;
      },
    },
    {
      header: "User",
      accessor: (user: IUser) => (
        <div className="flex items-center gap-3 justify-center mx-auto">
          {" "}
          <Avatar className="h-9 w-9">
            {user?.avatarUrl && (
              <AvatarImage
                src={user.avatarUrl}
                alt={user?.username || "User"}
              />
            )}

            <AvatarFallback className="bg-linear-to-br from-primary to-secondary text-primary-foreground font-bold text-sm">
              {user?.username?.charAt(0).toUpperCase() || "U"}
            </AvatarFallback>
          </Avatar>
          <div className="flex flex-col items-start">
            {" "}
            <span className="text-sm text-primary">
              {user?.username || "unknown"}
            </span>
            <span className="text-sm text-secondary">
              {user?.email || "unknown"}
            </span>
          </div>
        </div>
      ),
    },
    {
      header: "Role",
      accessor: (user: IUser) => (
        <div className="inline-flex items-center justify-center gap-2">
          <span className={`h-2 w-2 rounded-full ${getRoleColor(user.role)}`} />
          <span className="capitalize">{user.role}</span>
        </div>
      ),
    },
    {
      header: "Status",
      accessor: (user: IUser) => (
        <div className="inline-flex items-center justify-center gap-2">
          <span
            className={`h-2 w-2 rounded-full ${getStatusColor(user.status)}`}
          />
          <span className="capitalize">{user.status}</span>
        </div>
      ),
    },
  ];

  const actions = [];

  if (onView) {
    actions.push({
      label: "View",
      onClick: onView,
      icon: Eye,
    });
  }

  if (onUpdate) {
    actions.push({
      label: "Update",
      onClick: onUpdate,
      icon: Pencil,
    });
  }

  if (onResetPassword) {
    actions.push({
      label: "Reset password",
      onClick: onResetPassword,
      icon: Key,
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
      data={users}
      isLoading={isLoading}
      columns={columns}
      actions={actions}
      emptyMessage="Không tìm thấy người dùng nào"
      showPagination={showPagination}
      paginationData={paginationData}
      onPageChange={onPageChange}
    />
  );
};
