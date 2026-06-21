package com.ruoyi.plan.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 计划对象 plan
 *
 * @author ruoyi
 */
public class Plan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 提交人ID */
    @Excel(name = "提交人ID")
    private Long userId;

    /** 计划类型：DAILY/WEEKLY/MONTHLY */
    @Excel(name = "计划类型", readConverterExp = "DAILY=日报,WEEKLY=周报,MONTHLY=月报")
    private String planType;

    /** 计划标题 */
    @Excel(name = "计划标题")
    private String title;

    /** 详细描述（HTML） */
    private String description;

    /** 优先级：HIGH/MEDIUM/LOW */
    @Excel(name = "优先级", readConverterExp = "HIGH=高,MEDIUM=中,LOW=低")
    private String priority;

    /** 状态：DRAFT/SUBMITTED/APPROVING/APPROVED/REJECTED/ARCHIVED/OVERDUE */
    @Excel(name = "状态", readConverterExp = "DRAFT=草稿,SUBMITTED=已提交,APPROVING=审批中,APPROVED=已通过,REJECTED=待修改,ARCHIVED=已归档,OVERDUE=已逾期")
    private String status;

    /** 计划开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "开始时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 计划截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "截止时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 所属项目分类ID */
    private Long categoryId;

    /** 量化指标描述 */
    private String quantTarget;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    // ---------- 非数据库字段，用于列表展示 ----------

    /** 提交人姓名 */
    private String userName;

    /** 提交人所属部门名 */
    private String deptName;

    /** 分类名称 */
    private String categoryName;

    /** 是否已有成果 */
    private Boolean hasAchievement;

    /** 成果ID */
    private Long achievementId;

    /** 关联引用的计划ID列表 */
    private List<Long> refPlanIds;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getQuantTarget() { return quantTarget; }
    public void setQuantTarget(String quantTarget) { this.quantTarget = quantTarget; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Boolean getHasAchievement() { return hasAchievement; }
    public void setHasAchievement(Boolean hasAchievement) { this.hasAchievement = hasAchievement; }

    public Long getAchievementId() { return achievementId; }
    public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }

    public List<Long> getRefPlanIds() { return refPlanIds; }
    public void setRefPlanIds(List<Long> refPlanIds) { this.refPlanIds = refPlanIds; }
}
