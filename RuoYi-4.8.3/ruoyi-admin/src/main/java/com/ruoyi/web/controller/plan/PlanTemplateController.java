package com.ruoyi.web.controller.plan;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.plan.domain.PlanTemplate;
import com.ruoyi.plan.mapper.PlanTemplateMapper;

@Controller
@RequestMapping("/plan/template")
public class PlanTemplateController extends BaseController {
    private String prefix = "plan/template";

    @Autowired
    private PlanTemplateMapper templateMapper;

    @RequiresPermissions("plan:template:view")
    @GetMapping()
    public String template() { return prefix + "/template"; }

    @RequiresPermissions("plan:template:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(PlanTemplate template) {
        startPage();
        template.setUserId(ShiroUtils.getUserId());
        List<PlanTemplate> list = templateMapper.selectPlanTemplateList(template);
        return getDataTable(list);
    }

    @RequiresPermissions("plan:template:view")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(PlanTemplate template) {
        template.setUserId(ShiroUtils.getUserId());
        template.setCreateBy(ShiroUtils.getLoginName());
        templateMapper.insertPlanTemplate(template);
        return success();
    }

    @RequiresPermissions("plan:template:view")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        for (String id : ids.split(",")) {
            templateMapper.deletePlanTemplateById(Long.valueOf(id));
        }
        return success();
    }
}
