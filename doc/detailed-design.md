# 日/月计划和成果验收系统 - 详细设计文档

## 一、文档概述

本文档基于概要设计文档 `high-Level-design.md`，对系统的数据库、API 接口、核心流程和前端组件进行详细设计，作为编码开发的直接依据。

---

## 二、数据库详细设计

### 2.1 数据库建表 DDL

#### 2.1.1 department 部门表

```sql
CREATE TABLE department (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    name        VARCHAR(100)  NOT NULL                     COMMENT '部门名称',
    parent_id   BIGINT        DEFAULT NULL                 COMMENT '父部门ID（NULL=顶级部门）',
    sort_order  INT           DEFAULT 0                    COMMENT '排序号',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';
```

#### 2.1.2 users 用户表

```sql
CREATE TABLE users (
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
```

#### 2.1.3 project_category 项目分类表

```sql
CREATE TABLE project_category (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    name        VARCHAR(100)  NOT NULL                     COMMENT '分类名称',
    dept_id     BIGINT        DEFAULT NULL                 COMMENT '所属部门ID（NULL=全局）',
    sort_order  INT           DEFAULT 0                    COMMENT '排序号',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目分类表';
```

#### 2.1.4 plan 计划表

```sql
CREATE TABLE plan (
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
```

#### 2.1.5 achievement 成果表

```sql
CREATE TABLE achievement (
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
```

#### 2.1.6 approval_chain 审批链配置表

```sql
CREATE TABLE approval_chain (
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
```

#### 2.1.7 approval_record 审批记录表

```sql
CREATE TABLE approval_record (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '主键ID',
    target_id       BIGINT        NOT NULL                     COMMENT '审批目标ID（plan.id 或 achievement.id）',
    target_type     VARCHAR(20)   NOT NULL                     COMMENT '审批目标类型：PLAN/ACHIEVEMENT',
    approver_id     BIGINT        NOT NULL                     COMMENT '审批人ID',
    approval_level  INT           NOT NULL                     COMMENT '当前审批级别',
    action          VARCHAR(20)   NOT NULL                     COMMENT '审批操作：APPROVE/REJECT/TRANSFER',
    comment         TEXT          DEFAULT NULL                 COMMENT '审批意见',
    approved_at     DATETIME      DEFAULT NULL                 COMMENT '审批时间',
    sort_order      INT           DEFAULT 0                    COMMENT '同级别审批顺序',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    INDEX idx_target (target_id, target_type),
    INDEX idx_approver (approver_id),
    INDEX idx_approver_status (approver_id, action),
    INDEX idx_target_level (target_id, target_type, approval_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';
```

#### 2.1.8 notification 通知表

```sql
CREATE TABLE notification (
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
```

#### 2.1.9 attachment 附件表

```sql
CREATE TABLE attachment (
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
```

#### 2.1.10 plan_weekly_ref 日报-周报关联表

```sql
CREATE TABLE plan_weekly_ref (
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
```

#### 2.1.11 plan_monthly_ref 周报-月报关联表

```sql
CREATE TABLE plan_monthly_ref (
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
```

### 2.2 数据库初始化数据

```sql
-- 插入默认部门
INSERT INTO department (id, name, parent_id) VALUES (1, '默认部门', NULL);

-- 插入管理员账号（密码：admin123，BCrypt 加密）
-- 实际部署时替换为真实的 BCrypt hash
INSERT INTO users (username, password, real_name, role, dept_id, leader_id)
VALUES ('admin', '$2a$10$xxxxx', '系统管理员', 'ADMIN', 1, NULL);
```

### 2.3 枚举值定义

| 枚举 | 值 | 说明 |
|------|------|------|
| **plan_type** | `DAILY` | 日报 |
| | `WEEKLY` | 周报 |
| | `MONTHLY` | 月报 |
| **priority** | `HIGH` | 高优先级 |
| | `MEDIUM` | 中优先级 |
| | `LOW` | 低优先级 |
| **plan.status** | `DRAFT` | 草稿 |
| | `SUBMITTED` | 已提交（待审批链启动） |
| | `APPROVING` | 审批中 |
| | `APPROVED` | 已通过 |
| | `REJECTED` | 已驳回 |
| **achievement.status** | `PENDING` | 待填写 |
| | `SUBMITTED` | 已提交 |
| | `APPROVING` | 验收中 |
| | `APPROVED` | 已通过 |
| | `REJECTED` | 已驳回 |
| **role** | `EMPLOYEE` | 员工 |
| | `LEADER` | 领导 |
| | `ADMIN` | 管理员 |
| **approval.action** | `APPROVE` | 通过 |
| | `REJECT` | 驳回 |
| | `TRANSFER` | 转审 |
| **notification.type** | `PLAN_REJECTED` | 计划被驳回 |
| | `PLAN_APPROVED` | 计划通过终审 |
| | `NEW_APPROVAL_PLAN` | 有新的计划待审批 |
| | `NEW_APPROVAL_ACHV` | 有新的成果待验收 |
| | `REMIND_SUBMIT_PLAN` | 提醒提交计划 |
| | `REMIND_APPROVE` | 提醒审批超时 |
| | `ACHV_REJECTED` | 成果被驳回 |
| | `ACHV_APPROVED` | 成果通过终审 |

---

## 三、API 接口详细设计

### 3.1 统一规范

**Base URL**：`/api/v1`

**通用响应码**：

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如重复提交） |
| 500 | 服务器内部错误 |

**分页请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 1 | 页码 |
| pageSize | int | 20 | 每页条数（最大100） |

---

### 3.2 认证模块

#### POST `/auth/login` — 用户登录

**Request**：
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 28800,
    "user": {
      "id": 1,
      "username": "zhangsan",
      "realName": "张三",
      "role": "EMPLOYEE",
      "deptId": 1,
      "deptName": "技术部",
      "leaderId": 5,
      "leaderName": "李四",
      "avatarUrl": null
    }
  }
}
```

#### POST `/auth/logout` — 用户登出

**Request**：无 Body（Token 从 Header 取）

**Response**：
```json
{
  "code": 200,
  "message": "已登出",
  "data": null
}
```

#### GET `/auth/me` — 获取当前用户信息

**Response**：同上 `user` 对象

---

### 3.3 计划模块

#### GET `/plans` — 查询计划列表

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| planType | String | ❌ | DAILY / WEEKLY / MONTHLY |
| status | String | ❌ | DRAFT / SUBMITTED / APPROVING / APPROVED / REJECTED |
| priority | String | ❌ | HIGH / MEDIUM / LOW |
| startDate | String | ❌ | 开始日期 yyyy-MM-dd |
| endDate | String | ❌ | 结束日期 yyyy-MM-dd |
| categoryId | Long | ❌ | 项目分类ID |
| userId | Long | ❌ | 提交人ID（领导查看下属时使用） |
| keyword | String | ❌ | 标题关键词搜索 |
| page | int | ❌ | 页码，默认1 |
| pageSize | int | ❌ | 每页条数，默认20 |

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "userName": "张三",
        "planType": "DAILY",
        "title": "修复登录模块Bug",
        "description": "<p>修复用户登录时偶发500错误的问题</p>",
        "priority": "HIGH",
        "status": "APPROVED",
        "startTime": "2026-06-20 09:00:00",
        "endTime": "2026-06-20 18:00:00",
        "categoryId": 2,
        "categoryName": "项目A",
        "quantTarget": "修复3个Bug",
        "hasAchievement": true,
        "achievementId": 1,
        "createdAt": "2026-06-20 08:30:00"
      }
    ],
    "total": 45,
    "page": 1,
    "pageSize": 20
  }
}
```

#### GET `/plans/{id}` — 查看计划详情

**Response**：单个 plan 对象，额外包含：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "plan": { /* ...计划基本信息 */ },
    "approvalTimeline": [
      {
        "id": 10,
        "approvalLevel": 1,
        "approverId": 5,
        "approverName": "李四",
        "action": "APPROVE",
        "comment": "同意，注意优先级",
        "approvedAt": "2026-06-20 09:15:00"
      },
      {
        "id": 11,
        "approvalLevel": 2,
        "approverId": 8,
        "approverName": "王五",
        "action": "APPROVE",
        "comment": "通过",
        "approvedAt": "2026-06-20 10:00:00"
      }
    ],
    "refPlans": [
      {
        "id": 1,
        "planType": "DAILY",
        "title": "修复登录模块Bug",
        "status": "APPROVED"
      }
    ],
    "achievement": { /* 成果信息，如有 */ }
  }
}
```

#### POST `/plans` — 创建计划

**Request**：
```json
{
  "planType": "DAILY",
  "title": "修复登录模块Bug",
  "description": "<p>修复用户登录时偶发500错误的问题</p>",
  "priority": "HIGH",
  "startTime": "2026-06-20 09:00:00",
  "endTime": "2026-06-20 18:00:00",
  "categoryId": 2,
  "quantTarget": "修复3个Bug",
  "refPlanIds": [],
  "submitDirectly": false
}
```

| 字段 | 说明 |
|------|------|
| refPlanIds | 关联的下级计划ID列表（周报关联日报、月报关联周报） |
| submitDirectly | true=直接提交，false=保存草稿（默认） |

**Response**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": { "id": 1, "status": "DRAFT" }
}
```

#### PUT `/plans/{id}` — 修改计划

- 仅 `DRAFT` 或 `REJECTED` 状态可修改
- Request 字段同创建

#### POST `/plans/{id}/submit` — 提交计划

**Request**：无 Body

**Response**：
```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "id": 1,
    "status": "APPROVING",
    "currentApprovalLevel": 1,
    "pendingApprovers": ["李四"]
  }
}
```

**后端处理逻辑**：
1. 校验当前用户是计划提交人
2. 校验计划状态为 `DRAFT` 或 `REJECTED`
3. 查 `approval_chain` 表获取审批链配置
4. 如未配置，回退默认规则：直属领导(level=1) → 部门负责人(level=2)
5. 为第一级所有审批人创建 `approval_record`（action=NULL 表示待审批）
6. 更新 plan.status → `APPROVING`
7. 发送通知：WebSocket 推送给所有第一级审批人

#### DELETE `/plans/{id}` — 删除计划

- 仅 `DRAFT` 状态可删除

#### GET `/plans/calendar` — 日历视图数据

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| year | int | ✅ | 年份 |
| month | int | ✅ | 月份（1-12） |
| planType | String | ❌ | DAILY/WEEKLY/MONTHLY，不传则返回全部 |

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "2026-06-20",
      "plans": [
        {
          "id": 1,
          "title": "修复登录模块Bug",
          "planType": "DAILY",
          "priority": "HIGH",
          "status": "APPROVED"
        },
        {
          "id": 2,
          "title": "代码Review",
          "planType": "DAILY",
          "priority": "MEDIUM",
          "status": "APPROVING"
        }
      ]
    },
    {
      "date": "2026-06-21",
      "plans": []
    }
  ]
}
```

#### GET `/plans/{id}/rollup-options` — 获取可引用的下级计划

用于填写周报时拉取本周日报、填写月报时拉取本月周报。

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| planType | String | ✅ | 当前计划类型（WEEKLY→拉日报，MONTHLY→拉周报） |
| startDate | String | ✅ | 范围起始日期 yyyy-MM-dd |
| endDate | String | ✅ | 范围结束日期 yyyy-MM-dd |

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "planType": "DAILY",
      "title": "修复登录模块Bug",
      "status": "APPROVED",
      "startTime": "2026-06-20",
      "alreadyRefed": false
    }
  ]
}
```

---

### 3.4 成果模块

#### GET `/achievements` — 查询成果列表

**Query 参数类似 plans，额外增加**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| planId | Long | ❌ | 关联计划ID |

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "planId": 1,
        "planTitle": "修复登录模块Bug",
        "planType": "DAILY",
        "description": "<p>已完成3个Bug的修复和自测</p>",
        "actualQty": "修复3个Bug",
        "actualHours": 5.5,
        "issues": "测试环境数据库连接不稳定，调试耗时较多",
        "remark": "其中一个Bug需要后端配合上线",
        "status": "APPROVED",
        "submittedAt": "2026-06-20 17:30:00",
        "attachments": [
          {
            "id": 1,
            "fileName": "修复截图.png",
            "fileSize": 204800,
            "fileType": "image/png"
          }
        ]
      }
    ],
    "total": 30,
    "page": 1,
    "pageSize": 20
  }
}
```

#### GET `/achievements/{id}` — 查看成果详情

**Response**：成果完整信息 + 计划信息 + 计划 vs 实际对比：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "achievement": { /* ...成果信息 */ },
    "plan": { /* ...关联计划信息 */ },
    "comparison": {
      "planTitle": "修复登录模块Bug",
      "planQuantTarget": "修复3个Bug",
      "actualQty": "修复3个Bug",
      "planStartTime": "2026-06-20 09:00:00",
      "planEndTime": "2026-06-20 18:00:00",
      "actualHours": 5.5,
      "status": "MATCH"
    },
    "approvalTimeline": [ /* ... */ ],
    "attachments": [ /* ... */ ]
  }
}
```

> `comparison.status`：`MATCH`（达成） / `PARTIAL`（部分达成） / `EXCEED`（超额完成） / `NOT_MATCH`（未达成）

#### POST `/achievements` — 创建成果

**Request**：
```json
{
  "planId": 1,
  "description": "<p>已完成3个Bug的修复和自测</p>",
  "actualQty": "修复3个Bug",
  "actualHours": 5.5,
  "issues": "测试环境数据库连接不稳定，调试耗时较多",
  "remark": "其中一个Bug需要后端配合上线",
  "attachmentIds": [1, 2, 3],
  "submitDirectly": true
}
```

> `attachmentIds`：先上传附件拿到 ID，再随成果一起提交。

#### PUT `/achievements/{id}` — 修改成果

- 仅 `PENDING` 或 `REJECTED` 状态可修改

#### POST `/achievements/{id}/submit` — 提交成果

**处理逻辑**：
1. 校验成果状态为 `PENDING` 或 `REJECTED`
2. 查成果审批链（`plan_type='ACHIEVEMENT'`），如未配置则回退默认规则
3. 为第一级审批人创建 `approval_record`
4. 更新 achievement.status → `APPROVING`，记录 submitted_at
5. 推送通知给审批人

#### GET `/plans/{planId}/achievement` — 查看某计划的成果

**Response**：单个 achievement 对象（如果有）

---

### 3.5 审批模块

#### GET `/approvals/pending` — 我的待审批列表

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "recordId": 15,
        "targetType": "PLAN",
        "targetId": 1,
        "approvalLevel": 1,
        "totalLevels": 2,
        "submitterName": "张三",
        "title": "修复登录模块Bug",
        "planType": "DAILY",
        "priority": "HIGH",
        "submittedAt": "2026-06-20 08:30:00",
        "peerApprovals": [
          {
            "approverName": "赵六",
            "action": "APPROVE",
            "comment": "同意",
            "approvedAt": "2026-06-20 09:00:00"
          }
        ],
        "myAction": null
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 20
  }
}
```

> `peerApprovals`：同级其他审批人的审批情况。你已确认同级别审批人互相可见审批意见。
> `myAction`：当前用户对该条记录的操作，null 表示还未操作。

#### GET `/approvals/history` — 我的审批历史

**Response**：已处理的审批记录列表（action 不为 null 的记录）

#### POST `/approvals/{recordId}/approve` — 通过审批

**Request**：
```json
{
  "comment": "同意，按计划执行"
}
```

**后端处理逻辑**：
1. 校验 `recordId` 存在且审批人是当前用户且未审批
2. 更新 `approval_record`：action=APPROVE, comment, approved_at=now
3. 检查该级是否**全部审批人通过**：
   - 同级所有审批记录 action 均为 APPROVE → 该级通过
   - 否则：该级等待，不推进
4. 该级通过后，检查是否最后一级：
   - 是最后一级 → 更新 plan/achievement.status=APPROVED，通知提交人"通过"
   - 不是最后一级 → 为下一级所有审批人创建 approval_record，通知下一级审批人

#### POST `/approvals/{recordId}/reject` — 驳回审批

**Request**：
```json
{
  "comment": "计划不够具体，请补充量化指标后重新提交"
}
```

**处理逻辑**：驳回后，该级的其他待审批记录也不需要再审批了。更新 plan/achievement.status=REJECTED，通知提交人。

#### POST `/approvals/{recordId}/transfer` — 转审

**Request**：
```json
{
  "targetUserId": 10,
  "comment": "我不在，转给赵六审批"
}
```

**处理逻辑**：
1. 校验 `targetUserId` 与本用户同部门
2. 更新当前 `approval_record`：action=TRANSFER
3. 创建新的 `approval_record`，approver_id=targetUserId，同一级
4. 通知新的审批人

#### GET `/approvals/{targetType}/{targetId}/timeline` — 审批时间线

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "targetType": "PLAN",
    "targetId": 1,
    "currentLevel": 2,
    "totalLevels": 2,
    "status": "APPROVED",
    "records": [
      {
        "approvalLevel": 1,
        "levelLabel": "一级审批（直属领导）",
        "approvals": [
          {
            "approverName": "李四",
            "action": "APPROVE",
            "comment": "同意",
            "approvedAt": "2026-06-20 09:15:00"
          }
        ]
      },
      {
        "approvalLevel": 2,
        "levelLabel": "二级审批（部门负责人）",
        "approvals": [
          {
            "approverName": "王五",
            "action": "APPROVE",
            "comment": "通过",
            "approvedAt": "2026-06-20 10:00:00"
          }
        ]
      }
    ]
  }
}
```

---

### 3.6 通知模块

#### GET `/notifications` — 通知列表

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "type": "NEW_APPROVAL_PLAN",
        "title": "新的待审批计划",
        "content": "张三提交了日报计划「修复登录模块Bug」，请审批",
        "isRead": false,
        "relatedId": 1,
        "relatedType": "PLAN",
        "createdAt": "2026-06-20 08:30:00"
      }
    ],
    "unreadCount": 3,
    "total": 50,
    "page": 1,
    "pageSize": 20
  }
}
```

#### GET `/notifications/unread-count` — 未读数量

```json
{
  "code": 200,
  "message": "success",
  "data": { "count": 3 }
}
```

#### PUT `/notifications/{id}/read` — 标记已读

#### PUT `/notifications/read-all` — 全部已读

#### DELETE `/notifications/{id}` — 删除通知（手动）

---

### 3.7 文件模块

#### POST `/files/upload` — 上传附件

- Content-Type: `multipart/form-data`
- 字段名: `file`
- 限制：单个 ≤ 10MB，格式为常见文档/图片

**Response**：
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "fileName": "修复截图.png",
    "fileSize": 204800,
    "fileType": "image/png",
    "filePath": "/uploads/2026/06/abc123.png"
  }
}
```

#### GET `/files/{id}/download` — 下载附件

- 返回文件流，Content-Disposition: attachment

#### GET `/files/{id}/preview` — 预览附件

- 图片：直接返回图片
- 其他：返回文件元信息

#### DELETE `/files/{id}` — 删除附件

> 仅附件上传者或管理员可删除。附件与成果关联后不可单独删除（需先修改成果）。

---

### 3.8 统计模块

#### GET `/statistics/personal` — 个人统计

**Query 参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| startDate | String | ❌ | 统计起始日期 |
| endDate | String | ❌ | 统计结束日期 |
| planType | String | ❌ | DAILY/WEEKLY/MONTHLY，不传=全部 |

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalPlans": 45,
      "approvedPlans": 38,
      "rejectedPlans": 3,
      "completionRate": 84.4,
      "onTimeSubmitRate": 92.0
    },
    "byType": {
      "DAILY": { "total": 30, "approved": 27, "rate": 90.0 },
      "WEEKLY": { "total": 10, "approved": 8, "rate": 80.0 },
      "MONTHLY": { "total": 5, "approved": 3, "rate": 60.0 }
    },
    "byPriority": {
      "HIGH": { "total": 15, "approved": 12, "rate": 80.0 },
      "MEDIUM": { "total": 20, "approved": 18, "rate": 90.0 },
      "LOW": { "total": 10, "approved": 8, "rate": 80.0 }
    },
    "trend": [
      { "period": "2026-W25", "total": 5, "approved": 4, "rate": 80.0 },
      { "period": "2026-W24", "total": 5, "approved": 5, "rate": 100.0 }
    ]
  }
}
```

#### GET `/statistics/team` — 团队统计（领导可见）

**Response**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "teamSummary": {
      "totalMembers": 10,
      "totalPlans": 300,
      "approvedPlans": 250,
      "completionRate": 83.3,
      "avgOnTimeRate": 88.5
    },
    "members": [
      {
        "userId": 1,
        "userName": "张三",
        "totalPlans": 45,
        "approved": 38,
        "completionRate": 84.4,
        "onTimeRate": 92.0
      }
    ],
    "unsubmittedToday": [
      { "userId": 3, "userName": "王五" }
    ]
  }
}
```

> `unsubmittedToday`：当天尚未提交日报的团队成员。

#### GET `/statistics/trend` — 趋势数据

**Query**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| periodType | String | ✅ | WEEKLY / MONTHLY |
| periods | int | ❌ | 返回最近几个周期，默认12 |

---

### 3.9 管理模块

#### 用户管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users` | 用户列表（分页+搜索） |
| POST | `/admin/users` | 添加用户 |
| PUT | `/admin/users/{id}` | 编辑用户信息 |
| PUT | `/admin/users/{id}/disable` | 禁用/启用 |
| PUT | `/admin/users/{id}/reset-password` | 重置密码 |

**POST `/admin/users` Request**：
```json
{
  "username": "wangwu",
  "password": "123456",
  "realName": "王五",
  "role": "EMPLOYEE",
  "deptId": 1,
  "leaderId": 5,
  "email": "wangwu@example.com"
}
```

#### 部门管理、审批链配置、分类管理接口

均为标准 CRUD，格式参考概要设计文档。

---

## 四、核心流程时序图

### 4.1 计划提交与两级审批流程

```
员工          后端           数据库         审批人A(一级)    审批人B(二级)
 │            │              │                 │              │
 │ 提交计划    │              │                 │              │
 │───────────>│              │                 │              │
 │            │ 查审批链配置   │                 │              │
 │            │─────────────>│                 │              │
 │            │<─────────────│                 │              │
 │            │ 创建一级审批记录│                │              │
 │            │─────────────>│                 │              │
 │            │ 更新状态=APPROVING              │              │
 │            │─────────────>│                 │              │
 │            │             │                 │              │
 │            │──── WebSocket 推送通知 ───────>│              │
 │            │             │                 │              │
 │ 返回成功    │             │                 │              │
 │<───────────│             │                 │              │
 │            │             │                 │              │
 │            │             │    审批通过      │              │
 │            │             │<────────────────│              │
 │            │ 记录审批操作  │                 │              │
 │            │─────────────>│                 │              │
 │            │             │                 │              │
 │            │ 查同级是否全部通过              │              │
 │            │─────────────>│                 │              │
 │            │<─── 全部通过 ─│                 │              │
 │            │             │                 │              │
 │            │ 创建二级审批记录                │              │
 │            │─────────────>│                 │              │
 │            │             │                 │              │
 │            │────────────────── WebSocket ─────────────────>│
 │            │             │                 │              │
 │            │             │                 │   审批通过    │
 │            │             │                 │<─────────────│
 │            │ 记录审批操作  │                 │              │
 │            │─────────────>│                 │              │
 │            │ 最后一级，更新状态=APPROVED     │              │
 │            │─────────────>│                 │              │
 │            │             │                 │              │
 │            │── WebSocket 通知员工「计划已通过」──>           │
 │            │             │                 │              │
 │ 收到通知   │             │                 │              │
 │<───────────│             │                 │              │
```

### 4.2 审批驳回流程

```
审批人A        后端           数据库          员工
 │             │              │              │
 │ 驳回+意见    │              │              │
 │────────────>│              │              │
 │             │ 记录驳回操作   │              │
 │             │─────────────>│              │
 │             │ 更新状态=REJECTED            │
 │             │─────────────>│              │
 │             │              │              │
 │             │── WebSocket 通知员工 ───────>│
 │             │              │              │
 │             │              │   修改后重新提交
 │             │              │<─────────────│
 │             │ 状态变回APPROVING            │
 │             │ 重新创建一级审批记录          │
 │             │─────────────>│              │
```

### 4.3 成果提交流程

```
员工          后端           数据库          审批人
 │            │              │               │
 │ 选择已通过的│              │               │
 │ 计划，填成果│              │               │
 │───────────>│              │               │
 │            │ 校验plan已通过 │               │
 │            │─────────────>│               │
 │            │ 创建achievement               │
 │            │─────────────>│               │
 │            │              │               │
 │            │(如果直接提交)  │               │
 │            │ 查成果审批链   │               │
 │            │─────────────>│               │
 │            │ 创建审批记录   │               │
 │            │ 更新achievement状态=APPROVING  │
 │            │─────────────>│               │
 │            │── WebSocket ────────────────>│
 │            │              │               │
 │ 返回成功    │              │               │
 │<───────────│              │               │
```

### 4.4 WebSocket 连接与通知推送流程

```
前端                     后端                      Redis
 │                        │                         │
 │ 登录成功，建立WebSocket  │                         │
 │── SockJS 连接 ────────>│                         │
 │<── 连接建立，订阅 /user/queue/notifications ───>│
 │                        │                         │
 │                        │ 业务触发通知              │
 │                        │ (如：新审批到达)          │
 │                        │                         │
 │                        │ 写notification表          │
 │                        │────────────────────────>│
 │                        │                         │
 │                        │ STOMP 推送消息            │
 │<── {"type":"NOTIFICATION","data":{...}} ─────────│
 │                        │                         │
 │ 更新通知铃铛红点数       │                         │
 │ 显示消息提示            │                         │
```

---

## 五、前端组件详细设计

### 5.1 页面-组件映射

```
AppLayout.vue（主布局）
├── Sidebar.vue（侧边栏）
│   └── 根据 role 动态显示菜单项
├── Topbar.vue（顶栏）
│   ├── 面包屑导航
│   ├── NotificationBell.vue（通知铃铛 + 未读红点 + 下拉列表）
│   └── 用户头像下拉菜单
└── <router-view>（内容区）
    │
    ├── 计划列表页 PlanList.vue
    │   ├── 筛选栏（planType/status/priority/时间范围/关键词）
    │   ├── 表格（分页）
    │   │   └── PlanStatusTag.vue（状态标签：彩色圆点+文字）
    │   └── 操作按钮（新建/编辑/提交/删除/查看成果）
    │
    ├── 计划表单页 PlanCreate.vue / PlanEdit.vue
    │   ├── PlanForm.vue（计划表单组件，日/周/月复用）
    │   │   ├── 类型切换（日报/周报/月报）
    │   │   ├── 日期选择器
    │   │   ├── 富文本编辑器（描述）
    │   │   ├── 优先级选择
    │   │   ├── 分类下拉
    │   │   └── 引用计划选择（周报/月报时显示）
    │   └── 提交 / 保存草稿 按钮
    │
    ├── 计划详情页 PlanDetail.vue
    │   ├── 计划信息卡片
    │   ├── ApprovalTimeline.vue（审批时间线）
    │   ├── 引用的下级计划列表
    │   └── 成果信息（如有，含 PlanVsActual 对比）
    │
    ├── 日历视图 PlanCalendar.vue
    │   └── FullCalendar 组件
    │       ├── 日/周/月切换
    │       └── 点击日期查看计划详情弹窗
    │
    ├── 成果列表 AchievementList.vue
    ├── 成果表单 AchievementForm.vue
    │   ├── 关联计划选择
    │   ├── 富文本编辑器（完成说明）
    │   ├── 实际数量/耗时输入
    │   ├── FileUpload.vue（附件上传）
    │   └── PlanVsActual.vue（提交前预览对比）
    │
    ├── 成果详情 AchievementDetail.vue
    │   ├── 成果信息
    │   ├── PlanVsActual.vue（计划vs实际对比）
    │   ├── ApprovalTimeline.vue
    │   └── FilePreview.vue（附件预览/下载）
    │
    ├── 待审批列表 ApprovalPending.vue
    │   ├── 计划审批 Tab / 成果验收 Tab
    │   └── 每条记录含 ApprovalDialog.vue 操作入口
    │
    ├── 审批弹窗 ApprovalDialog.vue
    │   ├── 通过 / 驳回 / 转审 三个按钮
    │   ├── 审批意见输入框
    │   ├── 转审时：选择同部门用户
    │   └── 同级审批意见展示（可见同级其他人的意见）
    │
    ├── 个人统计 PersonalStats.vue
    │   └── ECharts（饼图+柱状图+折线图）
    │
    ├── 团队统计 TeamStats.vue
    ├── 通知列表 NotificationList.vue
    ├── 管理页面（4个：用户/部门/审批链/分类）
    └── 个人中心 ProfileView.vue
```

### 5.2 关键组件交互说明

#### 5.2.1 我的设计选择：计划列表内嵌成果入口

计划和成果不用严格分两个独立页面。员工的核心操作流是"看计划 → 提交成果"，所以：

- **计划列表** 中，每条已通过且尚未提交成果的计划，右侧显示「提交成果」按钮，点击直接跳转成果提交页
- 已提交成果的计划，显示成果状态标签，点击可查看成果详情
- 同时保留独立的「成果管理」菜单，用于：
  - 看所有成果的历史记录（含筛选）
  - 看被驳回待修改的成果

这样的好处是：员工最常走的路径（提交成果）路径最短。

#### 5.2.2 富文本编辑器

推荐使用 **Quill** 或 **Tiptap**，轻量且支持 Vue 3：

- 计划描述、成果说明使用富文本
- 工具栏简洁：加粗、列表、链接即可，不必太复杂

#### 5.2.3 日历组件

推荐使用 **FullCalendar Vue**（`@fullcalendar/vue3`）：

- 支持日/周/月三种视图切换
- 每个事件用颜色区分状态：绿色=已通过，橙色=审批中，红色=已驳回，灰色=草稿
- 点击事件弹出详情卡片

---

## 六、错误码定义

| code | 说明 | 场景 |
|------|------|------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 必填字段缺失、格式错误 |
| 401 | 未认证 | Token 缺失、过期、无效 |
| 403 | 无权限 | 非本人操作他人计划、员工访问管理接口 |
| 404 | 资源不存在 | 计划/成果/用户 ID 不存在 |
| 409 | 业务冲突 | 计划已提交不可修改、重复提交 |
| 1001 | 审批链未配置 | 部门和计划类型未找到审批链且无默认规则 |
| 1002 | 转审目标不在同部门 | 转审时选择非同部门用户 |
| 1003 | 审批记录已被处理 | 重复审批 |
| 1004 | 文件大小超限 | 单个文件 > 10MB |
| 1005 | 附件数量超限 | 单次 > 5 个附件 |
| 1006 | 文件格式不支持 | 不允许的文件类型 |

---

## 七、关键业务规则汇总

| # | 规则 | 说明 |
|---|------|------|
| 1 | 默认审批链 | 未配置时回退：直属领导(level=1) → 部门负责人(level=2) |
| 2 | 同级全部通过 | 同一级配置多个审批人时，全部通过后才进入下一级 |
| 3 | 同级互见 | 同级审批人审批时可以看到其他人的审批意见 |
| 4 | 转审限同部门 | 审批只能转给同部门的人 |
| 5 | 驳回重置 | 驳回后重新走完整的审批链（从第一级开始） |
| 6 | 计划不强制成果 | 计划通过后可选择不提交成果 |
| 7 | 计划与成果一对一 | 每条计划最多一个成果 |
| 8 | 通知永久保留 | 不自动清理，用户可手动删除 |
| 9 | JWT 8 小时 | Token 有效期 8 小时，登出加入 Redis 黑名单 |
| 10 | 周一开始/自然月 | 周一为每周首日，月份为自然月 |
| 11 | 超时不阻止 | 计划超时提交不会被系统阻止，仅提醒督促 |

---

> **文档版本**：v1.0  
> **创建日期**：2026-06-20  
> **对应概要设计**：`doc/high-Level-design.md`  
> **下一步**：编码开发
