import { useQuery, useQueryClient, UseQueryResult } from "@tanstack/react-query";
import { queryKeys } from "@/lib/queryClient";
import { blogService, IBlogDataResponse } from "../services/blogService";
import { IApiResponse } from "@/lib/axiosInstance";

/**
 * Blog Queries - for GET requests
 */

/**
 * Get all blogs with automatic caching
 */
export const useAllBlogsQuery = (
    isVisibility?: boolean,
    options?: {
        enabled?: boolean;
        refetchInterval?: number;
    }
): UseQueryResult<IApiResponse<IBlogDataResponse>, Error> => {
    return useQuery({
        queryKey: isVisibility !== undefined
            ? queryKeys.blogs.byVisibility(isVisibility)
            : queryKeys.blogs.lists(),
        queryFn: () => blogService.getAllBlogs(isVisibility),
        enabled: options?.enabled ?? true,
        staleTime: 3 * 60 * 1000, // 3 minutes
        refetchInterval: options?.refetchInterval,
    });
};

/**
 * Get user's blogs
 */
export const useUserBlogsQuery = (
    userId: string,
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IBlogDataResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.blogs.userBlogs(userId),
        queryFn: () => blogService.getUserBlogs(userId),
        enabled: (options?.enabled ?? true) && !!userId,
        staleTime: 3 * 60 * 1000, // 3 minutes
    });
};

/**
 * Get user's saved blogs
 */
export const useSavedBlogsQuery = (
    userId: string,
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IBlogDataResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.blogs.savedBlogs(userId),
        queryFn: () => blogService.getSavedBlogs(userId),
        enabled: (options?.enabled ?? true) && !!userId,
        staleTime: 3 * 60 * 1000, // 3 minutes
    });
};

/**
 * Get single blog by ID
 */
export const useBlogQuery = (
    blogId: string,
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IBlogDataResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.blogs.detail(blogId),
        queryFn: () => blogService.getBlog(blogId),
        enabled: (options?.enabled ?? true) && !!blogId,
        staleTime: 5 * 60 * 1000, // 5 minutes - blog details cached longer
    });
};

/**
 * Get blog comments
 */
export const useBlogCommentsQuery = (
    blogId: string,
    options?: {
        enabled?: boolean;
        refetchInterval?: number;
    }
): UseQueryResult<IApiResponse<IBlogDataResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.blogs.comments(blogId),
        queryFn: () => blogService.getBlogComments(blogId),
        enabled: (options?.enabled ?? true) && !!blogId,
        staleTime: 1 * 60 * 1000, // 1 minute - comments refetch more frequently
        refetchInterval: options?.refetchInterval,
    });
};

/**
 * Prefetch functions for background fetching
 */
export const usePrefetchBlogs = () => {
    const queryClient = useQueryClient();

    return {
        prefetchAllBlogs: async () => {
            await queryClient.prefetchQuery({
                queryKey: queryKeys.blogs.lists(),
                queryFn: () => blogService.getAllBlogs(),
            });
        },
        prefetchUserBlogs: async (userId: string) => {
            await queryClient.prefetchQuery({
                queryKey: queryKeys.blogs.userBlogs(userId),
                queryFn: () => blogService.getUserBlogs(userId),
            });
        },
    };
};
