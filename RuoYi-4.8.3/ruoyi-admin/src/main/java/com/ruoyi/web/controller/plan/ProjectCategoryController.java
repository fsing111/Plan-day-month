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
import com.ruoyi.plan.domain.ProjectCategory;
import com.ruoyi.plan.mapper.ProjectCategoryMapper;

@Controller
@RequestMapping("/plan/category")
public class ProjectCategoryController extends BaseController {
    private String prefix = "plan/category";

    @Autowired
    private ProjectCategoryMapper categoryMapper;

    @RequiresPermissions("plan:category:view")
    @GetMapping()
    public String category() { return prefix + "/category"; }

    @RequiresPermissions("plan:category:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ProjectCategory category) {
        startPage();
        List<ProjectCategory> list = categoryMapper.selectProjectCategoryList(category);
        return getDataTable(list);
    }

    @RequiresPermissions("plan:category:view")
    @GetMapping("/all")
    @ResponseBody
    public AjaxResult all() {
        List<ProjectCategory> list = categoryMapper.selectProjectCategoryList(new ProjectCategory());
        return AjaxResult.success(list);
    }

    @RequiresPermissions("plan:category:view")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(ProjectCategory category) {
        category.setCreateBy(ShiroUtils.getLoginName());
        categoryMapper.insertProjectCategory(category);
        return success();
    }

    @RequiresPermissions("plan:category:view")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(ProjectCategory category) {
        category.setUpdateBy(ShiroUtils.getLoginName());
        categoryMapper.updateProjectCategory(category);
        return success();
    }

    @RequiresPermissions("plan:category:view")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        for (String id : ids.split(",")) {
            categoryMapper.deleteProjectCategoryById(Long.valueOf(id));
        }
        return success();
    }
}
