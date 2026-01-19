import { useMutation, UseMutationResult, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import Cookies from 'js-cookie';
import {
    authService,
    IAuthDataResponse,
    RegisterDTO,
    LoginDTO,
    VerifyOTPDTO,
    ForgotPasswordDTO,
    ChangePasswordDTO
} from "../services/authService";
import { IApiResponse } from "@/lib/axiosInstance";
import { useAuthStore } from "@/stores/authStore";
import { queryKeys, invalidateQueries } from "@/lib/queryClient";
import { EUserRole } from "@/types/enum";

/**
 * Auth Mutations - for POST/PATCH/DELETE operations
 * Each mutation includes optimistic updates and cache invalidation
 */

/**
 * Register mutation
 */
export const useRegisterMutation = (): UseMutationResult<
    IApiResponse<IAuthDataResponse>,
    Error,
    RegisterDTO
> => {
    return useMutation({
        mutationFn: authService.register,
        onSuccess: (response) => {
            if (response.data?.success) {
                toast.success(response.message || "Registration successful! Please verify your email.");
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Registration failed";
            toast.error(message);
        },
    });
};

/**
 * Login mutation
 */
export const useLoginMutation = (): UseMutationResult<
    IApiResponse<IAuthDataResponse>,
    Error,
    LoginDTO
> => {
    const { handleSetUserAuth } = useAuthStore();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: authService.login,
        onSuccess: (response) => {
            const data = response.data;
            const { success, user } = data || {};

            if (success && user) {
                // Update Zustand auth state
                handleSetUserAuth(user);

                toast.success("Login successful!");

                // Prefetch data for admin users in background
                if (user.role === EUserRole.ADMIN) {
                    // TanStack will automatically fetch these when components mount
                    // We can also prefetch here if needed
                    setTimeout(() => {
                        queryClient.prefetchQuery({
                            queryKey: queryKeys.stats.dashboard(),
                        });
                        queryClient.prefetchQuery({
                            queryKey: queryKeys.users.lists(),
                        });
                    }, 100);
                }
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Login failed";
            toast.error(message);
        },
    });
};

/**
 * Logout mutation
 */
export const useLogoutMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    void
> => {
    const authStore = useAuthStore();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: authService.logout,
        onSuccess: (response) => {
            // Clear all auth cookies
            Cookies.remove('access_token');
            Cookies.remove('refresh_token');

            // Reset Zustand auth store
            authStore.reset();

            // Clear all TanStack Query cache
            queryClient.clear();

            if (response.data?.success) {
                toast.success(response.message || "Logged out successfully");
            }
        },
        onError: (error: any) => {
            // Even on error, clear local state
            Cookies.remove('access_token');
            Cookies.remove('refresh_token');
            authStore.reset();
            queryClient.clear();

            const message = error?.response?.data?.message || "Logout failed";
            toast.error(message);
        },
    });
};

/**
 * Send OTP mutation
 */
export const useSendOTPMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    string
> => {
    return useMutation({
        mutationFn: authService.sendOTP,
        onSuccess: (response) => {
            if (response.data?.success) {
                toast.success(response.message || "OTP sent successfully!");
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to send OTP";
            toast.error(message);
        },
    });
};

/**
 * Verify OTP mutation
 */
export const useVerifyOTPMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    { identifier: string; data: VerifyOTPDTO }
> => {
    return useMutation({
        mutationFn: ({ identifier, data }) => authService.verifyOTP(identifier, data),
        onSuccess: (response) => {
            if (response.data?.success) {
                toast.success(response.message || "OTP verified successfully!");
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "OTP verification failed";
            toast.error(message);
        },
    });
};

/**
 * Reset password mutation (send reset email)
 */
export const useResetPasswordMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    string
> => {
    return useMutation({
        mutationFn: authService.resetPassword,
        onSuccess: (response) => {
            if (response.data?.success) {
                toast.success(response.message || "Password reset email sent!");
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to reset password";
            toast.error(message);
        },
    });
};

/**
 * Forgot password mutation (set new password)
 */
export const useForgotPasswordMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    { identifier: string; data: ForgotPasswordDTO }
> => {
    return useMutation({
        mutationFn: ({ identifier, data }) => authService.forgotPassword(identifier, data),
        onSuccess: (response) => {
            if (response.data?.success) {
                toast.success(response.message || "Password changed successfully!");
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to change password";
            toast.error(message);
        },
    });
};

/**
 * Change password mutation (for logged in users)
 */
export const useChangePasswordMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    { identifier: string; data: ChangePasswordDTO }
> => {
    return useMutation({
        mutationFn: ({ identifier, data }) => authService.changePassword(identifier, data),
        onSuccess: (response) => {
            if (response.data?.success) {
                toast.success(response.message || "Password changed successfully!");
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to change password";
            toast.error(message);
        },
    });
};

/**
 * Refresh token mutation
 * Can be used in TokenRefresher component
 */
export const useRefreshTokenMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    void
> => {
    return useMutation({
        mutationFn: authService.refreshToken,
        retry: 1,
        onError: (error: any) => {
            console.error("Token refresh failed:", error);
            // Don't show toast for token refresh failures
            // The TokenRefresher component will handle this
        },
    });
};
