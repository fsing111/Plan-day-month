package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Plan template entity.
 */
@Data
@TableName("plan_template")
public class PlanTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String planType;
    private String title;
    private String description;
    private String priority;
    private Long categoryId;
    private String quantTarget;
    private LocalDateTime createdAt;
}
