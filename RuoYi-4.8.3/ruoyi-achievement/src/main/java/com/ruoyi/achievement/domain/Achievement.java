package com.ruoyi.achievement.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 成果对象 achievement
 *
 * @author ruoyi
 */
public class Achievement extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long planId;
    private String description;
    private String actualQty;
    private BigDecimal actualHours;
    private String issues;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submittedAt;

    private String delFlag;

    // 非数据库字段
    private List<Long> planIds;
    private String planTitle;
    private String planType;
    private String planPriority;
    private String planQuantTarget;
    private String userName;
    private String deptName;
    private String comparisonStatus;
    private List<Attachment> attachments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getActualQty() { return actualQty; }
    public void setActualQty(String actualQty) { this.actualQty = actualQty; }
    public BigDecimal getActualHours() { return actualHours; }
    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }
    public String getIssues() { return issues; }
    public void setIssues(String issues) { this.issues = issues; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<Long> getPlanIds() { return planIds; }
    public void setPlanIds(List<Long> planIds) { this.planIds = planIds; }
    public String getPlanTitle() { return planTitle; }
    public void setPlanTitle(String planTitle) { this.planTitle = planTitle; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public String getPlanPriority() { return planPriority; }
    public void setPlanPriority(String planPriority) { this.planPriority = planPriority; }
    public String getPlanQuantTarget() { return planQuantTarget; }
    public void setPlanQuantTarget(String planQuantTarget) { this.planQuantTarget = planQuantTarget; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getComparisonStatus() { return comparisonStatus; }
    public void setComparisonStatus(String comparisonStatus) { this.comparisonStatus = comparisonStatus; }
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
}
