package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Plan/Achievement comment entity.
 */
@Data
@TableName("plan_comment")
public class PlanComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long targetId;
    private String targetType;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
