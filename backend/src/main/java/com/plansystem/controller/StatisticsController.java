package com.plansystem.controller;

import com.plansystem.dto.Result;
import com.plansystem.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Operation(summary = "个人统计")
    @GetMapping("/personal")
    public Result<Map<String, Object>> personal() {
        return Result.success(statisticsService.getPersonalStats());
    }

    @Operation(summary = "团队统计")
    @GetMapping("/team")
    public Result<Map<String, Object>> team() {
        return Result.success(statisticsService.getTeamStats());
    }

    @Operation(summary = "趋势数据")
    @GetMapping("/trend")
    public Result<Object> trend() {
        return Result.success("趋势数据功能实现中");
    }
}
