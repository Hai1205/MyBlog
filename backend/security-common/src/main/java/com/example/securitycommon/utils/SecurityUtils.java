package com.example.securitycommon.utils;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.securitycommon.models.AuthenticatedUser;

public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get the current authenticated user from the security context
     * 
     * @return AuthenticatedUser or null if not authenticated
     */
    public static AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get the current user ID
     * 
     * @return UUID of the current user or null if not authenticated
     */
    public static UUID getCurrentUserId() {
        AuthenticatedUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get the current user email
     * 
     * @return email of the current user or null if not authenticated
     */
    public static String getCurrentUserEmail() {
        AuthenticatedUser user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Get the current user role
     * 
     * @return role of the current user or null if not authenticated
     */
    public static String getCurrentUserRole() {
        AuthenticatedUser user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * Check if the current user has a specific role
     * 
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        AuthenticatedUser user = getCurrentUser();
        return user != null && user.hasRole(role);
    }

    /**
     * Check if there is an authenticated user
     * 
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUser;
    }
}
