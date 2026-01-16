import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";
import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";
import Cookies from 'js-cookie';
import { useUserStore } from "./userStore";
import { useStatsStore } from "./statsStore";
import { EUserRole } from "../types/enum";

interface IAuthDataResponse {
	user: IUser;
	isActive: boolean;
}

export interface IAuthStore extends IBaseStore {
	userAuth: IUser | null;
	isAdmin: boolean;

	register: (fullname: string, username: string, email: string, password: string) => Promise<IApiResponse>;
	login: (identifier: string, password: string) => Promise<IApiResponse<IAuthDataResponse>>;
	logout: () => Promise<IApiResponse>;
	RefreshToken: () => Promise<IApiResponse>;
	sendOTP: (identifier: string) => Promise<IApiResponse>;
	verifyOTP: (identifier: string, otp: string, isActivation: boolean) => Promise<IApiResponse>;
	resetPassword: (email: string) => Promise<IApiResponse>;
	forgotPassword: (identifier: string, password: string, confirmPassword: string) => Promise<IApiResponse>;
	changePassword: (identifier: string, oldPassword: string, password: string, confirmPassword: string) => Promise<IApiResponse>;

	handleSetUserAuth: (user: IUser) => void;
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
		RefreshToken: async (): Promise<IApiResponse> => {
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, "/auth/refresh-token");
			});
		},

		register: async (fullname: string, username: string, email: string, password: string): Promise<IApiResponse<IAuthDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				fullname,
				username,
				email,
				password,
			}));

			return await get().handleRequest(async () => {
				const res = await handleRequest<IAuthDataResponse>(EHttpType.POST, `/auth/register`, formData);
				console.log("Registration res:", res);
				return res;
			});
		}, 
		
		login: async (identifier: string, password: string): Promise<IApiResponse<IAuthDataResponse>> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				identifier,
				password,
			}));

			return await get().handleRequest(async () => {
				const res = await handleRequest<IAuthDataResponse>(EHttpType.POST, `/auth/login`, formData);
				const { success, user } = res.data || {};

				if (success && user) {
					console.log("Logged in user:", user);
					set({
						userAuth: user,
						isAdmin: user?.role === EUserRole.ADMIN,
					});

					if (user?.role === EUserRole.ADMIN) {
						useStatsStore.getState().fetchDashboardStatsInBackground();
						useStatsStore.getState().fetchReportInBackground();
						useUserStore.getState().fetchAllUsersInBackground();
					}
				}

				return res;
			});
		}, 
		
		logout: async (): Promise<IApiResponse> => {
			return await get().handleRequest(async () => {
				const res = await handleRequest(EHttpType.POST, `/auth/logout`);

				// Clear all auth cookies
				Cookies.remove('access_token');
				Cookies.remove('refresh_token');

				get().reset();

				return res;
			});
		},

		sendOTP: async (identifier: string): Promise<IApiResponse> => {
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, `/auth/send-otp/${identifier}`);
			});
		},

		verifyOTP: async (identifier: string, otp: string, isActivation: boolean): Promise<IApiResponse> => {
			console.log("Verifying OTP for identifier:", identifier);
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				otp,
				isActivation,
			}));
			console.log("Identifier in verifyOTP:", identifier);

			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.POST, `/auth/verify-otp/${identifier}`, formData);
			});
		}, resetPassword: async (email: string): Promise<IApiResponse> => {
			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.PATCH, `/auth/reset-password/${email}`, {});
			});
		},

		forgotPassword: async (identifier: string, password: string, confirmPassword: string): Promise<IApiResponse> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				password,
				confirmPassword,
			}));

			return await get().handleRequest(async () => {
				return await handleRequest(EHttpType.PATCH, `/auth/forgot-password/${identifier}`, formData);
			});
		},

		changePassword: async (identifier: string, currentPassword: string, newPassword: string, confirmPassword: string): Promise<IApiResponse> => {
			const formData = new FormData();
			formData.append("data", JSON.stringify({
				currentPassword,
				newPassword,
				confirmPassword
			}));

			return await get().handleRequest(async () => {
				const res = await handleRequest(EHttpType.PATCH, `/auth/change-password/${identifier}`, formData);
				console.log("Change password res:", res);
				return res;
			});
		}, handleSetUserAuth: (user: IUser): void => {
			if (user) {
				set({
					userAuth: user,
					isAdmin: user.role === EUserRole.ADMIN
				});
			}
		},

		reset: () => {
			set({ ...initialState });
			useUserStore.getState().reset();
			useStatsStore.getState().reset();
		},
	}),
	{ storageType: EStorageType.COOKIE }
);