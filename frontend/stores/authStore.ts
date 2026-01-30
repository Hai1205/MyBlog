import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";
import { EUserRole } from "../types/enum";
import { useBlogStore } from "./blogStore";
import { useUserStore } from "./userStore";

export interface IAuthStore extends IBaseStore {
	userAuth: IUser | null;
	isAdmin: boolean;

	handleSetUserAuth: (user: IUser | null) => void;
	handleClearAuth: () => void;

	isAuthenticated: () => boolean;
	hasPermission: () => boolean;
	getUserRole: () => string | null;

	reset: () => void;
}

const storeName = "auth";
const initialState = {
	userAuth: null,
	isAdmin: false,
};

export const useAuthStore = createStore<IAuthStore>(
	storeName,
	initialState,
	(set, get) => ({
		handleSetUserAuth: (user: IUser | null): void => {
			if (user) {
				set({
					userAuth: user,
					isAdmin: user.role === EUserRole.ADMIN,
				});
			}
		},

		handleClearAuth: (): void => {
			set({
				userAuth: null,
				isAdmin: false,
			});
		},

		isAuthenticated: (): boolean => {
			return get().userAuth !== null;
		},

		hasPermission: (): boolean => {
			const { userAuth } = get();
			if (!userAuth) return false;

			if (userAuth.role === EUserRole.ADMIN) return true;

			return false;
		},

		getUserRole: (): string | null => {
			return get().userAuth?.role || null;
		},

		reset: () => {
			set({ ...initialState });
			useBlogStore.getState().reset();
			useUserStore.getState().reset();
		},
	}),
	{ storageType: EStorageType.COOKIE }
);