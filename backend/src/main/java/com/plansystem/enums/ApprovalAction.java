package com.plansystem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Approval action enumeration.
 */
@Getter
public enum ApprovalAction {

    APPROVE("APPROVE", "通过"),
    REJECT("REJECT", "驳回"),
    TRANSFER("TRANSFER", "转审");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;

    ApprovalAction(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
