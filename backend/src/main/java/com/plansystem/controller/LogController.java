package com.plansystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.PageResult;
import com.plansystem.dto.Result;
import com.plansystem.entity.OperationLog;
import com.plansystem.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/v1/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LEADER')")
public class LogController {

    private final OperationLogMapper logMapper;

    @Operation(summary = "操作日志列表")
    @GetMapping
    public Result<PageResult<OperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<OperationLog> pageParam = new Page<>(page, Math.min(pageSize, 100));
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null) wrapper.eq(OperationLog::getUsername, username);
        if (operation != null) wrapper.eq(OperationLog::getOperation, operation);
        if (startDate != null) wrapper.ge(OperationLog::getCreatedAt, startDate + " 00:00:00");
        if (endDate != null) wrapper.le(OperationLog::getCreatedAt, endDate + " 23:59:59");
        wrapper.orderByDesc(OperationLog::getCreatedAt);

        IPage<OperationLog> result = logMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result));
    }
}
