package com.plansystem.controller;

import com.plansystem.dto.PageResult;
import com.plansystem.dto.PlanCreateRequest;
import com.plansystem.dto.PlanVO;
import com.plansystem.dto.Result;
import com.plansystem.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Plan controller.
 */
@Tag(name = "计划管理", description = "计划的增删改查、提交、日历视图和汇总引用")
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @Operation(summary = "查询计划列表")
    @GetMapping
    public Result<PageResult<PlanVO>> list(
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<PlanVO> result = planService.getPlanList(
                page, pageSize, planType, status, priority,
                startDate, endDate, categoryId, userId, keyword);
        return Result.success(result);
    }

    @Operation(summary = "查看计划详情")
    @GetMapping("/{id}")
    public Result<PlanVO> detail(@PathVariable Long id) {
        PlanVO vo = planService.getPlanVOById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建计划")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody PlanCreateRequest request) {
        planService.createPlan(request);
        return Result.success("创建成功", null);
    }

    @Operation(summary = "修改计划")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PlanCreateRequest request) {
        planService.updatePlan(id, request);
        return Result.success("修改成功", null);
    }

    @Operation(summary = "提交计划")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        planService.submitPlan(id);
        return Result.success("提交成功", null);
    }

    @Operation(summary = "删除计划")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        planService.deletePlan(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "日历视图数据")
    @GetMapping("/calendar")
    public Result<Map<String, List<PlanVO>>> calendar(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String planType) {
        Map<String, List<PlanVO>> data = planService.getCalendarData(year, month, planType);
        return Result.success(data);
    }

    @Operation(summary = "获取可引用的下级计划")
    @GetMapping("/{id}/rollup-options")
    public Result<List<PlanVO>> rollupOptions(
            @PathVariable Long id,
            @RequestParam String planType,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<PlanVO> options = planService.getRollupOptions(id, planType, startDate, endDate);
        return Result.success(options);
    }
}
