import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";

// DTO interfaces
export interface Register {
    username: string;
    email: string;
    password: string;
}

export interface Login {
    password: string;
}

export interface VerifyOTP {
    otp: string;
    isActivation: boolean;
}

export interface ForgotPassword {
    password: string;
    confirmPassword: string;
}

export interface ChangePassword {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

// Response interfaces
export interface IAuthDataResponse {
    isActive: boolean;
    user: IUser;
}

/**
 * Auth Service - Pure API calls without state management
 * All functions return Promise<IApiResponse<T>>
 */
export const authService = {
    /**
     * Register a new user
     */
    register: async (data: Register): Promise<IApiResponse<IAuthDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify(data));

        return await handleRequest<IAuthDataResponse>(
            EHttpType.POST,
            `/auth/register`,
            formData
        );
    },

    /**
     * Login user
     */
    login: async (identifier: string, data: Login): Promise<IApiResponse<IAuthDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify(data));

        return await handleRequest<IAuthDataResponse>(
            EHttpType.POST,
            `/auth/login/${identifier}`,
            formData
        );
    },

    /**
     * Logout user
     */
    logout: async (identifier: string): Promise<IApiResponse> => {
        return await handleRequest(EHttpType.POST, `/auth/logout/${identifier}`);
    },

    /**
     * Refresh access token
     */
    refreshToken: async (): Promise<IApiResponse> => {
        return await handleRequest(EHttpType.POST, "/auth/refresh-token");
    },

    /**
     * Send OTP to user email/phone
     */
    sendOTP: async (identifier: string): Promise<IApiResponse> => {
        return await handleRequest(
            EHttpType.POST,
            `/auth/send-otp/${identifier}`
        );
    },

    /**
     * Verify OTP code
     */
    verifyOTP: async (
        identifier: string,
        data: VerifyOTP
    ): Promise<IApiResponse> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify(data));

        return await handleRequest(
            EHttpType.POST,
            `/auth/verify-otp/${identifier}`,
            formData
        );
    },

    /**
     * Reset password (send reset email)
     */
    resetPassword: async (email: string): Promise<IApiResponse> => {
        return await handleRequest(
            EHttpType.PATCH,
            `/auth/reset-password/${email}`,
            {}
        );
    },

    /**
     * Forgot password (set new password with token)
     */
    forgotPassword: async (
        identifier: string,
        data: ForgotPassword
    ): Promise<IApiResponse> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify(data));

        return await handleRequest(
            EHttpType.PATCH,
            `/auth/forgot-password/${identifier}`,
            formData
        );
    },

    /**
     * Change password (for logged in user)
     */
    changePassword: async (
        identifier: string,
        data: ChangePassword
    ): Promise<IApiResponse> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify(data));

        return await handleRequest(
            EHttpType.PATCH,
            `/auth/change-password/${identifier}`,
            formData
        );
    },
};
