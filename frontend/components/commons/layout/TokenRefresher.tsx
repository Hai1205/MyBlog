"use client";

import { useEffect, useRef } from "react";
import { useAuthStore } from "@/stores/authStore";

const TOKEN_REFRESH_INTERVAL = 10 * 60 * 1000; // 10 minutes in milliseconds

// Helper to check if refresh token exists
const hasRefreshToken = (): boolean => {
  if (typeof document === "undefined") return false;

  const getCookie = (name: string): string | null => {
    const matches = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`));
    return matches ? matches[2] : null;
  };

  return !!(
    getCookie("refresh_token") ||
    localStorage.getItem("refresh_token") ||
    sessionStorage.getItem("refresh_token")
  );
};

export function TokenRefresher() {
  const { RefreshToken, userAuth } = useAuthStore();
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const hasRunInitialRefresh = useRef(false);

  useEffect(() => {
    // Only run if user is authenticated AND refresh token exists
    if (!userAuth || !hasRefreshToken()) {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      hasRunInitialRefresh.current = false;
      return;
    }

    console.log("Token refresher started - will refresh every 10 minutes");

    // Refresh token immediately on mount if user is logged in
    const performRefresh = async () => {
      // Double check refresh token still exists before calling
      if (!hasRefreshToken()) {
        console.log("No refresh token found, skipping refresh");
        if (intervalRef.current) {
          clearInterval(intervalRef.current);
          intervalRef.current = null;
        }
        return;
      }

      try {
        await RefreshToken();
        console.log("Token refreshed successfully");
      } catch (error) {
        console.error("Failed to refresh token:", error);
        // Stop the interval if refresh fails
        if (intervalRef.current) {
          clearInterval(intervalRef.current);
          intervalRef.current = null;
        }
      }
    };

    // Skip initial refresh if just logged in (within 30 seconds)
    // This prevents immediate refresh right after login
    if (!hasRunInitialRefresh.current) {
      hasRunInitialRefresh.current = true;
      console.log("⏭️ Skipping initial refresh - user just logged in");
    }

    // Set up interval to refresh every 10 minutes
    intervalRef.current = setInterval(performRefresh, TOKEN_REFRESH_INTERVAL);

    // Cleanup on unmount
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
        console.log("🛑 Token refresher stopped");
      }
    };
  }, [userAuth, RefreshToken]);

  // This component doesn't render anything
  return null;
}
