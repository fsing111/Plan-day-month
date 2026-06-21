package com.ruoyi.web.controller.approval;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.approval.domain.ApprovalChain;
import com.ruoyi.approval.mapper.ApprovalChainMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.ShiroUtils;

@Controller
@RequestMapping("/approval/chain")
public class ApprovalChainController extends BaseController {

    @Autowired
    private ApprovalChainMapper chainMapper;

    @RequiresPermissions("approval:chain:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ApprovalChain chain) {
        startPage();
        List<ApprovalChain> list = chainMapper.selectApprovalChainList(chain);
        return getDataTable(list);
    }

    @RequiresPermissions("approval:chain:view")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(ApprovalChain chain) {
        chain.setCreateBy(ShiroUtils.getLoginName());
        chainMapper.insertApprovalChain(chain);
        return success();
    }

    @RequiresPermissions("approval:chain:view")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(ApprovalChain chain) {
        chain.setUpdateBy(ShiroUtils.getLoginName());
        chainMapper.updateApprovalChain(chain);
        return success();
    }

    @RequiresPermissions("approval:chain:view")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        for (String id : ids.split(",")) {
            chainMapper.deleteById(Long.valueOf(id));
        }
        return success();
    }
}
