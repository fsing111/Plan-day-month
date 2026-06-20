package com.plansystem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Priority enumeration.
 */
@Getter
public enum Priority {

    HIGH("HIGH", "高"),
    MEDIUM("MEDIUM", "中"),
    LOW("LOW", "低");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;

    Priority(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
