"use client";

import { useCallback, useState, useEffect } from "react";
import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { EUserRole, EUserStatus } from "@/types/enum";
import { useAuthStore } from "@/stores/authStore";
import { toast } from "react-toastify";
import { DashboardHeader } from "@/components/commons/admin/dashboard/DashboardHeader";
import { CreateUserDialog } from "@/components/commons/admin/userDashboard/CreateUserDialog";
import { UpdateUserDialog } from "@/components/commons/admin/userDashboard/UpdateUserDialog";
import { TableSearch } from "@/components/commons/admin/adminTable/TableSearch";
import { UserFilter } from "@/components/commons/admin/userDashboard/UserFilter";
import { UserTable } from "@/components/commons/admin/userDashboard/UserTable";
import { ExtendedUserData } from "@/components/commons/admin/userDashboard/constant";
import { ConfirmationDialog } from "@/components/commons/layout/ConfirmationDialog";
import { TableDashboardSkeleton } from "../adminTable/TableDashboardSkeleton";
import { useRouter } from "next/navigation";
import { useAllUsersQuery } from "@/hooks/api/queries/useUserQueries";
import {
  useCreateUserMutation,
  useUpdateUserMutation,
  useDeleteUserMutation,
} from "@/hooks/api/mutations/useUserMutations";
import { useResetPasswordMutation } from "@/hooks/api/mutations/useAuthMutations";

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
    data: usersResponse,
    isLoading: isLoadingUsers,
    refetch: refetchUsers,
  } = useAllUsersQuery();
  const usersTable = usersResponse?.data?.users || [];

  const { mutate: createUser } = useCreateUserMutation();
  const { mutate: updateUser } = useUpdateUserMutation();
  const { mutate: deleteUser } = useDeleteUserMutation();
  const { mutate: resetPassword } = useResetPasswordMutation();

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
    let results = [...usersTable];

    if (searchQuery.trim()) {
      const searchTerms = searchQuery.toLowerCase().trim();
      results = results.filter(
        (user) =>
          user.email.toLowerCase().includes(searchTerms) ||
          user.username.toLowerCase().includes(searchTerms),
      );
    }

    if (activeFilters.status.length > 0) {
      results = results.filter((user) =>
        activeFilters.status.includes(user.status || ""),
      );
    }

    if (activeFilters.role.length > 0) {
      results = results.filter((user) =>
        activeFilters.role.includes(user.role || ""),
      );
    }

    setFilteredUsers(results);
  }, [usersTable, searchQuery, activeFilters]);

  // Paginate filtered usersTable
  const paginatedUsers = filteredUsers.slice(
    (paginationState.page - 1) * paginationState.pageSize,
    paginationState.page * paginationState.pageSize,
  );

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Filter logic is now handled by useEffect
  };

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
    // Filter logic is now handled by useEffect
    closeMenuMenuFilters();
  };

  const handleRefresh = () => {
    setActiveFilters(userInitialFilters);
    setSearchQuery("");
    refetchUsers();
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
    if (!data) return;

    updateUser(
      {
        userId: data.id,
        data: {
          birth: data.birth || "",
          summary: data.summary || "",
          avatar: avatarFile,
          role: data.role,
          status: data.status,
          instagram: data.instagram || "",
          facebook: data.facebook || "",
          linkedin: data.linkedin || "",
        },
      },
      {
        onSuccess: () => {
          setIsUpdateUserOpen(false);
        },
      },
    );
  };

  const handleCreate = async () => {
    if (!data) return;

    createUser(
      {
        email: data.email,
        password: data.password || "",
        username: data.username,
        birth: data.birth || "",
        summary: data.summary || "",
        avatar: avatarFile,
        role: data.role,
        status: data.status,
        instagram: data.instagram || "",
        facebook: data.facebook || "",
        linkedin: data.linkedin || "",
      },
      {
        onSuccess: () => {
          setIsCreateUserOpen(false);
        },
      },
    );
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
      deleteUser(userToDelete.id);
      setDeleteDialogOpen(false);
      setUserToDelete(null);
    } else if (!isDeleteDialog && userToResetPassword) {
      resetPassword(userToResetPassword.email, {
        onSuccess: () => {
          toast.success("A new password has been sent to the user's email!");
        },
      });
      setResetPasswordDialogOpen(false);
      setUserToResetPassword(null);
    }
  };

  const onUpdate = async (user: IUser) => {
    setData(user);
    setIsUpdateUserOpen(true);
  };

  if (isLoadingUsers) {
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
        title={isDeleteDialog ? "Delete User" : "Reset Password"}
        description={
          isDeleteDialog
            ? "This action cannot be undone. This will permanently delete the user and remove it from our servers."
            : `Do you want to send a password reset email to ${
                userToResetPassword?.email
              }?`
        }
        confirmText={isDeleteDialog ? "Delete" : "Send Email"}
        cancelText="Cancel"
        isDestructive={isDeleteDialog}
        onConfirm={handleDialogConfirm}
      />
    </div>
  );
}
