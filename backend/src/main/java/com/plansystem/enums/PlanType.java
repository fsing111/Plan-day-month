package com.plansystem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Plan type enumeration.
 */
@Getter
public enum PlanType {

    DAILY("DAILY", "日报"),
    WEEKLY("WEEKLY", "周报"),
    MONTHLY("MONTHLY", "月报");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;

    PlanType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
