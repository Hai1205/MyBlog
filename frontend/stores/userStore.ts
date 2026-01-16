import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";
import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";
import { useAuthStore } from "./authStore";

interface IUserDataResponse {
	user: IUser;
	users: IUser[];
	isActive: boolean;
}

export interface IUserStore extends IBaseStore {
	user: IUser | null;
	users: IUser[];
	usersTable: IUser[];
	lastFetchTime: number | null;

	getAllUsers: () => Promise<IApiResponse<IUserDataResponse>>;
	fetchAllUsersInBackground: () => Promise<void>;
	getUser: (
		userId: string
	) => Promise<IApiResponse<IUserDataResponse>>;
	createUser: (
		email: string,
		password: string,
		fullname: string,
		username: string,
		birth: string,
		summary: string,
		avatar: File | null,
		role: string,
		status: string,
		instagram: string,
		facebook: string,
		linkedin: string,
	) => Promise<IApiResponse<IUserDataResponse>>;
	updateUser: (
		userId: string,
		fullname: string,
		birth: string,
		summary: string,
		avatar: File | null,
		role: string,
		status: string,
		instagram: string,
		facebook: string,
		linkedin: string,
	) => Promise<IApiResponse<IUserDataResponse>>;
	deleteUser: (
		userId: string
	) => Promise<IApiResponse>;

	handleRemoveUserFromTable: (userId: string) => Promise<void>;
	handleAddUserToTable: (user: IUser) => Promise<void>;
	handleUpdateUserInTable: (user: IUser) => Promise<void>;
}

const storeName = "user";
const initialState = {
	user: null,
	users: [],
	usersTable: [],
	lastFetchTime: null as number | null,
};

// Cache expiration time: 3 minutes
const CACHE_DURATION = 3 * 60 * 1000;

export const useUserStore = createStore<IUserStore>(
	storeName,
	initialState,
	(set, get) => ({
		getAllUsers: async (): Promise<IApiResponse<IUserDataResponse>> => {
			return await get().handleRequest(async () => {
				const res = await handleRequest<IUserDataResponse>(EHttpType.GET, `/users`);

				if (res.data && res.data.success && res.data.users) {
					set({
						usersTable: res.data.users,
						lastFetchTime: Date.now()
					});
				}

				return res;
			});
		},

		fetchAllUsersInBackground: async (): Promise<void> => {
			const state = get();
			const now = Date.now();

			// Check if cache is still valid
			if (state.usersTable.length > 0 && state.lastFetchTime) {
				const cacheAge = now - state.lastFetchTime;
				if (cacheAge < CACHE_DURATION) {
					console.log("Users cache is still valid, skipping fetch");
					return;
				}
			}

			// Check if already loading
			if (state.isLoading) {
				return;
			}

			await get().getAllUsers();
		},

		getUser: async (userId: string): Promise<IApiResponse<IUserDataResponse>> => {
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.GET, `/users/${userId}`);
			});
		},

		createUser: async (
			email: string,
			password: string,
			fullname: string,
			username: string,
			birth: string,
			summary: string,
			avatar: File | null,
			role: string,
			status: string,
			instagram: string,
			facebook: string,
			linkedin: string,
		): Promise<IApiResponse<IUserDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				email,
				password,
				fullname,
				username,
				instagram,
				facebook,
				linkedin,
				birth,
				summary,
				role,
				status,
			}));
			if (avatar) formData.append("avatar", avatar);

			return await get().handleRequest(async () => {
				const res = await handleRequest<IUserDataResponse>(EHttpType.POST, `/users`, formData);

				if (res.data && res.data.success && res.data.user) {
					get().handleAddUserToTable(res.data.user);
				}

				return res;
			});
		},

		updateUser: async (
			userId: string,
			fullname: string,
			birth: string,
			summary: string,
			avatar: File | null,
			role: string,
			status: string,
			instagram: string,
			facebook: string,
			linkedin: string,
		): Promise<IApiResponse<IUserDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				fullname,
				instagram,
				facebook,
				linkedin,
				birth,
				summary,
				role,
				status,
			}));
			if (avatar) formData.append("avatar", avatar);

			return await get().handleRequest(async () => {
				const res = await handleRequest<IUserDataResponse>(EHttpType.PATCH, `/users/${userId}`, formData);
				console.log('updateUser res:', res);
				const { success, user } = res.data || {};
				const { isAdmin, userAuth, handleSetUserAuth } = useAuthStore.getState();

				if (success && user) {
					if (isAdmin) get().handleUpdateUserInTable(user);
					if (userAuth?.id === userId) handleSetUserAuth(user);
				}

				return res;
			});
		},

		deleteUser: async (userId: string): Promise<IApiResponse> => {
			// Remove from table immediately
			get().handleRemoveUserFromTable(userId);

			// Call API in background
			get().handleRequest(async () => {
				const res = await handleRequest(EHttpType.DELETE, `/users/${userId}`);
				// If API fails, we could add back, but for now, assume success
				return res;
			});

			// Return success immediately
			return { data: { success: true } } as IApiResponse;
		},

		handleRemoveUserFromTable: (userId: string): void => {
			set({
				usersTable: get().usersTable.filter((user) => user.id !== userId),
			});
		},

		handleAddUserToTable: (user: IUser): void => {
			set({ usersTable: [user, ...get().usersTable] });
		},

		handleUpdateUserInTable: (user: IUser): void => {
			set({
				usersTable: get().usersTable.map((u) =>
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