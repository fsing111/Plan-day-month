package com.ruoyi.plan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 评论对象 plan_comment
 *
 * @author ruoyi
 */
public class PlanComment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long targetId;
    private String targetType;
    private Long userId;
    private String content;

    /** 非数据库字段 */
    private String userName;
    private String avatar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
