package com.ruoyi.achievement.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class AchievementPlanRef extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long achievementId;
    private Long planId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAchievementId() { return achievementId; }
    public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
}
