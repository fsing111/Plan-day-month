-- ============================================================
-- 日/月计划和成果验收系统 - 数据库初始化脚本
-- 数据库: plan_system  |  字符集: utf8mb4
-- 此文件在 MySQL 容器首次启动时由 /docker-entrypoint-initdb.d/ 自动执行
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS plan_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE plan_system;

-- ============================================================
-- 1. department 部门表
-- ============================================================
CREATE TABLE IF NOT EXISTS department (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    name        VARCHAR(100)  NOT NULL                     COMMENT '部门名称',
    parent_id   BIGINT        DEFAULT NULL                 COMMENT '父部门ID（NULL=顶级部门）',
    sort_order  INT           DEFAULT 0                    COMMENT '排序号',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================================
-- 2. users 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    username    VARCHAR(50)   NOT NULL UNIQUE              COMMENT '登录用户名',
    password    VARCHAR(255)  NOT NULL                     COMMENT '加密密码（BCrypt）',
    real_name   VARCHAR(50)   NOT NULL                     COMMENT '真实姓名',
    role        VARCHAR(20)   NOT NULL DEFAULT 'EMPLOYEE'  COMMENT '角色：EMPLOYEE/LEADER/ADMIN',
    dept_id     BIGINT        NOT NULL                     COMMENT '所属部门ID',
    leader_id   BIGINT        DEFAULT NULL                 COMMENT '直属领导ID（自引用）',
    email       VARCHAR(100)  DEFAULT NULL                 COMMENT '邮箱（预留）',
    phone       VARCHAR(20)   DEFAULT NULL                 COMMENT '手机号（预留）',
    avatar_url  VARCHAR(255)  DEFAULT NULL                 COMMENT '头像URL',
    enabled     TINYINT       DEFAULT 1                    COMMENT '是否启用：1启用/0禁用',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_dept_id (dept_id),
    INDEX idx_leader_id (leader_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 3. project_category 项目分类表
-- ============================================================
CREATE TABLE IF NOT EXISTS project_category (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    name        VARCHAR(100)  NOT NULL                     COMMENT '分类名称',
    dept_id     BIGINT        DEFAULT NULL                 COMMENT '所属部门ID（NULL=全局）',
    sort_order  INT           DEFAULT 0                    COMMENT '排序号',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目分类表';

-- ============================================================
-- 4. plan 计划表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan (
    id             BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    user_id        BIGINT        NOT NULL                     COMMENT '提交人ID',
    plan_type      VARCHAR(10)   NOT NULL                     COMMENT '计划类型：DAILY/WEEKLY/MONTHLY',
    title          VARCHAR(200)  NOT NULL                     COMMENT '计划标题',
    description    TEXT          DEFAULT NULL                 COMMENT '详细描述（富文本HTML）',
    priority       VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM'    COMMENT '优先级：HIGH/MEDIUM/LOW',
    status         VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'     COMMENT '状态：DRAFT/SUBMITTED/APPROVING/APPROVED/REJECTED',
    start_time     DATETIME      NOT NULL                     COMMENT '计划开始时间',
    end_time       DATETIME      NOT NULL                     COMMENT '计划截止时间',
    category_id    BIGINT        DEFAULT NULL                 COMMENT '所属项目分类ID',
    quant_target   VARCHAR(200)  DEFAULT NULL                 COMMENT '量化指标描述',
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_plan_type (plan_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_category_id (category_id),
    INDEX idx_user_plan_type (user_id, plan_type),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划表';

-- ============================================================
-- 5. achievement 成果表
-- ============================================================
CREATE TABLE IF NOT EXISTS achievement (
    id             BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    plan_id        BIGINT        NOT NULL UNIQUE              COMMENT '关联计划ID（一对一）',
    description    TEXT          NOT NULL                     COMMENT '完成说明（富文本HTML）',
    actual_qty     VARCHAR(200)  DEFAULT NULL                 COMMENT '实际完成数量',
    actual_hours   DECIMAL(5,1)  DEFAULT NULL                 COMMENT '实际耗时（小时）',
    issues         TEXT          DEFAULT NULL                 COMMENT '遇到的问题',
    remark         TEXT          DEFAULT NULL                 COMMENT '备注',
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING'   COMMENT '状态：PENDING/SUBMITTED/APPROVING/APPROVED/REJECTED',
    submitted_at   DATETIME      DEFAULT NULL                 COMMENT '提交时间',
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_plan_id (plan_id),
    INDEX idx_status (status),
    FOREIGN KEY (plan_id) REFERENCES plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果表';

-- ============================================================
-- 6. approval_chain 审批链配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS approval_chain (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    dept_id         BIGINT        NOT NULL                     COMMENT '所属部门ID',
    plan_type       VARCHAR(20)   NOT NULL                     COMMENT '适用类型：DAILY/WEEKLY/MONTHLY/ACHIEVEMENT',
    approval_level  INT           NOT NULL                     COMMENT '审批级别（1,2,3...）',
    approver_id     BIGINT        NOT NULL                     COMMENT '审批人ID',
    sort_order      INT           DEFAULT 0                    COMMENT '同级别内审批顺序',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dept_plan_type (dept_id, plan_type),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批链配置表';

-- ============================================================
-- 7. approval_record 审批记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS approval_record (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    target_id       BIGINT        NOT NULL                     COMMENT '审批目标ID（plan.id 或 achievement.id）',
    target_type     VARCHAR(20)   NOT NULL                     COMMENT '审批目标类型：PLAN/ACHIEVEMENT',
    approver_id     BIGINT        NOT NULL                     COMMENT '审批人ID',
    approval_level  INT           NOT NULL                     COMMENT '当前审批级别',
    action          VARCHAR(20)   DEFAULT NULL                 COMMENT '审批操作：APPROVE/REJECT/TRANSFER（NULL=待审批）',
    comment         TEXT          DEFAULT NULL                 COMMENT '审批意见',
    approved_at     DATETIME      DEFAULT NULL                 COMMENT '审批时间',
    sort_order      INT           DEFAULT 0                    COMMENT '同级别审批顺序',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    INDEX idx_target (target_id, target_type),
    INDEX idx_approver (approver_id),
    INDEX idx_approver_action (approver_id, action),
    INDEX idx_target_level (target_id, target_type, approval_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ============================================================
-- 8. notification 通知表
-- ============================================================
CREATE TABLE IF NOT EXISTS notification (
    id             BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    receiver_id    BIGINT        NOT NULL                     COMMENT '接收人ID',
    type           VARCHAR(30)   NOT NULL                     COMMENT '通知类型',
    title          VARCHAR(200)  NOT NULL                     COMMENT '通知标题',
    content        TEXT          DEFAULT NULL                 COMMENT '通知内容',
    is_read        TINYINT       DEFAULT 0                    COMMENT '是否已读：0未读/1已读',
    related_id     BIGINT        DEFAULT NULL                 COMMENT '关联业务ID（可点击跳转）',
    related_type   VARCHAR(20)   DEFAULT NULL                 COMMENT '关联业务类型：PLAN/ACHIEVEMENT',
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '通知时间',
    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_receiver_time (receiver_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================================
-- 9. attachment 附件表
-- ============================================================
CREATE TABLE IF NOT EXISTS attachment (
    id               BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    achievement_id   BIGINT        NOT NULL                     COMMENT '关联成果ID',
    file_name        VARCHAR(255)  NOT NULL                     COMMENT '原始文件名',
    file_path        VARCHAR(500)  NOT NULL                     COMMENT '服务器存储路径',
    file_size        BIGINT        NOT NULL                     COMMENT '文件大小（字节）',
    file_type        VARCHAR(50)   DEFAULT NULL                 COMMENT 'MIME类型',
    uploaded_at      DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '上传时间',
    INDEX idx_achievement_id (achievement_id),
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- ============================================================
-- 10. plan_weekly_ref 日报-周报关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_weekly_ref (
    id               BIGINT   PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    weekly_plan_id   BIGINT   NOT NULL                     COMMENT '周报计划ID',
    daily_plan_id    BIGINT   NOT NULL                     COMMENT '日报计划ID',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    UNIQUE KEY uk_weekly_daily (weekly_plan_id, daily_plan_id),
    INDEX idx_weekly (weekly_plan_id),
    INDEX idx_daily (daily_plan_id),
    FOREIGN KEY (weekly_plan_id) REFERENCES plan(id),
    FOREIGN KEY (daily_plan_id) REFERENCES plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日报-周报关联表';

-- ============================================================
-- 11. plan_monthly_ref 周报-月报关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_monthly_ref (
    id                BIGINT   PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    monthly_plan_id   BIGINT   NOT NULL                     COMMENT '月报计划ID',
    weekly_plan_id    BIGINT   NOT NULL                     COMMENT '周报计划ID',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    UNIQUE KEY uk_monthly_weekly (monthly_plan_id, weekly_plan_id),
    INDEX idx_monthly (monthly_plan_id),
    INDEX idx_weekly (weekly_plan_id),
    FOREIGN KEY (monthly_plan_id) REFERENCES plan(id),
    FOREIGN KEY (weekly_plan_id) REFERENCES plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周报-月报关联表';

-- ============================================================
-- 种子数据
-- ============================================================

-- 插入默认部门
INSERT INTO department (id, name, parent_id, sort_order) VALUES
(1, '默认部门', NULL, 0);

-- 插入管理员账号 (admin / admin123)
INSERT INTO users (id, username, password, real_name, role, dept_id, leader_id) VALUES
(1, 'admin', '$2a$10$VaQKUiNIwlVc.d5RqShevO8mwhwXyhwC0DbrzjhfZ/AfEJtpnPoSy', '系统管理员', 'ADMIN', 1, NULL);

-- 插入默认分类
INSERT INTO project_category (id, name, dept_id, sort_order) VALUES
(1, '日常运维', NULL, 1),
(2, '项目开发', NULL, 2),
(3, '会议培训', NULL, 3),
(4, '其他', NULL, 99);
