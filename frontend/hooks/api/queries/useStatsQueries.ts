import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { queryKeys } from "@/lib/queryClient";
import { statsService, IStatsResponse } from "../services/statsService";
import { IApiResponse } from "@/lib/axiosInstance";
import { useMemo } from "react";

/**
 * Stats Queries - for GET requests
 */

/**
 * Get dashboard statistics with automatic caching
 */
export const useDashboardStatsQuery = (
    options?: {
        enabled?: boolean;
        refetchInterval?: number;
    }
): UseQueryResult<IApiResponse<IStatsResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.stats.dashboard(),
        queryFn: statsService.getDashboardStats,
        enabled: options?.enabled ?? true,
        staleTime: 5 * 60 * 1000, // 5 minutes
        refetchInterval: options?.refetchInterval,
    });
};

/**
 * Get stats report (PDF) with automatic caching
 */
export const useStatsReportQuery = (
    options?: {
        enabled?: boolean;
    }
): UseQueryResult<IApiResponse<IStatsResponse>, Error> => {
    return useQuery({
        queryKey: queryKeys.stats.reports(),
        queryFn: statsService.getStatsReport,
        enabled: options?.enabled ?? true,
        staleTime: 5 * 60 * 1000, // 5 minutes
    });
};

/**
 * Hook to get stats report as Blob
 * Converts the report data to downloadable Blob
 */
export const useStatsReportBlob = (
    options?: {
        enabled?: boolean;
    }
): {
    blob: Blob | null;
    isLoading: boolean;
    error: Error | null;
} => {
    const { data, isLoading, error } = useStatsReportQuery(options);

    const blob = useMemo(() => {
        if (!data?.data?.statsReport) return null;
        return statsService.convertToBlob(data.data.statsReport);
    }, [data]);

    return {
        blob,
        isLoading,
        error,
    };
};
