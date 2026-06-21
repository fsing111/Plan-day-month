package com.ruoyi.web.controller.plan;

import java.util.*;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.framework.shiro.service.PlanSecurityService;
import com.ruoyi.plan.domain.Plan;
import com.ruoyi.plan.service.IPlanService;

/**
 * 计划管理Controller
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/plan/plan")
public class PlanController extends BaseController {

    private String prefix = "plan/plan";

    @Autowired
    private IPlanService planService;

    @Autowired
    private PlanSecurityService planSecurity;

    // ==================== 页面渲染 ====================

    @RequiresPermissions("plan:plan:view")
    @GetMapping()
    public String plan() {
        return prefix + "/plan";
    }

    @RequiresPermissions("plan:plan:view")
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("categories", new ArrayList<>()); // 由前端JS加载
        return prefix + "/add";
    }

    @RequiresPermissions("plan:plan:view")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        Plan plan = planService.selectPlanById(id);
        mmap.put("plan", plan);
        return prefix + "/add"; // 复用add页面
    }

    @RequiresPermissions("plan:plan:view")
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, ModelMap mmap) {
        Plan plan = planService.selectPlanById(id);
        mmap.put("plan", plan);
        return prefix + "/detail";
    }

    // ==================== API 接口 ====================

    @RequiresPermissions("plan:plan:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Plan plan) {
        // 设置数据权限
        plan.getParams().put("visibleUserIds",
                planSecurity.getVisibleUserIds(null));
        startPage();
        List<Plan> list = planService.selectPlanList(plan);
        return getDataTable(list);
    }

    @RequiresPermissions("plan:plan:add")
    @Log(title = "计划管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Plan plan) {
        plan.setCreateBy(ShiroUtils.getLoginName());
        planService.insertPlan(plan);
        return success();
    }

    @RequiresPermissions("plan:plan:edit")
    @Log(title = "计划管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Plan plan) {
        plan.setUpdateBy(ShiroUtils.getLoginName());
        planService.updatePlan(plan);
        return success();
    }

    @RequiresPermissions("plan:plan:submit")
    @Log(title = "计划管理", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{id}")
    @ResponseBody
    public AjaxResult submit(@PathVariable("id") Long id) {
        planService.submitPlan(id);
        return success();
    }

    @RequiresPermissions("plan:plan:withdraw")
    @Log(title = "计划管理", businessType = BusinessType.UPDATE)
    @PostMapping("/withdraw/{id}")
    @ResponseBody
    public AjaxResult withdraw(@PathVariable("id") Long id) {
        planService.withdrawPlan(id);
        return success();
    }

    @RequiresPermissions("plan:plan:remove")
    @Log(title = "计划管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        for (String id : ids.split(",")) {
            planService.deletePlan(Long.valueOf(id));
        }
        return success();
    }

    @RequiresPermissions("plan:plan:copy")
    @PostMapping("/copy/{id}")
    @ResponseBody
    public AjaxResult copy(@PathVariable("id") Long id) {
        Plan copy = planService.copyPlan(id);
        return AjaxResult.success(copy);
    }

    @RequiresPermissions("plan:plan:view")
    @GetMapping("/calendar-data")
    @ResponseBody
    public AjaxResult calendarData(Integer year, Integer month, String planType) {
        Map<String, List<Plan>> data = planService.getCalendarData(year, month, planType);
        return AjaxResult.success(data);
    }

    @RequiresPermissions("plan:plan:view")
    @GetMapping("/rollup-options")
    @ResponseBody
    public AjaxResult rollupOptions(Long planId, String planType, String startDate, String endDate) {
        List<Plan> options = planService.getRollupOptions(planId, planType, startDate, endDate);
        return AjaxResult.success(options);
    }
}
