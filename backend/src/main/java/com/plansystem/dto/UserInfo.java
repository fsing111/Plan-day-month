package com.plansystem.dto;

import com.plansystem.entity.User;
import lombok.Data;

/**
 * User information returned to frontend (excludes sensitive fields).
 */
@Data
public class UserInfo {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private Long deptId;
    private String deptName;
    private Long leaderId;
    private String leaderName;
    private String avatarUrl;

    /**
     * Build UserInfo from User entity.
     */
    public static UserInfo from(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setRealName(user.getRealName());
        info.setRole(user.getRole());
        info.setDeptId(user.getDeptId());
        info.setLeaderId(user.getLeaderId());
        info.setAvatarUrl(user.getAvatarUrl());
        return info;
    }
}
