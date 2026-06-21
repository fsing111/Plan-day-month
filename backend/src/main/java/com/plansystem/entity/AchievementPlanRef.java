package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Achievement-Plan reference entity (many-to-many).
 */
@Data
@TableName("achievement_plan_ref")
public class AchievementPlanRef {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long achievementId;
    private Long planId;
    private LocalDateTime createdAt;
}
