import { useMutation, UseMutationResult, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import {
    userService,
    IUserDataResponse,
    CreateUserDTO,
    UpdateUserDTO,
} from "../services/userService";
import { IApiResponse } from "@/lib/axiosInstance";
import { queryKeys, invalidateQueries } from "@/lib/queryClient";
import { useAuthStore } from "@/stores/authStore";

/**
 * User Mutations - for POST/PATCH/DELETE operations
 */

/**
 * Create user mutation
 */
export const useCreateUserMutation = (): UseMutationResult<
    IApiResponse<IUserDataResponse>,
    Error,
    CreateUserDTO
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: userService.createUser,
        onSuccess: (response) => {
            const { success, data } = response;
            const { user, message } = data || {};

            if (success && user) {
                toast.success(message || "User created successfully!");

                // Invalidate users list
                invalidateQueries.allUsers();
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to create user";
            toast.error(message);
        },
    });
};

/**
 * Update user mutation
 */
export const useUpdateUserMutation = (): UseMutationResult<
    IApiResponse<IUserDataResponse>,
    Error,
    { userId: string; data: UpdateUserDTO }
> => {
    const queryClient = useQueryClient();
    const { userAuth, handleSetUserAuth } = useAuthStore();

    return useMutation({
        mutationFn: ({ userId, data }) => userService.updateUser(userId, data),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { user, message } = data || {};

            if (success && user) {
                toast.success(message || "User updated successfully!");

                // Update auth store if updating current user
                if (userAuth?.id === variables.userId) {
                    handleSetUserAuth(user);
                }

                // Invalidate queries
                queryClient.invalidateQueries({
                    queryKey: queryKeys.users.detail(variables.userId),
                });
                invalidateQueries.allUsers();
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to update user";
            toast.error(message);
        },
    });
};

/**
 * Delete user mutation with optimistic update
 */
export const useDeleteUserMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    string
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: userService.deleteUser,

        // Optimistic update
        onMutate: async (userId) => {
            await queryClient.cancelQueries({ queryKey: queryKeys.users.all });

            const previousUsers = queryClient.getQueryData(queryKeys.users.lists());

            queryClient.setQueryData(
                queryKeys.users.lists(),
                (old: any) => {
                    if (!old?.data?.users) return old;
                    return {
                        ...old,
                        data: {
                            ...old.data,
                            users: old.data.users.filter((user: IUser) => user.id !== userId),
                        },
                    };
                }
            );

            return { previousUsers };
        },

        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "User deleted successfully!");
            }
        },

        onError: (error: any, userId, context) => {
            // Rollback on error
            if (context?.previousUsers) {
                queryClient.setQueryData(queryKeys.users.lists(), context.previousUsers);
            }

            const message = error?.response?.data?.message || "Failed to delete user";
            toast.error(message);
        },

        onSettled: () => {
            invalidateQueries.allUsers();
        },
    });
};
