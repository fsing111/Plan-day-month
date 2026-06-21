package com.ruoyi.web.controller.plan;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/plan/log")
public class OperationLogController {
    private String prefix = "plan/log";

    @RequiresPermissions("plan:log:view")
    @GetMapping()
    public String log() { return prefix + "/log"; }
}
