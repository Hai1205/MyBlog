import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";

export interface IBlogStore extends IBaseStore {
	selectedBlogId: string | null;
	filterCategory: string | null;
	sortBy: 'newest' | 'popular' | 'oldest';

	handleSetSelectedBlog: (blogId: string | null) => void;
	handleSetFilterCategory: (category: string | null) => void;
	handleSetSortBy: (sortBy: 'newest' | 'popular' | 'oldest') => void;

	getFilteredAndSortedBlogs: (blogs: IBlog[]) => IBlog[];

	reset: () => void;
}

const storeName = "blog";
const initialState = {
	selectedBlogId: null,
	filterCategory: null,
	sortBy: 'newest' as const,
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

		reset: () => {
			set({ ...initialState });
		},
	}),
	{ storageType: EStorageType.LOCAL }
);