import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";

export interface IBlogStore extends IBaseStore {
	selectedBlogId: string | null;
	filterCategory: string | null;
	blogToEdit: IBlog | null;
	sortBy: 'newest' | 'popular' | 'oldest';
	commentsByBlogId: Record<string, IComment[]>;
	savedBlogs: IBlog[];
	myBlogs: IBlog[];

	setSelectedBlog: (blogId: string | null) => void;
	setFilterCategory: (category: string | null) => void;
	setSortBy: (sortBy: 'newest' | 'popular' | 'oldest') => void;
	setBlogToEdit: (blog: IBlog | null) => void;
	getFilteredAndSortedBlogs: (blogs: IBlog[]) => IBlog[];

	// Comment management
	setCommentsForBlog: (blogId: string, comments: IComment[]) => void;
	getCommentsForBlog: (blogId: string) => IComment[];
	addCommentToBlog: (blogId: string, comment: IComment) => void;
	removeCommentFromBlog: (blogId: string, commentId: string) => void;

	setSavedBlogs: (blogs: IBlog[]) => void;
	removeFromSavedBlogs: (blogId: string) => Promise<void>;
	addToSavedBlogs: (blog: IBlog) => Promise<void>;

	setMyBlogs: (blogs: IBlog[]) => void;
	removeFromMyBlogs: (blogId: string) => Promise<void>;
	addToMyBlogs: (blog: IBlog) => Promise<void>;
	updateInMyBlogs: (blog: IBlog) => Promise<void>;

	reset: () => void;
}

const storeName = "blog";
const initialState = {
	selectedBlogId: null,
	filterCategory: null,
	sortBy: 'newest' as const,
	blogToEdit: null,
	commentsByBlogId: {},
	savedBlogs: [],
	myBlogs: [],
};

export const useBlogStore = createStore<IBlogStore>(
	storeName,
	initialState,
	(set, get) => ({
		setSelectedBlog: (blogId: string | null): void => {
			set({ selectedBlogId: blogId });
		},

		setFilterCategory: (category: string | null): void => {
			set({ filterCategory: category });
		},

		setSortBy: (sortBy: 'newest' | 'popular' | 'oldest'): void => {
			set({ sortBy });
		},

		setBlogToEdit: (blog: IBlog | null): void => {
			set({ blogToEdit: blog });
		},

		getFilteredAndSortedBlogs: (blogs: IBlog[]): IBlog[] => {
			const { filterCategory, sortBy } = get();

			let filtered = [...blogs];

			if (filterCategory) {
				filtered = filtered.filter(blog => blog.category === filterCategory);
			}

			switch (sortBy) {
				case 'newest':
					filtered.sort((a, b) =>
						new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
					);
					break;
				case 'oldest':
					filtered.sort((a, b) =>
						new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
					);
					break;
				case 'popular':
					filtered.sort((a, b) => {
						const aViews = (a as any).viewCount || 0;
						const bViews = (b as any).viewCount || 0;
						return bViews - aViews;
					});
					break;
			}

			return filtered;
		},

		// Comment management functions
		setCommentsForBlog: (blogId: string, comments: IComment[]): void => {
			const { commentsByBlogId } = get();
			set({
				commentsByBlogId: {
					...commentsByBlogId,
					[blogId]: comments,
				},
			});
		},

		addCommentToBlog: (blogId: string, comment: IComment): void => {
			const { commentsByBlogId } = get();
			const currentComments = commentsByBlogId[blogId] || [];
			set({
				commentsByBlogId: {
					...commentsByBlogId,
					[blogId]: [...currentComments, comment],
				},
			});
		},

		removeCommentFromBlog: (blogId: string, commentId: string): void => {
			const { commentsByBlogId } = get();
			const currentComments = commentsByBlogId[blogId] || [];
			set({
				commentsByBlogId: {
					...commentsByBlogId,
					[blogId]: currentComments.filter(comment => comment.id !== commentId),
				},
			});
		},

		getCommentsForBlog: (blogId: string): IComment[] => {
			const { commentsByBlogId } = get();
			return commentsByBlogId[blogId] || [];
		},

		setSavedBlogs: (blogs: IBlog[]): void => {
			set({ savedBlogs: blogs });
		},

		removeFromSavedBlogs: (blogId: string): void => {
			set({
				savedBlogs: get().savedBlogs.filter((user) => user.id !== blogId),
			});
		},

		addToSavedBlogs: (user: IBlog): void => {
			set({ savedBlogs: [user, ...get().savedBlogs] });
		},

		setMyBlogs: (blogs: IBlog[]): void => {
			set({ myBlogs: blogs });
		},

		removeFromMyBlogs: (blogId: string): void => {
			set({
				myBlogs: get().myBlogs.filter((user) => user.id !== blogId),
			});
		},

		addToMyBlogs: (user: IBlog): void => {
			set({ myBlogs: [user, ...get().myBlogs] });
		},

		updateInMyBlogs: (user: IBlog): void => {
			set({
				myBlogs: get().myBlogs.map((u) =>
					u.id === user.id ? user : u
				),
			});
		},

		reset: () => {
			set({ ...initialState });
		},
	}),
	{ storageType: EStorageType.LOCAL }
);