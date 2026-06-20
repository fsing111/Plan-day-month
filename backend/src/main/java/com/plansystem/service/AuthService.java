package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.dto.LoginRequest;
import com.plansystem.dto.LoginResponse;
import com.plansystem.dto.UserInfo;
import com.plansystem.entity.User;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.UserMapper;
import com.plansystem.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Authentication service handling login, logout, and token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * Authenticate user and return JWT token.
     */
    public LoginResponse login(LoginRequest request) {
        // Find user by username
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // Check if account is enabled
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // Generate JWT token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("User logged in: username={}, role={}", user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration())
                .user(UserInfo.from(user))
                .build();
    }

    /**
     * Logout user by adding token to Redis blacklist.
     */
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        long remainingTtl = jwtUtils.getTokenRemainingTtl(token);
        if (remainingTtl > 0) {
            String blacklistKey = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "1", remainingTtl, TimeUnit.SECONDS);
            log.info("Token blacklisted, TTL={}s", remainingTtl);
        }
    }

    /**
     * Get current user info from database.
     */
    public UserInfo getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return UserInfo.from(user);
    }
}
