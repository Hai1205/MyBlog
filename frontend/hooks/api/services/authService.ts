import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";

// DTO interfaces
export interface RegisterDTO {
    username: string;
    email: string;
    password: string;
}

export interface LoginDTO {
    identifier: string;
    password: string;
}

export interface VerifyOTPDTO {
    otp: string;
    isActivation: boolean;
}

export interface ForgotPasswordDTO {
    password: string;
    confirmPassword: string;
}

export interface ChangePasswordDTO {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

// Response interfaces
export interface IAuthDataResponse {
    user: IUser;
    isActive: boolean;
}

/**
 * Auth Service - Pure API calls without state management
 * All functions return Promise<IApiResponse<T>>
 */
export const authService = {
    /**
     * Register a new user
     */
    register: async (data: RegisterDTO): Promise<IApiResponse<IAuthDataResponse>> => {
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
    login: async (data: LoginDTO): Promise<IApiResponse<IAuthDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify(data));

        return await handleRequest<IAuthDataResponse>(
            EHttpType.POST,
            `/auth/login`,
            formData
        );
    },

    /**
     * Logout user
     */
    logout: async (): Promise<IApiResponse> => {
        return await handleRequest(EHttpType.POST, `/auth/logout`);
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
        data: VerifyOTPDTO
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
        data: ForgotPasswordDTO
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
        data: ChangePasswordDTO
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
