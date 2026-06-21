package com.ruoyi.web.controller.plan;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/plan/recycle")
public class RecycleBinController {
    private String prefix = "plan/recycle";

    @RequiresPermissions("plan:recycle:view")
    @GetMapping()
    public String recycle() { return prefix + "/recycle"; }
}
