package com.plansystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Achievement create/update request DTO.
 * Supports one-to-many achievement-plan relationship.
 */
@Data
public class AchievementCreateRequest {

    @NotEmpty(message = "至少关联一条计划")
    private List<Long> planIds;

    @NotBlank(message = "完成说明不能为空")
    private String description;

    private String actualQty;
    private BigDecimal actualHours;
    private String issues;
    private String remark;
    private Boolean submitDirectly;
}
