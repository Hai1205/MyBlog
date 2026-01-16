import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";
import { createStore, EStorageType, IBaseStore } from "@/lib/initialStore";

export interface IStatsResponse {
	dashboardStats?: IDashboardStats;
	statsReport?: string | Uint8Array; // Can be Base64 string or byte array
}

export interface IStatsStore extends IBaseStore {
	// Cached data
	dashboardStats: IDashboardStats | null;
	statsReport: Blob | null;
	lastStatsFetchTime: number | null;
	lastReportFetchTime: number | null;

	// Actions
	getDashboardStats: () => Promise<IApiResponse<IStatsResponse>>;
	getStatsReport: (forceRefresh?: boolean) => Promise<Blob | null>;
	fetchDashboardStatsInBackground: () => Promise<void>;
	fetchReportInBackground: () => Promise<void>;

	handleGetStatsReport: (statsReport?: string | Uint8Array) => Promise<Blob | null>;
}

const storeName = "stats";
const initialState = {
	dashboardStats: null,
	statsReport: null,
	lastStatsFetchTime: null,
	lastReportFetchTime: null,
};

// Cache expiration time: 5 minutes
const CACHE_DURATION = 5 * 60 * 1000;

export const useStatsStore = createStore<IStatsStore>(
	storeName,
	initialState,
	(set, get) => ({
		getDashboardStats: async (): Promise<IApiResponse<IStatsResponse>> => {
			return await get().handleRequest(async () => {
				const res = await handleRequest<IStatsResponse>(EHttpType.GET, `/stats/`);

				// Cache the result in store
				if (res.data && res.data.success) {
					set({
						dashboardStats: res.data.dashboardStats,
						lastStatsFetchTime: Date.now()
					});
				} return res;
			});
		},

		getStatsReport: async (forceRefresh: boolean = false): Promise<Blob | null> => {
			const now = Date.now();

			// Return cached report if available and not forcing refresh
			if (!forceRefresh && get().statsReport && get().lastReportFetchTime) {
				const cacheAge = now - (get().lastReportFetchTime || 0);
				if (cacheAge < CACHE_DURATION) {
					console.log("Using cached report");
					return get().statsReport;
				}
			}

			// Use handleRequest to get the report data
			const res = await handleRequest<IStatsResponse>(EHttpType.GET, `/stats/report`);

			return get().handleGetStatsReport(res.data?.statsReport);
		},

		fetchDashboardStatsInBackground: async (): Promise<void> => {
			const state = get();
			const now = Date.now();

			// Check if cache is still valid
			if (state.dashboardStats && state.lastStatsFetchTime) {
				const cacheAge = now - state.lastStatsFetchTime;
				if (cacheAge < CACHE_DURATION) {
					console.log("Stats cache is still valid, skipping fetch");
					return;
				}
			}

			// Check if already loading
			if (state.isLoading) {
				return;
			}

			await get().getDashboardStats();
		},

		fetchReportInBackground: async (): Promise<void> => {
			const state = get();
			const now = Date.now();

			// Check if cache is still valid
			if (state.statsReport && state.lastReportFetchTime) {
				const cacheAge = now - state.lastReportFetchTime;
				if (cacheAge < CACHE_DURATION) {
					console.log("Report cache is still valid, skipping fetch");
					return;
				}
			}

			// Check if already loading
			if (state.isLoading) {
				return;
			}

			// Refresh report in background without showing loading
			await get().getStatsReport(true);
		}, reset: () => {
			set({ ...initialState });
		},

		handleGetStatsReport: async (statsReport?: string | Uint8Array): Promise<Blob | null> => {
			if (statsReport) {
				let blob: Blob;

				// Check if statsReport is a Base64 string or byte array
				if (typeof statsReport === 'string') {
					// Decode Base64 string to binary
					const binaryString = atob(statsReport);
					const bytes = new Uint8Array(binaryString.length);
					for (let i = 0; i < binaryString.length; i++) {
						bytes[i] = binaryString.charCodeAt(i);
					}
					blob = new Blob([bytes], { type: 'application/pdf' });
				} else if (Array.isArray(statsReport)) {
					// Convert byte array to Blob
					const uint8Array = new Uint8Array(statsReport);
					blob = new Blob([uint8Array], { type: 'application/pdf' });
				} else {
					return null;
				}

				set({
					statsReport: blob,
					lastReportFetchTime: Date.now(),
				});
				return blob;
			}

			return null;
		},
	}),
	{ storageType: EStorageType.LOCAL }
);