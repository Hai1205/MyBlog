import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";

// DTO interfaces
export interface CreateUser {
    email: string;
    password: string;
    username: string;
    birth: string;
    summary: string;
    avatar: File | null;
    role: string;
    status: string;
    instagram: string;
    facebook: string;
    linkedin: string;
}

export interface UpdateUser {
    birth: string;
    summary: string;
    avatar: File | null;
    role: string;
    status: string;
    instagram: string;
    facebook: string;
    linkedin: string;
}

// Response interfaces
export interface IUserDataResponse {
    user: IUser;
    users: IUser[];
    isActive: boolean;
}

/**
 * User Service - Pure API calls without state management
 */
export const userService = {
    /**
     * Get all users
     */
    getAllUsers: async (): Promise<IApiResponse<IUserDataResponse>> => {
        return await handleRequest<IUserDataResponse>(EHttpType.GET, `/users`);
    },

    /**
     * Get single user by ID
     */
    getUser: async (userId: string): Promise<IApiResponse<IUserDataResponse>> => {
        return await handleRequest<IUserDataResponse>(
            EHttpType.GET,
            `/users/${userId}`
        );
    },

    /**
     * Get single user by identifier
     */
    getUserByIdentifier: async (identifier: string): Promise<IApiResponse<IUserDataResponse>> => {
        return await handleRequest<IUserDataResponse>(
            EHttpType.GET,
            `/users/identifier/${identifier}`
        );
    },
   
    /**
     * Get single user profile by identifier
     */
    getUserProfileByIdentifier: async (identifier: string): Promise<IApiResponse<IUserDataResponse>> => {
        return await handleRequest<IUserDataResponse>(
            EHttpType.GET,
            `/users/profile/${identifier}`
        );
    },

    /**
     * Create new user
     */
    createUser: async (data: CreateUser): Promise<IApiResponse<IUserDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({
            email: data.email,
            password: data.password,
            username: data.username,
            instagram: data.instagram,
            facebook: data.facebook,
            linkedin: data.linkedin,
            birth: data.birth,
            summary: data.summary,
            role: data.role,
            status: data.status,
        }));
        if (data.avatar) {
            formData.append("avatar", data.avatar);
        }

        return await handleRequest<IUserDataResponse>(
            EHttpType.POST,
            `/users`,
            formData
        );
    },

    /**
     * Update user
     */
    updateUser: async (
        userId: string,
        data: UpdateUser
    ): Promise<IApiResponse<IUserDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({
            instagram: data.instagram,
            facebook: data.facebook,
            linkedin: data.linkedin,
            birth: data.birth,
            summary: data.summary,
            role: data.role,
            status: data.status,
        }));
        if (data.avatar) {
            formData.append("avatar", data.avatar);
        }

        return await handleRequest<IUserDataResponse>(
            EHttpType.PATCH,
            `/users/${userId}`,
            formData
        );
    },

    /**
     * Delete user
     */
    deleteUser: async (userId: string): Promise<IApiResponse> => {
        return await handleRequest(EHttpType.DELETE, `/users/${userId}`);
    },

    /**
     * Follow user
     */
    followUser: async (followerId: string, followingId: string): Promise<IApiResponse<IUserDataResponse>> => {
        return await handleRequest(
            EHttpType.POST,
            `/users/${followerId}/follow/${followingId}`
        );
    },
    
    /**
     * Unfollow user
     */
    unfollowUser: async (followerId: string, followingId: string): Promise<IApiResponse<IUserDataResponse>> => {
        return await handleRequest(
            EHttpType.DELETE,
            `/users/${followerId}/unfollow/${followingId}`
        );
    },
};
