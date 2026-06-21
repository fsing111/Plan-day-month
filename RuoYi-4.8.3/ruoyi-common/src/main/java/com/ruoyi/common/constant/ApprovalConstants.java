package com.ruoyi.common.constant;

/**
 * 审批流常量
 *
 * @author ruoyi
 */
public class ApprovalConstants {

    /** 审批操作 */
    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_TRANSFER = "TRANSFER";

    /** 审批目标类型 */
    public static final String TARGET_PLAN = "PLAN";
    public static final String TARGET_ACHIEVEMENT = "ACHIEVEMENT";

    /** 通知类型 */
    public static final String NOTIFY_PLAN_REJECTED = "PLAN_REJECTED";
    public static final String NOTIFY_PLAN_APPROVED = "PLAN_APPROVED";
    public static final String NOTIFY_NEW_APPROVAL_PLAN = "NEW_APPROVAL_PLAN";
    public static final String NOTIFY_NEW_APPROVAL_ACHV = "NEW_APPROVAL_ACHV";
    public static final String NOTIFY_REMIND_SUBMIT_PLAN = "REMIND_SUBMIT_PLAN";
    public static final String NOTIFY_REMIND_APPROVE = "REMIND_APPROVE";
    public static final String NOTIFY_ACHV_REJECTED = "ACHV_REJECTED";
    public static final String NOTIFY_ACHV_APPROVED = "ACHV_APPROVED";
    public static final String NOTIFY_TRANSFER = "TRANSFER";

    /** 角色 */
    public static final String ROLE_EMPLOYEE = "plan_employee";
    public static final String ROLE_LEADER = "plan_leader";

    /** 角色ID */
    public static final Long ROLE_ID_EMPLOYEE = 200L;
    public static final Long ROLE_ID_LEADER = 201L;

    /** 操作日志类型 */
    public static final String OP_CREATE = "CREATE";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_DELETE = "DELETE";
    public static final String OP_SUBMIT = "SUBMIT";
    public static final String OP_WITHDRAW = "WITHDRAW";
    public static final String OP_APPROVE = "APPROVE";
    public static final String OP_REJECT = "REJECT";
    public static final String OP_TRANSFER = "TRANSFER";
}
