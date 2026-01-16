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
        fullname: string
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
        createdAt: string;
        updatedAt: string;
    }

    interface IComment {
        id: string;
        userId: string;
        blogId: string;
        fullname: string
        content: string
        createdAt: string;
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
        revenueStats: IRevenueStats | null;
    }

    export interface IRevenueStats {
        totalRevenue: number;
        thisMonthRevenue: number;
        lastMonthRevenue: number;
        growthRate: number;
        successfulTransactions: number;
        failedTransactions: number;
        pendingTransactions: number;
        revenueByPaymentMethod: Record<string, number>;
        revenueByPlan: Record<string, number>;
        dailyRevenue: IDailyRevenue[];
        monthlyRevenue: IMonthlyRevenue[];
    }

    export interface IDailyRevenue {
        date: string;
        revenue: number;
        transactions: number;
    }

    export interface IMonthlyRevenue {
        month: string;
        revenue: number;
        transactions: number;
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