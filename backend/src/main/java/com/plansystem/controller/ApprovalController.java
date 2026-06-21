package com.plansystem.controller;

import com.plansystem.dto.ApprovalVO;
import com.plansystem.dto.Result;
import com.plansystem.dto.PageResult;
import com.plansystem.entity.ApprovalRecord;
import com.plansystem.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "审批管理")
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @Operation(summary = "待审批列表")
    @GetMapping("/pending")
    public Result<List<ApprovalVO>> pending(@RequestParam(required = false) String targetType) {
        return Result.success(approvalService.getPendingApprovals(targetType));
    }

    @Operation(summary = "审批时间线")
    @GetMapping("/{type}/{id}/timeline")
    public Result<List<ApprovalRecord>> timeline(@PathVariable String type, @PathVariable Long id) {
        return Result.success(approvalService.getApprovalTimeline(type, id));
    }

    @Operation(summary = "通过审批")
    @PostMapping("/{recordId}/approve")
    public Result<Void> approve(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        approvalService.approve(recordId, body.get("comment"));
        return Result.success("审批通过", null);
    }

    @Operation(summary = "驳回审批（须填写原因）")
    @PostMapping("/{recordId}/reject")
    public Result<Void> reject(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        String comment = body.get("comment");
        if (comment == null || comment.isBlank()) {
            return Result.error(5002, "驳回必须填写具体原因");
        }
        approvalService.reject(recordId, comment);
        return Result.success("已驳回", null);
    }

    @Operation(summary = "转审")
    @PostMapping("/{recordId}/transfer")
    public Result<Void> transfer(@PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        String comment = (String) body.get("comment");
        approvalService.transfer(recordId, targetUserId, comment);
        return Result.success("已转审", null);
    }

    @Operation(summary = "批量通过审批")
    @PostMapping("/batch-approve")
    public Result<Map<String, Object>> batchApprove(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("recordIds");
        List<Long> recordIds = rawIds.stream().map(Long::valueOf).toList();
        String comment = (String) body.getOrDefault("comment", "批量通过");
        Map<String, Object> result = approvalService.batchApprove(recordIds, comment);
        return Result.success(result);
    }

    @Operation(summary = "审批历史")
    @GetMapping("/history")
    public Result<PageResult<ApprovalVO>> history(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String action) {
        return Result.success(approvalService.getApprovalHistory(page, pageSize, targetType, action));
    }
}
