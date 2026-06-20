package com.plansystem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * User role enumeration.
 */
@Getter
public enum UserRole {

    EMPLOYEE("EMPLOYEE", "员工"),
    LEADER("LEADER", "领导"),
    ADMIN("ADMIN", "管理员");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;

    UserRole(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
