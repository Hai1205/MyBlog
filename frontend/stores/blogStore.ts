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
	isLoadingAllBlogs: boolean
	isLoadingUserBlogs: boolean

	getAllBlogs: () => Promise<IApiResponse<IBlogDataResponse>>;
	fetchAllBlogsInBackground: () => Promise<void>;
	getUserBlogs: (userId: string) => Promise<IApiResponse<IBlogDataResponse>>;
	fetchUserBlogsInBackground: (userId: string) => Promise<void>;
	getBlog: (
		blogId: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	createBlog: (
		user: IUser | null,
		title: string,
		description: string,
		category: string,
		thumbnail: File | null,
		content: string,
	) => Promise<IApiResponse<IBlogDataResponse>>;
	saveBlog: (
		blogId: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	updateBlog: (
		blogId: string,
		title: string,
		description: string,
		category: string,
		thumbnail: File | null,
		content: string,
	) => Promise<IApiResponse<IBlogDataResponse>>;
	deleteBlog: (
		blogId: string
	) => Promise<IApiResponse<void>>;
	getAITitleResponse: (
		title: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	getAIDescriptionResponse: (
		title: string,
		description: string
	) => Promise<IApiResponse<IBlogDataResponse>>;
	getAIContentResponse: (
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

	handleUpdateBlog: (blogData: Partial<IBlog>) => void;
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
	lastFetchTimeAllBlogs: null as number | null,
	lastFetchTimeUserBlogs: null as number | null,
	isLoadingAllBlogs: false,
	isLoadingUserBlogs: false,
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
					console.log("All CVs cache is still valid, skipping fetch");
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
		): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				title,
				description,
				category,
				content,
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

		updateBlog: async (
			blogId: string,
			title: string,
			description: string,
			category: string,
			thumbnail: File | null,
			content: string,
		): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				title,
				description,
				category,
				content,
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

		getAITitleResponse: async (title: string): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({ title }));
			
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, `/ai/title`, formData);
			});
		},

		getAIDescriptionResponse: async (title: string, description: string): Promise<IApiResponse<IBlogDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({ title, description }));
			
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, `/ai/description`, formData);
			});
		},

		getAIContentResponse: async (content: string): Promise<IApiResponse<IBlogDataResponse>> => {
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

		handleUpdateBlog: (blogData: Partial<IBlog>) => {
			const currentState = get();
			// set({
			// 	currentBlog: currentState.currentBlog ? { ...currentState.currentBlog, ...blogData, updatedAt: new Date().toISOString() } : null,
			// } as Partial<IBlogStore>);
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