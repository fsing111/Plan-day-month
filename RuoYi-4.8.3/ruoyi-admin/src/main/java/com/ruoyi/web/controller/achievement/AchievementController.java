package com.ruoyi.web.controller.achievement;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.achievement.domain.Achievement;
import com.ruoyi.achievement.service.IAchievementService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;

@Controller
@RequestMapping("/achievement/achievement")
public class AchievementController extends BaseController {
    private String prefix = "achievement/achievement";

    @Autowired
    private IAchievementService achievementService;

    @RequiresPermissions("achievement:achievement:view")
    @GetMapping()
    public String achievement() { return prefix + "/achievement"; }

    @RequiresPermissions("achievement:achievement:view")
    @GetMapping("/add")
    public String add() { return prefix + "/add"; }

    @RequiresPermissions("achievement:achievement:view")
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("achievement", achievementService.selectAchievementById(id));
        return prefix + "/detail";
    }

    @RequiresPermissions("achievement:achievement:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Achievement achievement) {
        startPage();
        List<Achievement> list = achievementService.selectAchievementList(achievement);
        return getDataTable(list);
    }

    @RequiresPermissions("achievement:achievement:add")
    @Log(title = "成果管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Achievement achievement) {
        achievement.setCreateBy(ShiroUtils.getLoginName());
        achievementService.insertAchievement(achievement);
        return success();
    }

    @RequiresPermissions("achievement:achievement:edit")
    @Log(title = "成果管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Achievement achievement) {
        achievement.setUpdateBy(ShiroUtils.getLoginName());
        achievementService.updateAchievement(achievement);
        return success();
    }

    @RequiresPermissions("achievement:achievement:submit")
    @PostMapping("/submit/{id}")
    @ResponseBody
    public AjaxResult submit(@PathVariable("id") Long id) {
        achievementService.submitAchievement(id);
        return success();
    }

    @RequiresPermissions("achievement:achievement:withdraw")
    @PostMapping("/withdraw/{id}")
    @ResponseBody
    public AjaxResult withdraw(@PathVariable("id") Long id) {
        achievementService.withdrawAchievement(id);
        return success();
    }

    @RequiresPermissions("achievement:achievement:remove")
    @Log(title = "成果管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        for (String id : ids.split(",")) {
            achievementService.deleteAchievement(Long.valueOf(id));
        }
        return success();
    }
}
