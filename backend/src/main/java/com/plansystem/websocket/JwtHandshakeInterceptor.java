package com.plansystem.websocket;

import com.plansystem.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * JWT handshake interceptor — validates token before allowing WebSocket connection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // Try query parameter first (SockJS may use this)
            String token = servletRequest.getServletRequest().getParameter("token");

            // Fallback to Authorization header
            if (token == null || token.isEmpty()) {
                String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (token != null && !token.isEmpty()) {
                try {
                    if (!jwtUtils.isTokenExpired(token)) {
                        Long userId = jwtUtils.getUserIdFromToken(token);
                        String role = jwtUtils.getRoleFromToken(token);
                        attributes.put("userId", userId);
                        attributes.put("role", role);
                        log.debug("WebSocket handshake authenticated: userId={}", userId);
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("WebSocket JWT validation failed: {}", e.getMessage());
                }
            }
        }
        log.warn("WebSocket handshake rejected: no valid token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }
}
