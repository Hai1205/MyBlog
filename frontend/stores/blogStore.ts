import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";

export interface IBlogStore extends IBaseStore {
	selectedBlogId: string | null;
	filterCategory: string | null;
	blogToEdit: IBlog | null;
	sortBy: 'newest' | 'popular' | 'oldest';
	commentsByBlogId: Record<string, IComment[]>;

	handleSetSelectedBlog: (blogId: string | null) => void;
	handleSetFilterCategory: (category: string | null) => void;
	handleSetSortBy: (sortBy: 'newest' | 'popular' | 'oldest') => void;
	handleSetBlogToEdit: (blog: IBlog | null) => void;
	getFilteredAndSortedBlogs: (blogs: IBlog[]) => IBlog[];

	// Comment management
	setCommentsForBlog: (blogId: string, comments: IComment[]) => void;
	addCommentToBlog: (blogId: string, comment: IComment) => void;
	removeCommentFromBlog: (blogId: string, commentId: string) => void;
	getCommentsForBlog: (blogId: string) => IComment[];

	reset: () => void;
}

const storeName = "blog";
const initialState = {
	selectedBlogId: null,
	filterCategory: null,
	sortBy: 'newest' as const,
	blogToEdit: null as IBlog | null,
	commentsByBlogId: {} as Record<string, IComment[]>,
};

export const useBlogStore = createStore<IBlogStore>(
	storeName,
	initialState,
	(set, get) => ({
		handleSetSelectedBlog: (blogId: string | null): void => {
			set({ selectedBlogId: blogId });
		},

		handleSetFilterCategory: (category: string | null): void => {
			set({ filterCategory: category });
		},

		handleSetSortBy: (sortBy: 'newest' | 'popular' | 'oldest'): void => {
			set({ sortBy });
		},

		handleSetBlogToEdit: (blog: IBlog | null): void => {
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

		reset: () => {
			set({ ...initialState });
		},
	}),
	{ storageType: EStorageType.LOCAL }
);