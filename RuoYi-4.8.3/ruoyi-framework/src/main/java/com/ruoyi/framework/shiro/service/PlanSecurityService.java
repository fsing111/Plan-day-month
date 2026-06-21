package com.ruoyi.framework.shiro.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.constant.ApprovalConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.ShiroUtils;

/**
 * 计划系统安全工具服务
 * 提供当前用户角色判断和数据权限范围解析
 *
 * @author ruoyi
 */
@Component("planSecurity")
public class PlanSecurityService {

    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        return ShiroUtils.getUserId();
    }

    /**
     * 获取当前登录用户名
     */
    public String getCurrentLoginName() {
        SysUser user = ShiroUtils.getSysUser();
        return user != null ? user.getLoginName() : "";
    }

    /**
     * 判断当前用户是否为部门领导
     */
    public boolean isLeader() {
        SysUser user = ShiroUtils.getSysUser();
        if (user == null) {
            return false;
        }
        return ShiroUtils.getSubject().hasRole(ApprovalConstants.ROLE_LEADER);
    }

    /**
     * 判断当前用户是否为员工
     */
    public boolean isEmployee() {
        SysUser user = ShiroUtils.getSysUser();
        if (user == null) {
            return false;
        }
        return ShiroUtils.getSubject().hasRole(ApprovalConstants.ROLE_EMPLOYEE);
    }

    /**
     * 获取当前用户的角色标识（返回 null 表示 admin 或未登录）
     * @return "LEADER" 或 "EMPLOYEE"，null 表示管理员
     */
    public String getCurrentRole() {
        SysUser user = ShiroUtils.getSysUser();
        if (user == null || user.isAdmin()) {
            return null;
        }
        if (ShiroUtils.getSubject().hasRole(ApprovalConstants.ROLE_LEADER)) {
            return "LEADER";
        }
        return "EMPLOYEE";
    }

    /**
     * 获取当前用户可见的用户ID列表
     * - 员工：仅能看到自己
     * - 领导：能看到自己及所有下属（后续需递归查询）
     *
     * @param allSubordinateIds 领导的所有下属ID列表（由调用方提供，可为null）
     * @return 可见用户ID列表，null 表示管理员无限制
     */
    public List<Long> getVisibleUserIds(List<Long> allSubordinateIds) {
        SysUser user = ShiroUtils.getSysUser();
        if (user == null) {
            return List.of();
        }
        // 管理员看全部
        if (user.isAdmin()) {
            return null;
        }
        // 领导看自己+下属
        if (isLeader()) {
            List<Long> ids = new ArrayList<>();
            ids.add(user.getUserId());
            if (allSubordinateIds != null) {
                ids.addAll(allSubordinateIds);
            }
            return ids;
        }
        // 员工只看自己
        return List.of(user.getUserId());
    }
}
