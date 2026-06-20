package com.plansystem.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Plan vs actual comparison result.
 */
@Getter
public enum ComparisonStatus {

    MATCH("MATCH", "达成"),
    PARTIAL("PARTIAL", "部分达成"),
    EXCEED("EXCEED", "超额完成"),
    NOT_MATCH("NOT_MATCH", "未达成");

    @JsonValue
    private final String code;

    private final String label;

    ComparisonStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
