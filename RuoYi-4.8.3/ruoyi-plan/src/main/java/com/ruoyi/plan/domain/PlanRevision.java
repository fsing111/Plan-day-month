package com.ruoyi.plan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 计划修改版本记录 plan_revision
 *
 * @author ruoyi
 */
public class PlanRevision extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long planId;
    private Integer version;
    private String changes;
    private String submitterNote;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getChanges() { return changes; }
    public void setChanges(String changes) { this.changes = changes; }

    public String getSubmitterNote() { return submitterNote; }
    public void setSubmitterNote(String submitterNote) { this.submitterNote = submitterNote; }
}
