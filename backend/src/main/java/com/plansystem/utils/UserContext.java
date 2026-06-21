package com.plansystem.utils;

import java.util.Set;

/**
 * Thread-local holder for current authenticated user information.
 * Used to pass user context through the request lifecycle without
 * passing it through every method parameter.
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();
    private static final ThreadLocal<Long> DEPT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> PERMISSIONS = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void setRole(String role) {
        ROLE.set(role);
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static void setDeptId(Long deptId) {
        DEPT_ID.set(deptId);
    }

    public static Long getDeptId() {
        return DEPT_ID.get();
    }

    public static void setPermissions(Set<String> permissions) {
        PERMISSIONS.set(permissions);
    }

    public static Set<String> getPermissions() {
        return PERMISSIONS.get();
    }

    /**
     * Check if current user has leader role (admin functions merged into leader role).
     */
    public static boolean isLeader() {
        return "LEADER".equals(ROLE.get());
    }

    /**
     * Clear all thread-local values. Should be called after request completes.
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLE.remove();
        DEPT_ID.remove();
        PERMISSIONS.remove();
    }
}
