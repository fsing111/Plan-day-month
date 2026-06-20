package com.plansystem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Notification type enumeration.
 */
@Getter
public enum NotificationType {

    PLAN_REJECTED("PLAN_REJECTED", "计划被驳回"),
    PLAN_APPROVED("PLAN_APPROVED", "计划通过终审"),
    NEW_APPROVAL_PLAN("NEW_APPROVAL_PLAN", "有新的计划待审批"),
    NEW_APPROVAL_ACHV("NEW_APPROVAL_ACHV", "有新的成果待验收"),
    REMIND_SUBMIT_PLAN("REMIND_SUBMIT_PLAN", "提醒提交计划"),
    REMIND_APPROVE("REMIND_APPROVE", "提醒审批超时"),
    ACHV_REJECTED("ACHV_REJECTED", "成果被驳回"),
    ACHV_APPROVED("ACHV_APPROVED", "成果通过终审");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;

    NotificationType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
