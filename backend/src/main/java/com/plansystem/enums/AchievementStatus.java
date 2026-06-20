package com.plansystem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Achievement status enumeration.
 */
@Getter
public enum AchievementStatus {

    PENDING("PENDING", "待填写"),
    SUBMITTED("SUBMITTED", "已提交"),
    APPROVING("APPROVING", "验收中"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;

    AchievementStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
