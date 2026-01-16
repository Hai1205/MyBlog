import { useState, useCallback, useEffect } from "react";
import { PaginationData } from "@/components/commons/layout/pagination/PaginationControls";

export interface UsePaginationOptions {
    initialPage?: number;
    initialPageSize?: number;
    onPageChange?: (page: number) => void;
    onPageSizeChange?: (size: number) => void;
}

export interface PaginationState {
    page: number;
    pageSize: number;
    totalPages: number;
    totalElements: number;
    hasNext: boolean;
    hasPrevious: boolean;
}

export interface UsePaginationReturn {
    paginationState: PaginationState;
    paginationData: PaginationData;
    setPage: (page: number) => void;
    setPageSize: (size: number) => void;
    nextPage: () => void;
    prevPage: () => void;
    goToFirst: () => void;
    goToLast: () => void;
    updateTotalPages: (total: number) => void;
    updateTotalElements: (total: number) => void;
    setBackendResponse: (response: {
        totalPages: number;
        totalElements: number;
        currentPage: number;
        pageSize: number;
        hasNext: boolean;
        hasPrevious: boolean;
    }) => void;
}

export function usePagination(options: UsePaginationOptions = {}): UsePaginationReturn {
    const {
        initialPage = 1,
        initialPageSize = 10,
        onPageChange,
        onPageSizeChange,
    } = options;

    const [paginationState, setPaginationState] = useState<PaginationState>({
        page: initialPage,
        pageSize: initialPageSize,
        totalPages: 1,
        totalElements: 0,
        hasNext: false,
        hasPrevious: false,
    });

    const setPage = useCallback(
        (page: number) => {
            setPaginationState((prev) => ({
                ...prev,
                page,
                hasNext: page < prev.totalPages,
                hasPrevious: page > 1,
            }));
            onPageChange?.(page);
        },
        [onPageChange]
    );

    const setPageSize = useCallback(
        (size: number) => {
            setPaginationState((prev) => ({
                ...prev,
                pageSize: size,
                page: 1, // Reset to first page when changing page size
                totalPages: Math.ceil(prev.totalElements / size),
                hasNext: 1 < Math.ceil(prev.totalElements / size),
                hasPrevious: false,
            }));
            onPageSizeChange?.(size);
        },
        [onPageSizeChange]
    );

    const nextPage = useCallback(() => {
        setPaginationState((prev) => {
            if (!prev.hasNext) return prev;
            const newPage = prev.page + 1;
            return {
                ...prev,
                page: newPage,
                hasNext: newPage < prev.totalPages,
                hasPrevious: true,
            };
        });
    }, []);

    const prevPage = useCallback(() => {
        setPaginationState((prev) => {
            if (!prev.hasPrevious) return prev;
            const newPage = prev.page - 1;
            return {
                ...prev,
                page: newPage,
                hasNext: true,
                hasPrevious: newPage > 1,
            };
        });
    }, []);

    const goToFirst = useCallback(() => {
        setPage(1);
    }, [setPage]);

    const goToLast = useCallback(() => {
        setPaginationState((prev) => {
            const lastPage = prev.totalPages;
            return {
                ...prev,
                page: lastPage,
                hasNext: false,
                hasPrevious: lastPage > 1,
            };
        });
    }, []);

    const updateTotalPages = useCallback((total: number) => {
        setPaginationState((prev) => ({
            ...prev,
            totalPages: total,
            hasNext: prev.page < total,
            hasPrevious: prev.page > 1,
        }));
    }, []);

    const updateTotalElements = useCallback((total: number) => {
        setPaginationState((prev) => {
            const newTotalPages = Math.ceil(total / prev.pageSize);
            return {
                ...prev,
                totalElements: total,
                totalPages: newTotalPages,
                hasNext: prev.page < newTotalPages,
                hasPrevious: prev.page > 1,
            };
        });
    }, []);

    const setBackendResponse = useCallback(
        (response: {
            totalPages: number;
            totalElements: number;
            currentPage: number;
            pageSize: number;
            hasNext: boolean;
            hasPrevious: boolean;
        }) => {
            setPaginationState({
                page: response.currentPage,
                pageSize: response.pageSize,
                totalPages: response.totalPages,
                totalElements: response.totalElements,
                hasNext: response.hasNext,
                hasPrevious: response.hasPrevious,
            });
        },
        []
    );

    const paginationData: PaginationData = {
        currentPage: paginationState.page,
        totalPages: paginationState.totalPages,
        totalElements: paginationState.totalElements,
        pageSize: paginationState.pageSize,
        hasNext: paginationState.hasNext,
        hasPrevious: paginationState.hasPrevious,
    };

    return {
        paginationState,
        paginationData,
        setPage,
        setPageSize,
        nextPage,
        prevPage,
        goToFirst,
        goToLast,
        updateTotalPages,
        updateTotalElements,
        setBackendResponse,
    };
}
