package com.plansystem.security;

import com.plansystem.utils.JwtUtils;
import com.plansystem.utils.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT authentication filter that runs once per request.
 * Extracts JWT from Authorization header, validates it,
 * and sets the Spring Security authentication context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                // Check if token is blacklisted
                String blacklistKey = BLACKLIST_PREFIX + token;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                    log.debug("Token is blacklisted");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Check if token is expired
                if (jwtUtils.isTokenExpired(token)) {
                    log.debug("Token is expired");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extract user info and set context
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                UserContext.setUserId(userId);
                UserContext.setUsername(username);
                UserContext.setRole(role);

                // Set Spring Security authentication
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                authentication.setDetails(username);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authenticated: userId={}, role={}", userId, role);
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear thread-local after request completes
            UserContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
