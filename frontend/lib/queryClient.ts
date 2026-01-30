import { QueryClient, DefaultOptions } from "@tanstack/react-query";
import { toast } from "react-toastify";

const queryConfig: DefaultOptions = {
    queries: {
        // Retry configuration
        retry: (failureCount, error: any) => {
            // Don't retry on 4xx errors (client errors)
            if (error?.response?.status >= 400 && error?.response?.status < 500) {
                return false;
            }
            // Retry up to 1 time on network/server errors
            return failureCount < 1;
        },

        // Cache configuration
        staleTime: 3 * 60 * 1000, // 3 minutes - data is fresh
        gcTime: 10 * 60 * 1000, // 10 minutes - garbage collection time (formerly cacheTime)

        // Refetch configuration
        refetchOnWindowFocus: false, // Don't refetch on window focus
        refetchOnReconnect: true, // Refetch on reconnect
        refetchOnMount: true, // Refetch on mount if stale

        // Error handling
        throwOnError: false, // Don't throw errors, handle them in components
    },

    mutations: {
        // Error handling for mutations
        onError: (error: any) => {
            const message =
                error?.response?.data?.message ||
                error?.message ||
                "An unexpected error occurred";

            toast.error(message);
        },

        // Retry configuration for mutations
        retry: false, // Don't retry mutations by default
    },
};

// Create a client factory function for SSR compatibility
export function makeQueryClient() {
    return new QueryClient({
        defaultOptions: queryConfig,
    });
}

// Browser-only client instance (for client components)
let browserQueryClient: QueryClient | undefined = undefined;

export function getQueryClient() {
    if (typeof window === 'undefined') {
        // Server: always create a new query client
        return makeQueryClient();
    } else {
        // Browser: reuse the same client
        if (!browserQueryClient) browserQueryClient = makeQueryClient();
        return browserQueryClient;
    }
}

// Export for backward compatibility
export const queryClient = getQueryClient();

// Query keys factory - for consistency
export const queryKeys = {
    auth: {
        all: ['auth'] as const,
        user: () => [...queryKeys.auth.all, 'user'] as const,
        refreshToken: () => [...queryKeys.auth.all, 'refresh'] as const,
    },

    blogs: {
        all: ['blogs'] as const,
        lists: () => [...queryKeys.blogs.all, 'list'] as const,
        list: (filters?: Record<string, any>) =>
            [...queryKeys.blogs.lists(), filters] as const,
        byVisibility: (isVisibility: boolean) =>
            [...queryKeys.blogs.all, 'visibility', isVisibility] as const,
        detail: (id: string, userId?: string) =>
            userId
                ? [...queryKeys.blogs.all, 'detail', id, 'user', userId] as const
                : [...queryKeys.blogs.all, 'detail', id] as const,
        userBlogs: (userId: string) =>
            [...queryKeys.blogs.all, 'user', userId] as const,
        savedBlogs: (userId: string) =>
            [...queryKeys.blogs.all, 'saved', userId] as const,
        comments: (blogId: string) =>
            [...queryKeys.blogs.all, 'comments', blogId] as const,
    },

    users: {
        all: ['users'] as const,
        lists: () => [...queryKeys.users.all, 'list'] as const,
        detail: (identifier: string, ) => [...queryKeys.users.all, 'detail', identifier] as const,
    },

    stats: {
        all: ['stats'] as const,
        dashboard: () => [...queryKeys.stats.all, 'dashboard'] as const,
        reports: () => [...queryKeys.stats.all, 'reports'] as const,
    },
};

// Helper function to invalidate queries
export const invalidateQueries = {
    allBlogs: () => queryClient.invalidateQueries({ queryKey: queryKeys.blogs.all }),
    blogDetail: (id: string) =>
        queryClient.invalidateQueries({ queryKey: queryKeys.blogs.detail(id) }),
    userBlogs: (userId: string) =>
        queryClient.invalidateQueries({ queryKey: queryKeys.blogs.userBlogs(userId) }),
    allUsers: () => queryClient.invalidateQueries({ queryKey: queryKeys.users.all }),
    allAuth: () => queryClient.invalidateQueries({ queryKey: queryKeys.auth.all }),
};

// Helper for prefetching (for background fetches)
export const prefetchQueries = {
    blogs: async () => {
        await queryClient.prefetchQuery({
            queryKey: queryKeys.blogs.lists(),
            staleTime: 3 * 60 * 1000,
        });
    },

    users: async () => {
        await queryClient.prefetchQuery({
            queryKey: queryKeys.users.lists(),
            staleTime: 3 * 60 * 1000,
        });
    },
};
