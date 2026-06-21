-- ============================================================
-- 日/月计划和成果验收系统 - RuoYi框架迁移SQL
-- 执行顺序：在 ry_20260319.sql 之后执行
-- ============================================================

-- ============================================================
-- 0. 扩展 RuoYi 系统表
-- ============================================================

-- sys_user 增加直属领导字段
ALTER TABLE sys_user ADD COLUMN leader_id BIGINT DEFAULT NULL COMMENT '直属领导ID（自引用）' AFTER dept_id;

-- ============================================================
-- 1. project_category 项目分类表
-- ============================================================
CREATE TABLE IF NOT EXISTS project_category (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    name        VARCHAR(100)  NOT NULL                     COMMENT '分类名称',
    dept_id     BIGINT        DEFAULT NULL                 COMMENT '所属部门ID（NULL=全局）',
    sort_order  INT           DEFAULT 0                    COMMENT '排序号',
    create_by   VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    update_by   VARCHAR(64)   DEFAULT ''                   COMMENT '更新者',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500)  DEFAULT NULL                 COMMENT '备注',
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目分类表';

-- ============================================================
-- 2. plan 计划表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan (
    id             BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    user_id        BIGINT        NOT NULL                     COMMENT '提交人ID',
    plan_type      VARCHAR(10)   NOT NULL                     COMMENT '计划类型：DAILY/WEEKLY/MONTHLY',
    title          VARCHAR(200)  NOT NULL                     COMMENT '计划标题',
    description    TEXT          DEFAULT NULL                 COMMENT '详细描述（HTML）',
    priority       VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM'    COMMENT '优先级：HIGH/MEDIUM/LOW',
    status         VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'     COMMENT '状态：DRAFT/SUBMITTED/APPROVING/APPROVED/REJECTED/ARCHIVED/OVERDUE',
    start_time     DATETIME      NOT NULL                     COMMENT '计划开始时间',
    end_time       DATETIME      NOT NULL                     COMMENT '计划截止时间',
    category_id    BIGINT        DEFAULT NULL                 COMMENT '所属项目分类ID',
    quant_target   VARCHAR(200)  DEFAULT NULL                 COMMENT '量化指标描述',
    del_flag       CHAR(1)       DEFAULT '0'                  COMMENT '删除标志（0存在 2删除）',
    create_by      VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    update_by      VARCHAR(64)   DEFAULT ''                   COMMENT '更新者',
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark         VARCHAR(500)  DEFAULT NULL                 COMMENT '备注',
    INDEX idx_user_id (user_id),
    INDEX idx_plan_type (plan_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_category_id (category_id),
    INDEX idx_user_plan_type (user_id, plan_type),
    INDEX idx_user_status (user_id, status),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划表';

-- ============================================================
-- 3. achievement 成果表
-- ============================================================
CREATE TABLE IF NOT EXISTS achievement (
    id             BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    plan_id        BIGINT        DEFAULT NULL                 COMMENT '关联计划ID（主关联，一对多由achievement_plan_ref支持）',
    description    TEXT          NOT NULL                     COMMENT '完成说明（HTML）',
    actual_qty     VARCHAR(200)  DEFAULT NULL                 COMMENT '实际完成数量',
    actual_hours   DECIMAL(5,1)  DEFAULT NULL                 COMMENT '实际耗时（小时）',
    issues         TEXT          DEFAULT NULL                 COMMENT '遇到的问题',
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING'   COMMENT '状态：PENDING/SUBMITTED/APPROVING/APPROVED/REJECTED',
    submitted_at   DATETIME      DEFAULT NULL                 COMMENT '提交时间',
    del_flag       CHAR(1)       DEFAULT '0'                  COMMENT '删除标志（0存在 2删除）',
    create_by      VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    update_by      VARCHAR(64)   DEFAULT ''                   COMMENT '更新者',
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark         VARCHAR(500)  DEFAULT NULL                 COMMENT '备注',
    INDEX idx_plan_id (plan_id),
    INDEX idx_status (status),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果表';

-- ============================================================
-- 4. achievement_plan_ref 成果-计划关联表（一对多）
-- ============================================================
CREATE TABLE IF NOT EXISTS achievement_plan_ref (
    id               BIGINT   PRIMARY KEY AUTO_INCREMENT,
    achievement_id   BIGINT   NOT NULL,
    plan_id          BIGINT   NOT NULL,
    create_by        VARCHAR(64)   DEFAULT ''                 COMMENT '创建者',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    UNIQUE KEY uk_achv_plan (achievement_id, plan_id),
    INDEX idx_achievement (achievement_id),
    INDEX idx_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果-计划关联表';

-- ============================================================
-- 5. approval_chain 审批链配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS approval_chain (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    dept_id         BIGINT        NOT NULL                     COMMENT '所属部门ID',
    plan_type       VARCHAR(20)   NOT NULL                     COMMENT '适用类型：DAILY/WEEKLY/MONTHLY/ACHIEVEMENT',
    approval_level  INT           NOT NULL                     COMMENT '审批级别（1,2,3...）',
    approver_id     BIGINT        NOT NULL                     COMMENT '审批人ID',
    sort_order      INT           DEFAULT 0                    COMMENT '同级别内审批顺序',
    create_by       VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    update_by       VARCHAR(64)   DEFAULT ''                   COMMENT '更新者',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500)  DEFAULT NULL                 COMMENT '备注',
    INDEX idx_dept_plan_type (dept_id, plan_type),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批链配置表';

-- ============================================================
-- 6. approval_record 审批记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS approval_record (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    target_id       BIGINT        NOT NULL                     COMMENT '审批目标ID',
    target_type     VARCHAR(20)   NOT NULL                     COMMENT '审批目标类型：PLAN/ACHIEVEMENT',
    approver_id     BIGINT        NOT NULL                     COMMENT '审批人ID',
    approval_level  INT           NOT NULL                     COMMENT '当前审批级别',
    action          VARCHAR(20)   DEFAULT NULL                 COMMENT '审批操作：APPROVE/REJECT/TRANSFER（NULL=待审批）',
    comment         TEXT          DEFAULT NULL                 COMMENT '审批意见',
    approved_at     DATETIME      DEFAULT NULL                 COMMENT '审批时间',
    sort_order      INT           DEFAULT 0                    COMMENT '同级别审批顺序',
    create_by       VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    update_by       VARCHAR(64)   DEFAULT ''                   COMMENT '更新者',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500)  DEFAULT NULL                 COMMENT '备注',
    INDEX idx_target (target_id, target_type),
    INDEX idx_approver (approver_id),
    INDEX idx_approver_action (approver_id, action),
    INDEX idx_target_level (target_id, target_type, approval_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ============================================================
-- 7. notification 通知表
-- ============================================================
CREATE TABLE IF NOT EXISTS notification (
    id             BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    receiver_id    BIGINT        NOT NULL                     COMMENT '接收人ID',
    type           VARCHAR(30)   NOT NULL                     COMMENT '通知类型',
    title          VARCHAR(200)  NOT NULL                     COMMENT '通知标题',
    content        TEXT          DEFAULT NULL                 COMMENT '通知内容',
    is_read        TINYINT       DEFAULT 0                    COMMENT '是否已读：0未读/1已读',
    related_id     BIGINT        DEFAULT NULL                 COMMENT '关联业务ID',
    related_type   VARCHAR(20)   DEFAULT NULL                 COMMENT '关联业务类型：PLAN/ACHIEVEMENT',
    create_by      VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_receiver_time (receiver_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================================
-- 8. attachment 附件表
-- ============================================================
CREATE TABLE IF NOT EXISTS attachment (
    id               BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    achievement_id   BIGINT        NOT NULL                     COMMENT '关联成果ID',
    file_name        VARCHAR(255)  NOT NULL                     COMMENT '原始文件名',
    file_path        VARCHAR(500)  NOT NULL                     COMMENT '服务器存储路径',
    file_size        BIGINT        NOT NULL                     COMMENT '文件大小（字节）',
    file_type        VARCHAR(50)   DEFAULT NULL                 COMMENT 'MIME类型',
    create_by        VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '上传时间',
    INDEX idx_achievement_id (achievement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- ============================================================
-- 9. plan_weekly_ref 日报-周报关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_weekly_ref (
    id               BIGINT   PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    weekly_plan_id   BIGINT   NOT NULL                     COMMENT '周报计划ID',
    daily_plan_id    BIGINT   NOT NULL                     COMMENT '日报计划ID',
    create_by        VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_weekly_daily (weekly_plan_id, daily_plan_id),
    INDEX idx_weekly (weekly_plan_id),
    INDEX idx_daily (daily_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日报-周报关联表';

-- ============================================================
-- 10. plan_monthly_ref 周报-月报关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_monthly_ref (
    id                BIGINT   PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    monthly_plan_id   BIGINT   NOT NULL                     COMMENT '月报计划ID',
    weekly_plan_id    BIGINT   NOT NULL                     COMMENT '周报计划ID',
    create_by         VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_monthly_weekly (monthly_plan_id, weekly_plan_id),
    INDEX idx_monthly (monthly_plan_id),
    INDEX idx_weekly (weekly_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周报-月报关联表';

-- ============================================================
-- 11. recycle_bin 回收站表（软删除记录）
-- ============================================================
CREATE TABLE IF NOT EXISTS recycle_bin (
    id               BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    original_table   VARCHAR(50)   NOT NULL                     COMMENT '原始表名',
    original_id      BIGINT        NOT NULL                     COMMENT '原始记录ID',
    title            VARCHAR(200)  DEFAULT NULL                 COMMENT '记录标题',
    delete_reason    VARCHAR(500)  DEFAULT NULL                 COMMENT '删除原因',
    deleted_by       BIGINT        NOT NULL                     COMMENT '删除人ID',
    can_restore_until DATETIME     DEFAULT NULL                 COMMENT '可恢复截止时间（30天）',
    create_by        VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    INDEX idx_original (original_table, original_id),
    INDEX idx_deleted_by (deleted_by),
    INDEX idx_deleted_at (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回收站表';

-- ============================================================
-- 12. plan_template 计划模板表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_template (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    name        VARCHAR(100) NOT NULL                   COMMENT '模板名称',
    plan_type   VARCHAR(10) NOT NULL                    COMMENT '计划类型',
    title       VARCHAR(200)                            COMMENT '计划标题',
    description TEXT                                    COMMENT '详细描述',
    priority    VARCHAR(10) DEFAULT 'MEDIUM'            COMMENT '优先级',
    category_id BIGINT                                  COMMENT '所属分类ID',
    quant_target VARCHAR(200)                           COMMENT '量化指标',
    create_by   VARCHAR(64)   DEFAULT ''                COMMENT '创建者',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)   DEFAULT ''                COMMENT '更新者',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500)  DEFAULT NULL              COMMENT '备注',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划模板表';

-- ============================================================
-- 13. plan_comment 计划/成果评论表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_comment (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    target_id   BIGINT        NOT NULL                     COMMENT '目标ID',
    target_type VARCHAR(20)   NOT NULL                     COMMENT '目标类型：PLAN/ACHIEVEMENT',
    user_id     BIGINT        NOT NULL                     COMMENT '评论人ID',
    content     TEXT          NOT NULL                     COMMENT '评论内容',
    create_by   VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '评论时间',
    INDEX idx_target (target_id, target_type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ============================================================
-- 14. operation_log 操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS operation_log (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    user_id     BIGINT        NOT NULL                     COMMENT '操作人ID',
    username    VARCHAR(50)   NOT NULL                     COMMENT '操作人用户名',
    operation   VARCHAR(30)   NOT NULL                     COMMENT '操作类型：CREATE/UPDATE/DELETE/SUBMIT/WITHDRAW/APPROVE/REJECT',
    target_type VARCHAR(20)   NOT NULL                     COMMENT '目标类型：PLAN/ACHIEVEMENT/APPROVAL',
    target_id   BIGINT        DEFAULT NULL                 COMMENT '目标ID',
    summary     VARCHAR(500)  DEFAULT NULL                 COMMENT '变更摘要',
    ip_address  VARCHAR(50)   DEFAULT NULL                 COMMENT '操作IP',
    create_by   VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (create_time),
    INDEX idx_operation (operation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 15. plan_revision 计划修改版本记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_revision (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    plan_id         BIGINT        NOT NULL                     COMMENT '计划ID',
    version         INT           NOT NULL                     COMMENT '版本号',
    changes         JSON          DEFAULT NULL                 COMMENT '变更字段及新旧值',
    submitter_note  TEXT          DEFAULT NULL                 COMMENT '员工填写的修改说明',
    create_by       VARCHAR(64)   DEFAULT ''                   COMMENT '创建者',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划修改版本记录';

-- ============================================================
-- 种子数据
-- ============================================================

-- 插入业务角色
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark) VALUES
(200, '普通员工', 'plan_employee', 3, '5', '0', '0', 'admin', sysdate(), '计划系统员工角色：仅可查看自己的数据'),
(201, '部门领导', 'plan_leader',   4, '4', '0', '0', 'admin', sysdate(), '计划系统领导角色：可查看本部门及以下数据，兼任管理员');

-- 插入默认分类
INSERT INTO project_category (id, name, dept_id, sort_order, create_by, create_time) VALUES
(1, '日常运维', NULL, 1, 'admin', sysdate()),
(2, '项目开发', NULL, 2, 'admin', sysdate()),
(3, '会议培训', NULL, 3, 'admin', sysdate()),
(4, '其他',     NULL, 99, 'admin', sysdate());

-- ============================================================
-- 菜单权限数据（menu_id 从 2000 开始，与 RuoYi 原有菜单不冲突）
-- ============================================================

-- 一级菜单：工作计划
INSERT INTO sys_menu VALUES ('2000', '工作计划', '0', '1', '#', '', 'M', '0', '1', '', 'fa fa-calendar', 'admin', sysdate(), '', null, '工作计划管理目录');

-- 二级菜单：计划相关
INSERT INTO sys_menu VALUES ('2001', '我的计划', '2000',   '1', '/plan/plan',        '', 'C', '0', '1', 'plan:plan:view',         'fa fa-list-alt',        'admin', sysdate(), '', null, '我的计划菜单');
INSERT INTO sys_menu VALUES ('2002', '日历视图', '2000',   '2', '/plan/calendar',     '', 'C', '0', '1', 'plan:calendar:view',      'fa fa-calendar-check-o', 'admin', sysdate(), '', null, '日历视图菜单');
INSERT INTO sys_menu VALUES ('2003', '计划模板', '2000',   '3', '/plan/template',     '', 'C', '0', '1', 'plan:template:view',      'fa fa-clone',            'admin', sysdate(), '', null, '计划模板菜单');
INSERT INTO sys_menu VALUES ('2004', '项目分类', '2000',   '4', '/plan/category',     '', 'C', '0', '1', 'plan:category:view',      'fa fa-tags',             'admin', sysdate(), '', null, '项目分类菜单');

-- 计划按钮权限
INSERT INTO sys_menu VALUES ('2010', '计划查询', '2001', '1',  '#', '', 'F', '0', '1', 'plan:plan:list',         '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2011', '计划新增', '2001', '2',  '#', '', 'F', '0', '1', 'plan:plan:add',          '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2012', '计划修改', '2001', '3',  '#', '', 'F', '0', '1', 'plan:plan:edit',         '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2013', '计划删除', '2001', '4',  '#', '', 'F', '0', '1', 'plan:plan:remove',       '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2014', '计划提交', '2001', '5',  '#', '', 'F', '0', '1', 'plan:plan:submit',       '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2015', '计划撤回', '2001', '6',  '#', '', 'F', '0', '1', 'plan:plan:withdraw',     '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2016', '计划导出', '2001', '7',  '#', '', 'F', '0', '1', 'plan:plan:export',       '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2017', '计划复制', '2001', '8',  '#', '', 'F', '0', '1', 'plan:plan:copy',         '#', 'admin', sysdate(), '', null, '');

-- 一级菜单：成果管理
INSERT INTO sys_menu VALUES ('2100', '成果管理', '0', '2', '#', '', 'M', '0', '1', '', 'fa fa-check-square', 'admin', sysdate(), '', null, '成果验收管理目录');

-- 二级菜单
INSERT INTO sys_menu VALUES ('2101', '成果列表', '2100',   '1', '/achievement/achievement', '', 'C', '0', '1', 'achievement:achievement:view', 'fa fa-check-circle', 'admin', sysdate(), '', null, '成果列表菜单');

-- 成果按钮权限
INSERT INTO sys_menu VALUES ('2110', '成果查询', '2101', '1',  '#', '', 'F', '0', '1', 'achievement:achievement:list',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2111', '成果新增', '2101', '2',  '#', '', 'F', '0', '1', 'achievement:achievement:add',     '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2112', '成果修改', '2101', '3',  '#', '', 'F', '0', '1', 'achievement:achievement:edit',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2113', '成果删除', '2101', '4',  '#', '', 'F', '0', '1', 'achievement:achievement:remove',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2114', '成果提交', '2101', '5',  '#', '', 'F', '0', '1', 'achievement:achievement:submit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2115', '成果撤回', '2101', '6',  '#', '', 'F', '0', '1', 'achievement:achievement:withdraw','#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2116', '成果导出', '2101', '7',  '#', '', 'F', '0', '1', 'achievement:achievement:export',  '#', 'admin', sysdate(), '', null, '');

-- 一级菜单：审批中心
INSERT INTO sys_menu VALUES ('2200', '审批中心', '0', '3', '#', '', 'M', '0', '1', '', 'fa fa-gavel', 'admin', sysdate(), '', null, '审批中心管理目录');

-- 二级菜单
INSERT INTO sys_menu VALUES ('2201', '待审批',     '2200', '1', '/approval/pending',  '', 'C', '0', '1', 'approval:approval:pending',  'fa fa-hourglass-half', 'admin', sysdate(), '', null, '待审批菜单');
INSERT INTO sys_menu VALUES ('2202', '审批历史',   '2200', '2', '/approval/history',  '', 'C', '0', '1', 'approval:approval:history',  'fa fa-history',         'admin', sysdate(), '', null, '审批历史菜单');
INSERT INTO sys_menu VALUES ('2203', '审批链配置', '2200', '3', '/approval/chain',    '', 'C', '0', '1', 'approval:chain:view',       'fa fa-link',            'admin', sysdate(), '', null, '审批链配置菜单');

-- 审批按钮权限
INSERT INTO sys_menu VALUES ('2210', '审批查询', '2201', '1', '#', '', 'F', '0', '1', 'approval:approval:list',          '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2211', '审批通过', '2201', '2', '#', '', 'F', '0', '1', 'approval:approval:approve',       '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2212', '审批驳回', '2201', '3', '#', '', 'F', '0', '1', 'approval:approval:reject',        '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2213', '审批转审', '2201', '4', '#', '', 'F', '0', '1', 'approval:approval:transfer',      '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2214', '批量审批', '2201', '5', '#', '', 'F', '0', '1', 'approval:approval:batchApprove',  '#', 'admin', sysdate(), '', null, '');

-- 一级菜单：统计分析
INSERT INTO sys_menu VALUES ('2300', '统计分析', '0', '4', '#', '', 'M', '0', '1', '', 'fa fa-bar-chart', 'admin', sysdate(), '', null, '统计分析管理目录');

-- 二级菜单
INSERT INTO sys_menu VALUES ('2301', '个人统计', '2300', '1', '/statistics/personal',  '', 'C', '0', '1', 'statistics:statistics:personal', 'fa fa-user',     'admin', sysdate(), '', null, '个人统计菜单');
INSERT INTO sys_menu VALUES ('2302', '团队统计', '2300', '2', '/statistics/team',      '', 'C', '0', '1', 'statistics:statistics:team',     'fa fa-users',    'admin', sysdate(), '', null, '团队统计菜单');

-- 一级菜单：消息通知
INSERT INTO sys_menu VALUES ('2400', '消息中心', '0', '5', '#', '', 'M', '0', '1', '', 'fa fa-bell', 'admin', sysdate(), '', null, '消息通知管理目录');

-- 二级菜单
INSERT INTO sys_menu VALUES ('2401', '消息通知', '2400', '1', '/notification/notification', '', 'C', '0', '1', 'notification:notification:view', 'fa fa-envelope', 'admin', sysdate(), '', null, '通知列表菜单');

-- 通知按钮权限
INSERT INTO sys_menu VALUES ('2410', '通知查询', '2401', '1', '#', '', 'F', '0', '1', 'notification:notification:list',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2411', '标记已读', '2401', '2', '#', '', 'F', '0', '1', 'notification:notification:read',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2412', '通知删除', '2401', '3', '#', '', 'F', '0', '1', 'notification:notification:remove', '#', 'admin', sysdate(), '', null, '');

-- 一级菜单：系统运维
INSERT INTO sys_menu VALUES ('2500', '系统运维', '0', '6', '#', '', 'M', '0', '1', '', 'fa fa-wrench', 'admin', sysdate(), '', null, '系统运维管理目录');

-- 二级菜单
INSERT INTO sys_menu VALUES ('2501', '回收站',     '2500', '1', '/plan/recycle',  '', 'C', '0', '1', 'plan:recycle:view',  'fa fa-trash',       'admin', sysdate(), '', null, '回收站菜单');
INSERT INTO sys_menu VALUES ('2502', '操作日志',   '2500', '2', '/plan/log',      '', 'C', '0', '1', 'plan:log:view',      'fa fa-history',      'admin', sysdate(), '', null, '操作日志菜单');

-- 回收站按钮权限
INSERT INTO sys_menu VALUES ('2510', '回收查询', '2501', '1', '#', '', 'F', '0', '1', 'plan:recycle:list',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2511', '恢复数据', '2501', '2', '#', '', 'F', '0', '1', 'plan:recycle:restore', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2512', '彻底删除', '2501', '3', '#', '', 'F', '0', '1', 'plan:recycle:remove',  '#', 'admin', sysdate(), '', null, '');

-- 操作日志按钮权限
INSERT INTO sys_menu VALUES ('2520', '日志查询', '2502', '1', '#', '', 'F', '0', '1', 'plan:log:list',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES ('2521', '日志导出', '2502', '2', '#', '', 'F', '0', '1', 'plan:log:export', '#', 'admin', sysdate(), '', null, '');

-- 仪表盘首页（工作台）
INSERT INTO sys_menu VALUES ('2600', '工作台', '0', '0', '/dashboard', '', 'C', '0', '1', 'dashboard:dashboard:view', 'fa fa-dashboard', 'admin', sysdate(), '', null, '工作台仪表盘');

-- ============================================================
-- 角色-菜单授权
-- ============================================================

-- 普通员工角色（plan_employee, menu_id=200）：可访问计划、成果、日历、模板、统计(个人)、通知、仪表盘
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(200, 2000), (200, 2001), (200, 2002), (200, 2003), -- 工作计划 + 子菜单
(200, 2010), (200, 2011), (200, 2012), (200, 2013), (200, 2014), (200, 2015), (200, 2017), -- 计划按钮
(200, 2100), (200, 2101), -- 成果管理
(200, 2110), (200, 2111), (200, 2112), (200, 2113), (200, 2114), (200, 2115), -- 成果按钮
(200, 2300), (200, 2301), -- 统计分析(个人)
(200, 2400), (200, 2401), (200, 2410), (200, 2411), -- 消息中心
(200, 2600); -- 工作台

-- 部门领导角色（plan_leader, menu_id=201）：拥有员工全部权限 + 审批 + 团队统计 + 审批链 + 系统运维 + 项目分类
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(201, 2000), (201, 2001), (201, 2002), (201, 2003), (201, 2004),
(201, 2010), (201, 2011), (201, 2012), (201, 2013), (201, 2014), (201, 2015), (201, 2016), (201, 2017),
(201, 2100), (201, 2101),
(201, 2110), (201, 2111), (201, 2112), (201, 2113), (201, 2114), (201, 2115), (201, 2116),
(201, 2200), (201, 2201), (201, 2202), (201, 2203),
(201, 2210), (201, 2211), (201, 2212), (201, 2213), (201, 2214),
(201, 2300), (201, 2301), (201, 2302),
(201, 2400), (201, 2401), (201, 2410), (201, 2411), (201, 2412),
(201, 2500), (201, 2501), (201, 2502),
(201, 2510), (201, 2511), (201, 2512),
(201, 2520), (201, 2521),
(201, 2600);

-- ============================================================
-- 定时任务注册
-- ============================================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES
('计划自动归档',   'DEFAULT', 'planTask.planArchive()',   '0 0 2 * * ?',    '2', '1', '0', 'admin', sysdate(), '每日凌晨2:00归档已通过且到期的计划'),
('计划逾期标记',   'DEFAULT', 'planTask.planOverdue()',   '0 0 3 * * ?',    '2', '1', '0', 'admin', sysdate(), '每日凌晨3:00标记逾期计划'),
('催办提醒发送',   'DEFAULT', 'reminderTask.sendReminders()', '0 15 9 * * MON-FRI', '2', '1', '0', 'admin', sysdate(), '工作日9:15发送催办提醒');

