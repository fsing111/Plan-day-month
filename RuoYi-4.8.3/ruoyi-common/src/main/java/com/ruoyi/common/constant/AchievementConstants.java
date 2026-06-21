package com.ruoyi.common.constant;

/**
 * 成果管理常量
 *
 * @author ruoyi
 */
public class AchievementConstants {

    /** 成果状态 */
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVING = "APPROVING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    /** 可编辑的状态 */
    public static final String[] EDITABLE_STATUSES = {STATUS_PENDING, STATUS_REJECTED};

    /** 可提交的状态 */
    public static final String[] SUBMITTABLE_STATUSES = {STATUS_PENDING, STATUS_REJECTED};

    /** 可撤回的状态 */
    public static final String[] WITHDRAWABLE_STATUSES = {STATUS_SUBMITTED, STATUS_APPROVING};

    /** 审批目标类型 */
    public static final String APPROVAL_TARGET_ACHIEVEMENT = "ACHIEVEMENT";

    /** 对比状态 */
    public static final String COMPARISON_MATCH = "MATCH";
    public static final String COMPARISON_PARTIAL = "PARTIAL";
    public static final String COMPARISON_EXCEED = "EXCEED";
    public static final String COMPARISON_NOT_MATCH = "NOT_MATCH";

    /** 软删除标记 */
    public static final String DEL_FLAG_NORMAL = "0";
    public static final String DEL_FLAG_DELETED = "2";
}
