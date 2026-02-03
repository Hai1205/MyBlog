import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";

export interface IUserStore extends IBaseStore {
	adminUsers: IUser[];
	currentUser: IUser | null;

	handleSetCurrentUser: (user: IUser | null) => void;
	setAdminUsers: (users: IUser[]) => void;
	removeFromAdminUsers: (userId: string) => Promise<void>;
	addToAdminUsers: (user: IUser) => Promise<void>;
	updateInAdminUsers: (user: IUser) => Promise<void>;

	reset: () => void;
}

const storeName = "user";
const initialState = {
	adminUsers: [],
	currentUser: null,
};

export const useUserStore = createStore<IUserStore>(
	storeName,
	initialState,
	(set, get) => ({
		handleSetCurrentUser: (user: IUser | null): void => {
			if (user) {
				set({
					currentUser: user,
				});
			}
		},

		setAdminUsers: (users: IUser[]): void => {
			set({ adminUsers: users });
		},

		removeFromAdminUsers: (userId: string): void => {
			set({
				adminUsers: get().adminUsers.filter((user) => user.id !== userId),
			});
		},

		addToAdminUsers: (user: IUser): void => {
			set({ adminUsers: [user, ...get().adminUsers] });
		},

		updateInAdminUsers: (user: IUser): void => {
			set({
				adminUsers: get().adminUsers.map((u) =>
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