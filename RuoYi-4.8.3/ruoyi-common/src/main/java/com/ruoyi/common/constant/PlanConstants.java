package com.ruoyi.common.constant;

/**
 * 计划管理常量
 *
 * @author ruoyi
 */
public class PlanConstants {

    /** 计划类型 */
    public static final String TYPE_DAILY = "DAILY";
    public static final String TYPE_WEEKLY = "WEEKLY";
    public static final String TYPE_MONTHLY = "MONTHLY";

    /** 计划状态 */
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVING = "APPROVING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";
    public static final String STATUS_OVERDUE = "OVERDUE";

    /** 优先级 */
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    /** 可编辑的状态：草稿、待修改（驳回） */
    public static final String[] EDITABLE_STATUSES = {STATUS_DRAFT, STATUS_REJECTED};

    /** 可提交的状态 */
    public static final String[] SUBMITTABLE_STATUSES = {STATUS_DRAFT, STATUS_REJECTED};

    /** 可撤回的状态：已提交、审批中（且无人审批） */
    public static final String[] WITHDRAWABLE_STATUSES = {STATUS_SUBMITTED, STATUS_APPROVING};

    /** 审批目标类型 */
    public static final String APPROVAL_TARGET_PLAN = "PLAN";

    /** 软删除标记 */
    public static final String DEL_FLAG_NORMAL = "0";
    public static final String DEL_FLAG_DELETED = "2";
}
