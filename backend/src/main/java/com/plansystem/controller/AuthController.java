package com.plansystem.controller;

import com.plansystem.dto.LoginRequest;
import com.plansystem.dto.LoginResponse;
import com.plansystem.dto.Result;
import com.plansystem.dto.UserInfo;
import com.plansystem.service.AuthService;
import com.plansystem.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller providing login, logout, and user info endpoints.
 */
@Tag(name = "认证管理", description = "用户登录、登出和当前用户信息")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * User login.
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * User logout.
     */
    @Operation(summary = "用户登出", description = "登出当前用户，Token加入黑名单")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        authService.logout(token);
        return Result.success("已登出", null);
    }

    /**
     * Get current authenticated user info.
     */
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserInfo> getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        UserInfo userInfo = authService.getCurrentUser(userId);
        return Result.success(userInfo);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
