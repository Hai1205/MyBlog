import { EHttpType, handleRequest, IApiResponse } from "@/lib/axiosInstance";

// Response interfaces
export interface IStatsResponse {
    dashboardStats?: IDashboardStats;
    statsReport?: string | Uint8Array;
}

/**
 * Stats Service - Pure API calls without state management
 */
export const statsService = {
    /**
     * Get dashboard statistics
     */
    getDashboardStats: async (): Promise<IApiResponse<IStatsResponse>> => {
        return await handleRequest<IStatsResponse>(EHttpType.GET, `/stats/`);
    },

    /**
     * Get stats report (PDF)
     */
    getStatsReport: async (): Promise<IApiResponse<IStatsResponse>> => {
        return await handleRequest<IStatsResponse>(EHttpType.GET, `/stats/report`);
    },

    /**
     * Convert stats report data to Blob
     */
    convertToBlob: (statsReport?: string | Uint8Array): Blob | null => {
        if (!statsReport) return null;

        let blob: Blob;

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

        return blob;
    },
};
