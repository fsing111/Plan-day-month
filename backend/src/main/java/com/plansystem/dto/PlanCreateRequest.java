package com.plansystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Plan create/update request DTO.
 */
@Data
public class PlanCreateRequest {

    @NotBlank(message = "计划类型不能为空")
    private String planType;

    @NotBlank(message = "计划标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "优先级不能为空")
    private String priority;

    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @NotNull(message = "结束时间不能为空")
    private String endTime;

    private Long categoryId;
    private String quantTarget;
    private List<Long> refPlanIds;
    private Boolean submitDirectly;
}
