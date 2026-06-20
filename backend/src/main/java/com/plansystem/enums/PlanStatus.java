package com.plansystem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Plan status enumeration.
 */
@Getter
public enum PlanStatus {

    DRAFT("DRAFT", "草稿"),
    SUBMITTED("SUBMITTED", "已提交"),
    APPROVING("APPROVING", "审批中"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;

    PlanStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
