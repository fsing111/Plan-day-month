package com.plansystem.controller;

import com.plansystem.dto.Result;
import com.plansystem.dto.UserInfo;
import com.plansystem.service.AuthService;
import com.plansystem.service.DepartmentService;
import com.plansystem.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User controller for profile and user queries.
 */
@Tag(name = "用户管理", description = "个人中心和用户查询")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final DepartmentService departmentService;

    @Operation(summary = "获取个人中心信息")
    @GetMapping("/profile")
    public Result<UserInfo> getProfile() {
        Long userId = UserContext.getUserId();
        UserInfo userInfo = authService.getCurrentUser(userId);
        // Enrich with department name
        userInfo.setDeptName(departmentService.getDeptName(userInfo.getDeptId()));
        return Result.success(userInfo);
    }
}
