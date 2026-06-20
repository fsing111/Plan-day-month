package com.plansystem.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Plan view object returned to frontend.
 */
@Data
@Builder
public class PlanVO {

    private Long id;
    private Long userId;
    private String userName;
    private String planType;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String startTime;
    private String endTime;
    private Long categoryId;
    private String categoryName;
    private String quantTarget;
    private Boolean hasAchievement;
    private Long achievementId;
    private String createdAt;
}
