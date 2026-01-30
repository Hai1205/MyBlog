import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";

// DTO interfaces
export interface CreateBlogDTO {
    title: string;
    description: string;
    category: string;
    thumbnail: File | null;
    content: string;
    isVisibility: boolean;
}

export interface UpdateBlogDTO {
    title: string;
    description: string;
    category: string;
    thumbnail: File | null;
    content: string;
    isVisibility: boolean;
}

export interface AddCommentDTO {
    content: string;
}

export interface UpdateCommentDTO {
    content: string;
}

export interface AnalyzeTitleDTO {
    title: string;
}

export interface AnalyzeDescriptionDTO {
    title: string;
    description: string;
}

export interface AnalyzeContentDTO {
    content: string;
}

// Response interfaces
export interface IBlogDataResponse {
    blog: IBlog;
    blogs: IBlog[];
    comment: IComment;
    comments: IComment[];
    content: string;
    title: string;
    description: string;
}

/**
 * Blog Service - Pure API calls without state management
 */
export const blogService = {
    /**
     * Get all blogs
     */
    getAllBlogs: async (isVisibility?: boolean): Promise<IApiResponse<IBlogDataResponse>> => {
        const params = isVisibility !== undefined ? `?isVisibility=${isVisibility}` : '';
        return await handleRequest<IBlogDataResponse>(EHttpType.GET, `/blogs${params}`);
    },

    /**
     * Get user's blogs
     */
    getUserBlogs: async (userId: string): Promise<IApiResponse<IBlogDataResponse>> => {
        return await handleRequest<IBlogDataResponse>(
            EHttpType.GET,
            `/blogs/users/${userId}`
        );
    },

    /**
     * Get user's saved blogs
     */
    getSavedBlogs: async (userId: string): Promise<IApiResponse<IBlogDataResponse>> => {
        return await handleRequest<IBlogDataResponse>(
            EHttpType.GET,
            `/blogs/users/${userId}/saved`
        );
    },

    /**
     * Get single blog by ID
     */
    getBlog: async (blogId: string, userId?: string): Promise<IApiResponse<IBlogDataResponse>> => {
        const endpoint = userId
            ? `/blogs/${blogId}/users/${userId}`
            : `/blogs/${blogId}`;

        return await handleRequest<IBlogDataResponse>(
            EHttpType.GET,
            endpoint
        );
    },

    /**
     * Create new blog
     */
    createBlog: async (userId: string, data: CreateBlogDTO): Promise<IApiResponse<IBlogDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({
            title: data.title,
            description: data.description,
            category: data.category,
            content: data.content,
            isVisibility: data.isVisibility,
        }));
        if (data.thumbnail) {
            formData.append("thumbnail", data.thumbnail);
        }

        return await handleRequest<IBlogDataResponse>(
            EHttpType.POST,
            `/blogs/users/${userId}`,
            formData
        );
    },

    /**
     * Duplicate blog
     */
    duplicateBlog: async (blogId: string, userId: string): Promise<IApiResponse<IBlogDataResponse>> => {
        return await handleRequest<IBlogDataResponse>(
            EHttpType.POST,
            `/blogs/${blogId}/users/${userId}/duplicate`);
    },

    /**
     * Update blog
     */
    updateBlog: async (
        blogId: string,
        data: UpdateBlogDTO
    ): Promise<IApiResponse<IBlogDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({
            title: data.title,
            description: data.description,
            category: data.category,
            content: data.content,
            isVisibility: data.isVisibility,
        }));
        if (data.thumbnail) {
            formData.append("thumbnail", data.thumbnail);
        }

        return await handleRequest<IBlogDataResponse>(
            EHttpType.PATCH,
            `/blogs/${blogId}`,
            formData
        );
    },

    /**
     * Delete blog
     */
    deleteBlog: async (blogId: string): Promise<IApiResponse> => {
        return await handleRequest(EHttpType.DELETE, `/blogs/${blogId}`);
    },

    /**
     * Save blog
     */
    saveBlog: async (
        blogId: string,
        userId: string
    ): Promise<IApiResponse<IBlogDataResponse>> => {
        return await handleRequest<IBlogDataResponse>(
            EHttpType.POST,
            `/blogs/${blogId}/users/${userId}/save`,
            new FormData()
        );
    },

    /**
     * Unsave blog
     */
    unsaveBlog: async (
        blogId: string,
        userId: string
    ): Promise<IApiResponse<IBlogDataResponse>> => {
        return await handleRequest<IBlogDataResponse>(
            EHttpType.DELETE,
            `/blogs/${blogId}/users/${userId}/unsave`
        );
    },

    /**
     * Get blog comments
     */
    getBlogComments: async (blogId: string): Promise<IApiResponse<IBlogDataResponse>> => {
        return await handleRequest<IBlogDataResponse>(
            EHttpType.GET,
            `/comments/blogs/${blogId}`
        );
    },

    /**
     * Add comment to blog
     */
    addComment: async (blogId: string, userId: string, data: AddCommentDTO): Promise<IApiResponse<IBlogDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({
            content: data.content,
        }));

        return await handleRequest<IBlogDataResponse>(
            EHttpType.POST,
            `/comments/blogs/${blogId}/users/${userId}`,
            formData
        );
    },

    /**
     * Update comment
     */
    updateComment: async (
        commentId: string,
        data: UpdateCommentDTO
    ): Promise<IApiResponse<IBlogDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({
            content: data.content,
        }));

        return await handleRequest<IBlogDataResponse>(
            EHttpType.PATCH,
            `/comments/${commentId}`,
            formData
        );
    },

    /**
     * Delete comment
     */
    deleteComment: async (commentId: string): Promise<IApiResponse> => {
        return await handleRequest(EHttpType.DELETE, `/comments/${commentId}`);
    },

    /**
     * AI: Analyze title
     */
    analyzeTitle: async (data: AnalyzeTitleDTO): Promise<IApiResponse<IBlogDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({ title: data.title }));

        return await handleRequest<IBlogDataResponse>(
            EHttpType.POST,
            `/ai/title`,
            formData
        );
    },

    /**
     * AI: Analyze description
     */
    analyzeDescription: async (
        data: AnalyzeDescriptionDTO
    ): Promise<IApiResponse<IBlogDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({
            title: data.title,
            description: data.description,
        }));

        return await handleRequest<IBlogDataResponse>(
            EHttpType.POST,
            `/ai/description`,
            formData
        );
    },

    /**
     * AI: Analyze content
     */
    analyzeContent: async (
        data: AnalyzeContentDTO
    ): Promise<IApiResponse<IBlogDataResponse>> => {
        const formData = new FormData();
        formData.append("data", JSON.stringify({ content: data.content }));

        return await handleRequest<IBlogDataResponse>(
            EHttpType.POST,
            `/ai/content`,
            formData
        );
    },
};
