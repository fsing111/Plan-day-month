package com.ruoyi.web.controller.approval;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.approval.domain.ApprovalRecord;
import com.ruoyi.approval.service.IApprovalService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

@Controller
@RequestMapping("/approval")
public class ApprovalController extends BaseController {
    private String prefix = "approval";

    @Autowired
    private IApprovalService approvalService;

    @RequiresPermissions("approval:approval:pending")
    @GetMapping("/pending")
    public String pending() { return prefix + "/pending"; }

    @RequiresPermissions("approval:approval:history")
    @GetMapping("/history")
    public String history() { return prefix + "/history"; }

    @RequiresPermissions("approval:chain:view")
    @GetMapping("/chain")
    public String chain() { return prefix + "/chain"; }

    @RequiresPermissions("approval:approval:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ApprovalRecord record) {
        startPage();
        List<ApprovalRecord> list = approvalService.selectPendingList(record);
        return getDataTable(list);
    }

    @PostMapping("/history/list")
    @ResponseBody
    @RequiresPermissions("approval:approval:history")
    public TableDataInfo historyList(ApprovalRecord record) {
        startPage();
        List<ApprovalRecord> list = approvalService.selectHistoryList(record);
        return getDataTable(list);
    }

    @GetMapping("/timeline/{type}/{id}")
    @ResponseBody
    public AjaxResult timeline(@PathVariable("type") String type, @PathVariable("id") Long id) {
        List<ApprovalRecord> list = approvalService.getTimeline(type, id);
        return AjaxResult.success(list);
    }

    @RequiresPermissions("approval:approval:approve")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PostMapping("/approve/{recordId}")
    @ResponseBody
    public AjaxResult approve(@PathVariable Long recordId, @RequestParam(defaultValue = "") String comment) {
        approvalService.approve(recordId, comment);
        return success();
    }

    @RequiresPermissions("approval:approval:reject")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{recordId}")
    @ResponseBody
    public AjaxResult reject(@PathVariable Long recordId, String comment) {
        approvalService.reject(recordId, comment);
        return success();
    }

    @RequiresPermissions("approval:approval:transfer")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PostMapping("/transfer/{recordId}")
    @ResponseBody
    public AjaxResult transfer(@PathVariable Long recordId, Long targetUserId, String comment) {
        approvalService.transfer(recordId, targetUserId, comment);
        return success();
    }

    @RequiresPermissions("approval:approval:batchApprove")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-approve")
    @ResponseBody
    public AjaxResult batchApprove(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> recordIds = ((List<Integer>) body.get("recordIds")).stream()
                .map(Long::valueOf).toList();
        String comment = (String) body.getOrDefault("comment", "");
        Map<String, Object> result = approvalService.batchApprove(recordIds, comment);
        return AjaxResult.success(result);
    }
}
