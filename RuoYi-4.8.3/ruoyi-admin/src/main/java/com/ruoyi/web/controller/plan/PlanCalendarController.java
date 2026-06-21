package com.ruoyi.web.controller.plan;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/plan/calendar")
public class PlanCalendarController {
    private String prefix = "plan/calendar";

    @RequiresPermissions("plan:calendar:view")
    @GetMapping()
    public String calendar() { return prefix + "/calendar"; }
}
