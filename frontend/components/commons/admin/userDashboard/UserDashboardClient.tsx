"use client";

import { useCallback, useState, useEffect } from "react";
import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { EUserRole, EUserStatus } from "@/types/enum";
import { useUserStore } from "@/stores/userStore";
import { useAuthStore } from "@/stores/authStore";
import { toast } from "react-toastify";
import { DashboardHeader } from "@/components/commons/admin/dashboard/DashboardHeader";
import CreateUserDialog from "@/components/commons/admin/userDashboard/CreateUserDialog";
import UpdateUserDialog from "@/components/commons/admin/userDashboard/UpdateUserDialog";
import { TableSearch } from "@/components/commons/admin/adminTable/TableSearch";
import { UserFilter } from "@/components/commons/admin/userDashboard/UserFilter";
import { UserTable } from "@/components/commons/admin/userDashboard/UserTable";
import { ExtendedUserData } from "@/components/commons/admin/userDashboard/constant";
import ConfirmationDialog from "@/components/commons/layout/ConfirmationDialog";
import TableDashboardSkeleton from "../adminTable/TableDashboardSkeleton";
import { useRouter } from "next/navigation";

export type UserFilterType = "status" | "role" | "plan";
export interface IUserFilter {
  status: string[];
  role: string[];
  plan: string[];
  [key: string]: string[];
}

const userInitialFilters: IUserFilter = {
  status: [],
  role: [],
  plan: [],
};

export default function UserDashboardClient() {
  const {
    usersTable,
    fetchAllUsersInBackground,
    createUser,
    updateUser,
    deleteUser,
    getAllUsers,
  } = useUserStore();
  const { resetPassword } = useAuthStore();

  const router = useRouter();

  const [searchQuery, setSearchQuery] = useState("");
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [previewAvatar, setPreviewAvatar] = useState<string>("");
  const [isCreateUserOpen, setIsCreateUserOpen] = useState(false);
  const [isUpdateUserOpen, setIsUpdateUserOpen] = useState(false);

  const [activeFilters, setActiveFilters] =
    useState<IUserFilter>(userInitialFilters);
  const [filteredUsers, setFilteredUsers] = useState<IUser[]>([]);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;
  const totalPages = Math.ceil(filteredUsers.length / pageSize);
  // const startIndex = (currentPage - 1) * pageSize;
  // const endIndex = startIndex + pageSize;

  const paginationState = { page: currentPage, pageSize: pageSize };
  const paginationData = {
    totalElements: filteredUsers.length,
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
    fetchAllUsersInBackground();
  }, []);

  const filterData = useCallback(
    (query: string, filters: { status: string[]; role: string[] }) => {
      let results = [...usersTable];

      if (query.trim()) {
        const searchTerms = query.toLowerCase().trim();
        results = results.filter(
          (user) =>
            user.email.toLowerCase().includes(searchTerms) ||
            user.username.toLowerCase().includes(searchTerms),
        );
      }

      if (filters.status.length > 0) {
        results = results.filter((user) =>
          filters.status.includes(user.status || ""),
        );
      }

      if (filters.role.length > 0) {
        results = results.filter((user) =>
          filters.role.includes(user.role || ""),
        );
      }

      setFilteredUsers(results);
    },
    [usersTable],
  );

  useEffect(() => {
    filterData(searchQuery, activeFilters);
  }, [usersTable, searchQuery, activeFilters, filterData]);

  // Update pagination when filtered usersTable change
  useEffect(() => {
    paginationData.totalElements = filteredUsers.length;
    paginationData.totalPages = Math.ceil(
      filteredUsers.length / paginationState.pageSize,
    );
  }, [filteredUsers.length, paginationState.pageSize]);

  // Paginate filtered usersTable
  const paginatedUsers = filteredUsers.slice(
    (paginationState.page - 1) * paginationState.pageSize,
    paginationState.page * paginationState.pageSize,
  );

  const handleSearch = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();

      filterData(searchQuery, activeFilters);
    },
    [searchQuery, activeFilters, filterData],
  );

  const toggleFilter = (value: string, type: UserFilterType) => {
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
    setActiveFilters(userInitialFilters);
    setSearchQuery("");
    setFilteredUsers(usersTable);
    closeMenuMenuFilters();
  };

  const applyFilters = () => {
    filterData(searchQuery, activeFilters);
    closeMenuMenuFilters();
  };

  const handleRefresh = () => {
    setActiveFilters(userInitialFilters);
    setSearchQuery("");
    getAllUsers();
  };

  const [openMenuFilters, setOpenMenuFilters] = useState(false);
  const closeMenuMenuFilters = () => setOpenMenuFilters(false);

  const [dialogKey, setDialogKey] = useState(0);

  const [data, setData] = useState<ExtendedUserData | null>(null);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<IUser | null>(null);

  const [resetPasswordDialogOpen, setResetPasswordDialogOpen] = useState(false);
  const [userToResetPassword, setUserToResetPassword] = useState<IUser | null>(
    null,
  );

  // Combined dialog state
  const isDeleteDialog = deleteDialogOpen;

  const defaultUser: ExtendedUserData = {
    id: "",
    username: "",
    email: "",
    password: "",
    role: EUserRole.USER,
    status: EUserStatus.PENDING,
    planExpiration: "",
    instagram: "",
    facebook: "",
    linkedin: "",
    createdAt: "",
    updatedAt: "",
  };

  const handleChange = (
    field: keyof ExtendedUserData,
    value: string | string[] | boolean,
  ) => {
    setData((prev) => {
      if (!prev) {
        return { ...defaultUser, [field]: value } as ExtendedUserData;
      }

      return { ...prev, [field]: value };
    });
  };

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setAvatarFile(file);
      setPreviewAvatar(URL.createObjectURL(file));
    }
  };

  const handleUpdate = async () => {
    if (!data) {
      return;
    }

    const res = await updateUser(
      data.id,
      data.birth || "",
      data.summary || "",
      avatarFile || null,
      data.role,
      data.status,
      data.instagram || "",
      data.facebook || "",
      data.linkedin || "",
    );

    if (res?.data?.success) {
      toast.success("User updated successfully");
    } else {
      toast.error("Failed to update user");
    }

    setIsUpdateUserOpen(false);
  };

  const handleCreate = async () => {
    if (!data) {
      return;
    }

    const res = await createUser(
      data.email,
      data.password || "",
      data.username,
      data.birth || "",
      data.summary || "",
      avatarFile || null,
      data.role,
      data.status,
      data.instagram || "",
      data.facebook || "",
      data.linkedin || "",
    );

    if (res?.data?.success) {
      toast.success("User created successfully");
    } else {
      toast.error("Failed to create user");
    }

    setIsCreateUserOpen(false);
  };

  const onDelete = (user: IUser) => {
    setUserToDelete(user);
    setDeleteDialogOpen(true);
  };

  const onResetPassword = async (user: IUser) => {
    setUserToResetPassword(user);
    setResetPasswordDialogOpen(true);
  };

  const onView = async (user: IUser) => {
    router.push(`/profile/${user.id}`);
  };

  const handleDialogClose = (open: boolean) => {
    if (!open) {
      setDeleteDialogOpen(false);
      setResetPasswordDialogOpen(false);
      setUserToDelete(null);
      setUserToResetPassword(null);
    }
  };

  const handleDialogConfirm = async () => {
    if (isDeleteDialog && userToDelete) {
      toast.success("Xóa người dùng thành công!");
      setDeleteDialogOpen(false);
      setUserToDelete(null);
      await deleteUser(userToDelete.id);
    } else if (!isDeleteDialog && userToResetPassword) {
      toast.success("Đã gửi mật khẩu mới về email của người dùng!");
      setResetPasswordDialogOpen(false);
      setUserToResetPassword(null);
      await resetPassword(userToResetPassword.email);
    }
  };

  const onUpdate = async (user: IUser) => {
    setData(user);
    setIsUpdateUserOpen(true);
  };

  if (usersTable === null) {
    return <TableDashboardSkeleton />;
  }

  return (
    <div className="space-y-4">
      <DashboardHeader
        title="User Dashboard"
        onCreateClick={() => {
          setData(defaultUser);
          setIsCreateUserOpen(true);
        }}
        createButtonText="Create User"
      />

      {/* Use consistent key to avoid hydration issues */}
      <CreateUserDialog
        key={`create-${dialogKey}-${isCreateUserOpen ? "open" : "closed"}`}
        isOpen={isCreateUserOpen}
        onOpenChange={(open) => {
          setIsCreateUserOpen(open);
          if (!open) {
            setData(null);
            setDialogKey((prev) => prev + 1);
          }
        }}
        onChange={handleChange}
        onUserCreated={handleCreate}
        data={data}
        previewAvatar={previewAvatar}
        handleAvatarChange={handleAvatarChange}
      />

      <UpdateUserDialog
        key={`update-${dialogKey}-${isUpdateUserOpen ? "open" : "closed"}`}
        isOpen={isUpdateUserOpen}
        onOpenChange={(open) => {
          setIsUpdateUserOpen(open);
          if (!open) {
            setData(null);
            setDialogKey((prev) => prev + 1);
          }
        }}
        onChange={handleChange}
        data={data}
        onUserUpdated={handleUpdate}
        previewAvatar={previewAvatar}
        handleAvatarChange={handleAvatarChange}
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
                  placeholder="Search Users..."
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

                <UserFilter
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

          <UserTable
            users={paginatedUsers}
            isLoading={false}
            onUpdate={onUpdate}
            onDelete={onDelete}
            onResetPassword={onResetPassword}
            onView={onView}
            showPagination={filteredUsers.length > 10}
            paginationData={paginationData}
            onPageChange={setPage}
          />
        </Card>
      </div>

      <ConfirmationDialog
        open={deleteDialogOpen || resetPasswordDialogOpen}
        onOpenChange={handleDialogClose}
        title={isDeleteDialog ? "Delete User" : "Đặt lại mật khẩu"}
        description={
          isDeleteDialog
            ? "Hành động này không thể hoàn tác. Điều này sẽ xóa vĩnh viễn người dùng và loại bỏ nó khỏi máy chủ của chúng tôi."
            : `Bạn có muốn gửi email đặt lại mật khẩu cho ${
                userToResetPassword?.email
              }?`
        }
        confirmText={isDeleteDialog ? "Delete" : "Gửi email"}
        cancelText="Hủy"
        isDestructive={isDeleteDialog}
        onConfirm={handleDialogConfirm}
      />
    </div>
  );
}
