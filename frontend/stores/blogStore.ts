import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";
import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";

interface IBlogDataResponse {
	blog: IBlog,
	blogs: IBlog[],
	comments: IComment[],
	content: string,
	title: string,
	description: string,
}

export interface IBlogStore extends IBaseStore {
	blogList: IBlog[]
	userBlogs: IBlog[]
	blogsTable: IBlog[]
	savedBlogs: IBlog[]
	blogs: IBlog[]
	lastFetchTimeAllBlogs: number | null
	lastFetchTimeUserBlogs: number | null
	lastFetchTimeSavedBlogs: number | null
	isLoadingAllBlogs: boolean
	isLoadingUserBlogs: boolean
	isLoadingSavedBlogs: boolean

	getAllBlogs: () => Promise<IApiResponse<IBlogDataResponse>>;
	fetchAllBlogsInBackground: () => Promise<void>;
	getUserBlogs: (userId: string) => Promise<IApiResponse<IBlogDataResponse>>;
	fetchUserBlogsInBackground: (userId: string) => Promise<void>;
	getSavedBlogs: (userId: string) => Promise<IApiResponse<IBlogDataResponse>>;
	fetchSavedBlogsInBackground: (userId: string) => Promise<void>;
	getBlog: (
		blogId: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	createBlog: (
		userId: string,
		title: string,
		description: string,
		category: string,
		thumbnail: File | null,
		content: string,
		isVisibility: boolean
	) => Promise<IApiResponse<IBlogDataResponse>>;
	saveBlog: (
		blogId: string,
		userId: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	unsaveBlog: (
		blogId: string,
		userId: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	updateBlog: (
		blogId: string,
		title: string,
		description: string,
		category: string,
		thumbnail: File | null,
		content: string,
		isVisibility: boolean
	) => Promise<IApiResponse<IBlogDataResponse>>;
	deleteBlog: (
		blogId: string
	) => Promise<IApiResponse<void>>;
	analyzeTitle: (
		title: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	analyzeDescription: (
		title: string,
		description: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	analyzeContent: (
		content: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	getBlogComments: (
		blogId: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	addComment: (
		blogId: string,
		userId: string,
		content: string,
	) => Promise<IApiResponse<IBlogDataResponse>>;
	updateComment: (
		commentId: string,
	) => Promise<IApiResponse<IBlogDataResponse>>;
	deleteComment: (
		commentId: string
	) => Promise<IApiResponse<void>>;

	handleClearBlogList: () => void;
	handleAddBlogToUserBlogs: (blog: IBlog) => void;
	handleRemoveBlogFromUserBlogs: (blogId: string) => void;
	handleRemoveBlogFromTable: (blogId: string) => void;
}

const storeName = "blog";
const initialState = {
	blogList: [],
	userBlogs: [],
	blogsTable: [],
	savedBlogs: [],
	lastFetchTimeAllBlogs: null as number | null,
	lastFetchTimeUserBlogs: null as number | null,
	lastFetchTimeSavedBlogs: null as number | null,
	isLoadingAllBlogs: false,
	isLoadingUserBlogs: false,
	isLoadingSavedBlogs: false,
};

// Cache expiration time: 3 minutes
const CACHE_DURATION = 3 * 60 * 1000;

export const useBlogStore = createStore<IBlogStore>(
	storeName,
	initialState,
	(set, get) => ({
		getAllBlogs: async (): Promise<IApiResponse<IBlogDataResponse>> => {
			return await get().handleRequest(async () => {
				set({ isLoadingAllBlogs: true });

				try {
					const res = await handleRequest<IBlogDataResponse>(EHttpType.GET, `/blogs`);

					if (res.data && res.data.success && res.data.blogs) {
						set({
							blogsTable: res.data.blogs,
							lastFetchTimeAllBlogs: Date.now(),
							isLoadingAllBlogs: false
						});
					} else {
						set({ isLoadingAllBlogs: false });
					}

					return res;
				} catch (error) {
					set({ isLoadingAllBlogs: false });
					throw error;
				}
			});
		},

		fetchAllBlogsInBackground: async (): Promise<void> => {
			const state = get();
			const now = Date.now();

			// Check if cache is still valid
			if (state.blogsTable.length > 0 && state.lastFetchTimeAllBlogs) {
				const cacheAge = now - state.lastFetchTimeAllBlogs;
				if (cacheAge < CACHE_DURATION) {
					console.log("All Blogs cache is still valid, skipping fetch");
					return;
				}
			}

			// Check if already loading
			if (state.isLoadingAllBlogs) {
				return;
			}

			get().getAllBlogs();
		},

		getUserBlogs: async (userId: string): Promise<IApiResponse<IBlogDataResponse>> => {
			return await get().handleRequest(async () => {
				// Set loading state before API call
				set({ isLoadingUserBlogs: true });

				try {
					const res = await handleRequest<IBlogDataResponse>(EHttpType.GET, `/blogs/users/${userId}`);

					if (res.data && res.data.success && res.data.blogs) {
						set({
							userBlogs: res.data.blogs,
							lastFetchTimeUserBlogs: Date.now(),
							isLoadingUserBlogs: false
						});
					} else {
						set({ isLoadingUserBlogs: false });
					}

					return res;
				} catch (error) {
					set({ isLoadingUserBlogs: false });
					throw error;
				}
			});
		},

		fetchUserBlogsInBackground: async (userId: string): Promise<void> => {
			const state = get();
			const now = Date.now();

			// Check if cache is still valid
			if (state.userBlogs.length > 0 && state.lastFetchTimeUserBlogs) {
				const cacheAge = now - state.lastFetchTimeUserBlogs;
				if (cacheAge < CACHE_DURATION) {
					console.log("User Blogs cache is still valid, skipping fetch");
					return;
				}
			}

			// Check if already loading
			if (state.isLoadingUserBlogs) {
				return;
			}

			get().getUserBlogs(userId);
		},

		getSavedBlogs: async (userId: string): Promise<IApiResponse<IBlogDataResponse>> => {
			return await get().handleRequest(async () => {
				// Set loading state before API call
				set({ isLoadingSavedBlogs: true });

				try {
					const res = await handleRequest<IBlogDataResponse>(EHttpType.GET, `/blogs/users/${userId}/saved`);

					if (res.data && res.data.success && res.data.blogs) {
						set({
							savedBlogs: res.data.blogs,
							lastFetchTimeSavedBlogs: Date.now(),
							isLoadingSavedBlogs: false
						});
					} else {
						set({ isLoadingSavedBlogs: false });
					}

					return res;
				} catch (error) {
					set({ isLoadingSavedBlogs: false });
					throw error;
				}
			});
		},

		fetchSavedBlogsInBackground: async (userId: string): Promise<void> => {
			const state = get();
			const now = Date.now();

			// Check if cache is still valid
			if (state.savedBlogs.length > 0 && state.lastFetchTimeSavedBlogs) {
				const cacheAge = now - state.lastFetchTimeSavedBlogs;
				if (cacheAge < CACHE_DURATION) {
					console.log("Saved Blogs cache is still valid, skipping fetch");
					return;
				}
			}

			// Check if already loading
			if (state.isLoadingSavedBlogs) {
				return;
			}

			get().getSavedBlogs(userId);
		},

		getBlog: async (blogId: string): Promise<IApiResponse<IBlogDataResponse>> => {
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.GET, `/blogs/${blogId}`);
			});
		},

		createBlog: async (
			userId: string,
			title: string,
			description: string,
			category: string,
			thumbnail: File | null,
			content: string,
			isVisibility: boolean
		): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				title,
				description,
				category,
				content,
				isVisibility,
			}));
			if (thumbnail) formData.append("thumbnail", thumbnail);

			return await get().handleRequest(async () => {
				const res = await handleRequest<IBlogDataResponse>(EHttpType.POST, `/blogs/users/${userId}`, formData);
				console.log("Create Blog response:", res);

				if (res.data && res.data.success && res.data.blog) {
					get().handleAddBlogToUserBlogs(res.data.blog);
				}

				return res;
			});
		},

		saveBlog: async (
			blogId: string,
			userId: string
		): Promise<IApiResponse<IBlogDataResponse>> => {
			return await get().handleRequest(async () => {
				const res = await handleRequest<IBlogDataResponse>(EHttpType.POST, `/blogs/${blogId}/users/${userId}/save`, new FormData());
				console.log("Save Blog response:", res);

				if (res.data && res.data.success && res.data.blog) {
					get().handleAddBlogToUserBlogs(res.data.blog);
				}

				return res;
			});
		},

		unsaveBlog: async (
			blogId: string,
			userId: string
		): Promise<IApiResponse<IBlogDataResponse>> => {
			// Remove from saved blogs immediately for optimistic UI
			const currentSavedBlogs = get().savedBlogs;
			set({ savedBlogs: currentSavedBlogs.filter(blog => blog.id !== blogId) });

			// Call API in background
			get().handleRequest(async () => {
				const res = await handleRequest<IBlogDataResponse>(EHttpType.DELETE, `/blogs/${blogId}/users/${userId}/unsave`);
				console.log("Unsave Blog response:", res);
				return res;
			});

			// Return success immediately
			return { data: { success: true } } as IApiResponse<IBlogDataResponse>;
		},

		updateBlog: async (
			blogId: string,
			title: string,
			description: string,
			category: string,
			thumbnail: File | null,
			content: string,
			isVisibility: boolean
		): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				title,
				description,
				category,
				content,
				isVisibility,
			}));
			if (thumbnail) formData.append("thumbnail", thumbnail);

			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.PATCH, `/blogs/${blogId}`, formData);
			});
		},

		deleteBlog: async (blogId: string): Promise<IApiResponse> => {
			// Remove from user Blogs immediately
			get().handleRemoveBlogFromUserBlogs(blogId);

			// Remove from Blogs table immediately
			get().handleRemoveBlogFromTable(blogId);

			// Call API in background
			get().handleRequest(async () => {
				const res = await handleRequest(EHttpType.DELETE, `/blogs/${blogId}`);
				// If API fails, we could add back, but for now, assume success
				return res;
			});

			// Return success immediately
			return { data: { success: true } } as IApiResponse;
		},

		analyzeTitle: async (title: string): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({ title }));

			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, `/ai/title`, formData);
			});
		},

		analyzeDescription: async (title: string, description: string): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({ title, description }));

			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, `/ai/description`, formData);
			});
		},

		analyzeContent: async (content: string): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({ content }));

			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, `/ai/content`, formData);
			});
		},

		getBlogComments: async (blogId: string): Promise<IApiResponse<IBlogDataResponse>> => {
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.GET, `/comments/blogs/${blogId}`);
			});
		},

		addComment: async (
			blogId: string,
			userId: string,
			content: string
		): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				content,
			}));

			return await get().handleRequest(async () => {
				const res = await handleRequest<IBlogDataResponse>(EHttpType.POST, `/comments/blogs/${blogId}/users/${userId}`, formData);
				console.log("Add Comment response:", res);

				if (res.data && res.data.success && res.data.blog) {
					get().handleAddBlogToUserBlogs(res.data.blog);
				}

				return res;
			});
		},

		updateComment: async (
			commentId: string,
			content: string
		): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				content,
			}));

			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.PATCH, `/comments/${commentId}`, formData);
			});
		},

		deleteComment: async (commentId: string): Promise<IApiResponse> => {
			get().handleRequest(async () => {
				const res = await handleRequest(EHttpType.DELETE, `/comments/${commentId}`);
				return res;
			});

			return { data: { success: true } } as IApiResponse;
		},

		handleClearBlogList: (): void => {
			set({ blogList: [] });
		},

		reset: () => {
			set({ ...initialState });
		},
	}),
	{ storageType: EStorageType.LOCAL }
);