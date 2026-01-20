import { ECategory, EUserRole, EUserStatus } from "./enum";

declare global {
    // Pagination types
    interface IPageable {
        page: number;
        size: number;
        sort?: string;
    }

    interface IPageResponse<T> {
        content: T[];
        totalElements: number;
        totalPages: number;
        currentPage: number;
        pageSize: number;
        hasNext: boolean;
        hasPrevious: boolean;
        first: boolean;
        last: boolean;
    }

    interface IUser {
        id: string
        username: string
        email: string
        birth?: string
        summary?: string
        avatarUrl?: string
        role: EUserRole
        status: EUserStatus
        instagram?: string
        facebook?: string
        linkedin?: string
        createdAt: string;
        updatedAt: string;
    }

    interface IBlog {
        id: string;
        title: string;
        description: string;
        content: string;
        thumbnailUrl: string;
        category: ECategory;
        author: IUser;
        comments: IComment[];
        isVisibility: boolean;
        isSaved: boolean;
        createdAt: string;
        updatedAt: string;
    }

    interface IComment {
        id: string;
        userId: string;
        blogId: string;
        username: string;
        content: string;
        createdAt: string;
        updatedAt: string;
    }

    interface ISavedBlog {
        id: string;
        userId: string;
        blogId: string;
        createdAt: string;
    }

    export interface IDashboardStats {
        totalUsers: number;
        activeUsers: number;
        pendingUsers: number;
        bannedUsers: number;
        usersCreatedThisMonth: number;
        totalBlogs: number;
        publicBlogs: number;
        privateBlogs: number;
        blogsCreatedThisMonth: number;
        recentActivities: IActivityInfo[];
    }

    export interface IActivityInfo {
        id: string;
        type: string;
        description: string;
        timestamp: string;
        userId: string;
    }
}

export { };