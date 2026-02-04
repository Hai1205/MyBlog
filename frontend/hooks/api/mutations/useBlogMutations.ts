import { useMutation, UseMutationResult, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import {
    blogService,
    IBlogDataResponse,
    CreateBlogDTO,
    UpdateBlogDTO,
    AddCommentDTO,
    UpdateCommentDTO,
    AnalyzeTitleDTO,
    AnalyzeDescriptionDTO,
    AnalyzeContentDTO,
} from "../services/blogService";
import { IApiResponse } from "@/lib/axiosInstance";
import { queryKeys, invalidateQueries } from "@/lib/queryClient";

/**
 * Blog Mutations - for POST/PATCH/DELETE operations
 */

/**
 * Create blog mutation
 */
export const useCreateBlogMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { userId: string; data: CreateBlogDTO }
> => {
    return useMutation({
        mutationFn: ({ userId, data }) => blogService.createBlog(userId, data),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { blog, message } = data || {};

            if (success && blog) {
                toast.success(message || "Blog created successfully!");

                // Invalidate queries to refetch
                invalidateQueries.allBlogs();
                invalidateQueries.userBlogs(variables.userId);
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to create blog";
            toast.error(message);
        },
    });
};

/**
 * Duplicate blog mutation
 */
export const useDuplicateBlogMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { blogId: string; userId: string }
> => {
    return useMutation({
        mutationFn: ({ blogId, userId }) => blogService.duplicateBlog(blogId, userId),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { blog, message } = data || {};

            if (success && blog) {
                toast.success(message || "Blog duplicated successfully!");

                // Invalidate queries to refetch
                invalidateQueries.allBlogs();
                invalidateQueries.userBlogs(variables.userId);
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to duplicate blog";
            toast.error(message);
        },
    });
};

/**
 * Update blog mutation
 */
export const useUpdateBlogMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { blogId: string; data: UpdateBlogDTO }
> => {
    return useMutation({
        mutationFn: ({ blogId, data }) => blogService.updateBlog(blogId, data),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Blog updated successfully!");

                // Invalidate specific blog and lists
                invalidateQueries.blogDetail(variables.blogId);
                invalidateQueries.allBlogs();
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to update blog";
            toast.error(message);
        },
    });
};

/**
 * Delete blog mutation with optimistic update
 */
export const useDeleteBlogMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    { blogId: string; userId?: string }
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ blogId }) => blogService.deleteBlog(blogId),

        // Optimistic update
        onMutate: async ({ blogId, userId }) => {
            // Cancel outgoing refetches
            await queryClient.cancelQueries({ queryKey: queryKeys.blogs.all });

            // Snapshot previous values
            const previousAllBlogs = queryClient.getQueryData(queryKeys.blogs.lists());
            const previousUserBlogs = userId
                ? queryClient.getQueryData(queryKeys.blogs.userBlogs(userId))
                : null;

            // Optimistically update
            queryClient.setQueryData(
                queryKeys.blogs.lists(),
                (old: any) => {
                    if (!old?.data?.blogs) return old;
                    return {
                        ...old,
                        data: {
                            ...old.data,
                            blogs: old.data.blogs.filter((blog: IBlog) => blog.id !== blogId),
                        },
                    };
                }
            );

            if (userId) {
                queryClient.setQueryData(
                    queryKeys.blogs.userBlogs(userId),
                    (old: any) => {
                        if (!old?.data?.blogs) return old;
                        return {
                            ...old,
                            data: {
                                ...old.data,
                                blogs: old.data.blogs.filter((blog: IBlog) => blog.id !== blogId),
                            },
                        };
                    }
                );
            }

            return { previousAllBlogs, previousUserBlogs };
        },

        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Blog deleted successfully!");
            }
        },

        onError: (error: any, variables, context) => {
            // Rollback on error
            if (context?.previousAllBlogs) {
                queryClient.setQueryData(queryKeys.blogs.lists(), context.previousAllBlogs);
            }
            if (context?.previousUserBlogs && variables.userId) {
                queryClient.setQueryData(
                    queryKeys.blogs.userBlogs(variables.userId),
                    context.previousUserBlogs
                );
            }

            const message = error?.response?.data?.message || "Failed to delete blog";
            toast.error(message);
        },

        onSettled: () => {
            // Refetch to ensure consistency
            invalidateQueries.allBlogs();
        },
    });
};

/**
 * Save blog mutation
 */
export const useSaveBlogMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { blogId: string; userId: string }
> => {
    return useMutation({
        mutationFn: ({ blogId, userId }) => blogService.saveBlog(blogId, userId),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Blog saved!");
                invalidateQueries.userBlogs(variables.userId);
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to save blog";
            toast.error(message);
        },
    });
};

/**
 * Unsave blog mutation with optimistic update
 */
export const useUnsaveBlogMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { blogId: string; userId: string }
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ blogId, userId }) => blogService.unsaveBlog(blogId, userId),

        // Optimistic update
        onMutate: async ({ blogId, userId }) => {
            await queryClient.cancelQueries({
                queryKey: queryKeys.blogs.savedBlogs(userId)
            });

            const previousSavedBlogs = queryClient.getQueryData(
                queryKeys.blogs.savedBlogs(userId)
            );

            queryClient.setQueryData(
                queryKeys.blogs.savedBlogs(userId),
                (old: any) => {
                    if (!old?.data?.blogs) return old;
                    return {
                        ...old,
                        data: {
                            ...old.data,
                            blogs: old.data.blogs.filter((blog: IBlog) => blog.id !== blogId),
                        },
                    };
                }
            );

            return { previousSavedBlogs };
        },

        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Blog unsaved!");
            }
        },

        onError: (error: any, variables, context) => {
            if (context?.previousSavedBlogs) {
                queryClient.setQueryData(
                    queryKeys.blogs.savedBlogs(variables.userId),
                    context.previousSavedBlogs
                );
            }

            const message = error?.response?.data?.message || "Failed to unsave blog";
            toast.error(message);
        },
    });
};

/**
 * Like blog mutation
 */
export const useLikeBlogMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { blogId: string; userId: string }
> => {
    return useMutation({
        mutationFn: ({ blogId, userId }) => blogService.likeBlog(blogId, userId),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Blog liked!");
                invalidateQueries.blogDetail(variables.blogId);
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to like blog";
            toast.error(message);
        },
    });
};

/**
 * Unlike blog mutation
 */
export const useUnlikeBlogMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { blogId: string; userId: string }
> => {
    return useMutation({
        mutationFn: ({ blogId, userId }) => blogService.unlikeBlog(blogId, userId),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Blog unliked!");
                invalidateQueries.blogDetail(variables.blogId);
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to unlike blog";
            toast.error(message);
        },
    });
};

/**
 * Add comment mutation
 */
export const useAddCommentMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { blogId: string; userId: string; data: AddCommentDTO }
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ blogId, userId, data }) => blogService.addComment(blogId, userId, data),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Comment added!");

                // Invalidate comments for this blog
                queryClient.invalidateQueries({
                    queryKey: queryKeys.blogs.comments(variables.blogId),
                });
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to add comment";
            toast.error(message);
        },
    });
};

/**
 * Update comment mutation
 */
export const useUpdateCommentMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    { commentId: string; blogId: string; data: UpdateCommentDTO }
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ commentId, data }) => blogService.updateComment(commentId, data),
        onSuccess: (response, variables) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Comment updated!");

                queryClient.invalidateQueries({
                    queryKey: queryKeys.blogs.comments(variables.blogId),
                });
            }
        },
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to update comment";
            toast.error(message);
        },
    });
};

/**
 * Delete comment mutation with optimistic update
 */
export const useDeleteCommentMutation = (): UseMutationResult<
    IApiResponse,
    Error,
    { commentId: string; blogId: string }
> => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ commentId }) => blogService.deleteComment(commentId),

        onMutate: async ({ commentId, blogId }) => {
            await queryClient.cancelQueries({
                queryKey: queryKeys.blogs.comments(blogId),
            });

            const previousComments = queryClient.getQueryData(
                queryKeys.blogs.comments(blogId)
            );

            queryClient.setQueryData(
                queryKeys.blogs.comments(blogId),
                (old: any) => {
                    if (!old?.data?.comments) return old;
                    return {
                        ...old,
                        data: {
                            ...old.data,
                            comments: old.data.comments.filter(
                                (comment: IComment) => comment.id !== commentId
                            ),
                        },
                    };
                }
            );

            return { previousComments };
        },

        onSuccess: (response) => {
            const { success, data } = response;
            const { message } = data || {};

            if (success) {
                toast.success(message || "Comment deleted!");
            }
        },

        onError: (error: any, variables, context) => {
            if (context?.previousComments) {
                queryClient.setQueryData(
                    queryKeys.blogs.comments(variables.blogId),
                    context.previousComments
                );
            }

            const message = error?.response?.data?.message || "Failed to delete comment";
            toast.error(message);
        },
    });
};

/**
 * AI: Analyze title mutation
 */
export const useAnalyzeTitleMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    AnalyzeTitleDTO
> => {
    return useMutation({
        mutationFn: blogService.analyzeTitle,
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to analyze title";
            toast.error(message);
        },
    });
};

/**
 * AI: Analyze description mutation
 */
export const useAnalyzeDescriptionMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    AnalyzeDescriptionDTO
> => {
    return useMutation({
        mutationFn: blogService.analyzeDescription,
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to analyze description";
            toast.error(message);
        },
    });
};

/**
 * AI: Analyze content mutation
 */
export const useAnalyzeContentMutation = (): UseMutationResult<
    IApiResponse<IBlogDataResponse>,
    Error,
    AnalyzeContentDTO
> => {
    return useMutation({
        mutationFn: blogService.analyzeContent,
        onError: (error: any) => {
            const message = error?.response?.data?.message || "Failed to analyze content";
            toast.error(message);
        },
    });
};
