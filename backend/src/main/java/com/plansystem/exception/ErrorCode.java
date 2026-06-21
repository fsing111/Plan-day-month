package com.plansystem.exception;

import lombok.Getter;

/**
 * System error codes.
 */
@Getter
public enum ErrorCode {

    // Common errors
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限执行该操作"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // Business errors
    APPROVAL_CHAIN_NOT_CONFIGURED(1001, "审批链未配置"),
    TRANSFER_TARGET_NOT_IN_SAME_DEPT(1002, "转审目标不在同部门"),
    APPROVAL_ALREADY_PROCESSED(1003, "该审批记录已被处理"),
    FILE_SIZE_EXCEEDED(1004, "文件大小超限（最大10MB）"),
    ATTACHMENT_COUNT_EXCEEDED(1005, "附件数量超限（最多5个）"),
    FILE_TYPE_NOT_SUPPORTED(1006, "文件格式不支持"),

    // Plan errors
    PLAN_STATUS_NOT_EDITABLE(2001, "当前计划状态不可编辑"),
    PLAN_ALREADY_SUBMITTED(2002, "计划已提交，不可重复提交"),
    PLAN_NOT_APPROVED(2003, "计划未通过审批，不可提交成果"),
    PLAN_DUPLICATE_DAILY(2004, "今日已有日报，请勿重复提交"),
    PLAN_DUPLICATE_WEEKLY(2005, "本周已有周报，请勿重复提交"),
    PLAN_DUPLICATE_MONTHLY(2006, "本月已有月报，请勿重复提交"),
    PLAN_CANNOT_WITHDRAW(2007, "已有审批人处理，无法撤回"),

    // Achievement errors
    ACHIEVEMENT_ALREADY_EXISTS(3001, "该计划已有成果"),
    ACHIEVEMENT_STATUS_NOT_EDITABLE(3002, "当前成果状态不可编辑"),
    ACHIEVEMENT_CANNOT_WITHDRAW(3003, "已有审批人处理，无法撤回"),

    // User errors
    USERNAME_ALREADY_EXISTS(4001, "用户名已存在"),
    PASSWORD_ERROR(4002, "密码错误"),
    USER_DISABLED(4003, "账号已被禁用"),
    DEPARTMENT_HAS_USERS(4004, "部门下存在用户，不可删除"),

    // Approval errors
    NOT_APPROVER(5001, "您不是该记录的审批人"),
    APPROVAL_REJECT_COMMENT_REQUIRED(5002, "驳回必须填写具体原因");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
