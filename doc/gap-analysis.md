# HR需求 vs 现有系统 — 差距分析报告

> 生成日期：2026-06-20  
> 基于已确认的全部需求逐条对比

---

## 一、需求确认汇总

### 1.1 原始需求（HR转述）

| # | 需求 | 确认结果 |
|---|------|----------|
| 1 | 严格划分员工和领导两套系统 | 同一系统，按角色区分菜单和权限 |
| 2 | 计划/成果提交后审批前可修改 | 需要「撤回」功能 |
| 3 | 审批后不可修改 | 维持现有：APPROVED后锁定 |
| 4 | 成果关联多条计划条目 | **一对多**（一次成果汇总多条计划） |
| 5 | 审批分待审/通过/驳回/完成 | "完成"=终审通过；驳回改称"待修改" |
| 6 | 每次审批带详情意见 | 维持现有 comment 字段 |
| 7 | 日/月内容可统计查询 | 维持现有统计模块 |
| 8 | 保留周报 | 日/周/月三级均保留 |
| 9 | 系统角色 | 两种：员工 + 领导（管理员合并到领导） |

### 1.2 隐性实际场景需求（企业真实痛点）

| # | 需求 | 确认结果 |
|---|------|----------|
| 10 | 日/月计划，时间维度自动归档 | 计划截止后自动标记为「已归档」只读状态 |
| 11 | 按月/按日筛选查询 | 已有日期筛选，需补充归档Tab |
| 12 | 审批状态：待审批/已通过/已驳回/待修改 | REJECTED→"待修改"，驳回后从头审批 |
| 13 | 驳回必须附带理由 | 前端按钮禁用 + 后端接口校验，双重强制 |
| 14 | 成果绑定审批通过的计划 | 维持此约束，配合一对多调整 |
| 15 | 数据留存永久可查，方便绩效考核 | 归档筛选入口 + Excel导出 |
| 16 | 流程可追溯 | 已有审批时间线，满足 |

---

## 二、需要改动的清单

### 🔴 重大改动（涉及数据库结构调整）

#### G-1 计划-成果：一对一 → 一对多

**现状**：
```
achievement.plan_id BIGINT UNIQUE  — 一个成果只能关联一条计划
```

**目标**：
```
一条成果可关联多条已通过的计划（如一周工作成果汇总多条日报）
```

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| DB | `db/init.sql` | ① `achievement.plan_id` 去掉 UNIQUE 约束（改为普通索引）；② 新增 `achievement_plan_ref` 关联表 |
| 后端 | `entity/Achievement.java` | 新增 `planIds` 字段（List<Long>），通过关联表映射 |
| 后端 | `entity/AchievementPlanRef.java` | **新建**关联实体 |
| 后端 | `mapper/AchievementPlanRefMapper.java` | **新建** |
| 后端 | `service/AchievementService.java` | 创建成果时批量插入关联记录，查询时JOIN返回计划列表 |
| 后端 | `controller/AchievementController.java` | create/update 接口接收 `planIds: [1,2,3]` |
| 后端 | `dto/AchievementCreateRequest.java` | `planId` → `planIds` |
| 前端 | `views/achievement/AchievementSubmit.vue` | 计划选择改为多选（checkbox列表） |
| 前端 | `views/achievement/AchievementDetail.vue` | 展示多条关联计划 |
| 前端 | `api/achievement.js` | 请求参数调整 |

**新增关联表 DDL**：
```sql
CREATE TABLE achievement_plan_ref (
    id               BIGINT   PRIMARY KEY AUTO_INCREMENT,
    achievement_id   BIGINT   NOT NULL,
    plan_id          BIGINT   NOT NULL,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_achv_plan (achievement_id, plan_id),
    INDEX idx_achievement (achievement_id),
    INDEX idx_plan (plan_id),
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果-计划关联表';
```

---

#### G-2 新增「撤回」功能

**现状**：
- 计划一旦提交（SUBMITTED），不可再编辑
- 没有撤回接口

**目标**：
- 员工提交后、任何审批人尚未审批前，可撤回至草稿状态
- 撤回后取消所有待审批的 approval_record

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `controller/PlanController.java` | 新增 `POST /plans/{id}/withdraw` |
| 后端 | `controller/AchievementController.java` | 新增 `POST /achievements/{id}/withdraw` |
| 后端 | `service/PlanService.java` | 撤回逻辑：校验无人审批→状态→DRAFT→删除待审批记录→发通知 |
| 后端 | `service/AchievementService.java` | 同理 |
| 前端 | `views/plan/PlanList.vue` | 待审批状态的计划显示「撤回」按钮 |
| 前端 | `views/achievement/AchievementList.vue` | 同理 |
| 前端 | `api/plan.js` | 新增 withdrawPlan(id) |
| 前端 | `api/achievement.js` | 新增 withdrawAchievement(id) |

**撤回条件**：
- 计划/成果状态为 SUBMITTED 或 APPROVING
- 没有任何审批人已经做出审批操作（所有 approval_record.action IS NULL）

---

### 🟡 中等改动（不涉及表结构变更）

#### G-3 角色合并：三种→两种

**现状**：EMPLOYEE / LEADER / ADMIN 三种角色

**目标**：EMPLOYEE / LEADER 两种（管理员功能由领导兼任）

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `enums/UserRole.java` | 移除 ADMIN 值 |
| 后端 | `config/SecurityConfig.java` | 管理接口权限从 ADMIN→LEADER |
| 后端 | `db/init.sql` | 种子数据中 admin 用户 role 改为 LEADER |
| 后端 | `entity/User.java` | role 字段注释去掉 ADMIN |
| 前端 | `stores/user.js` | `isAdmin` 合并到 `isLeader`；`isLeader = role === 'LEADER'` |
| 前端 | `router/index.js` | 管理页面路由 meta.roles：`['ADMIN']` → `['LEADER']` |
| 前端 | `components/layout/Sidebar.vue` | `v-if="userStore.isAdmin"` → `v-if="userStore.isLeader"` |

---

#### G-4 审批状态：驳回 → 待修改

**现状**：`REJECTED` 显示为"已驳回"

**目标**：`REJECTED` 显示为"待修改"（业务逻辑不变，仅改显示文字）

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `enums/PlanStatus.java` | `REJECTED("REJECTED", "已驳回")` → `REJECTED("REJECTED", "待修改")` |
| 后端 | `enums/AchievementStatus.java` | 同上 |
| 前端 | 所有状态标签组件 | 筛选下拉框、状态Tag中的"已驳回"→"待修改" |
| 前端 | `views/plan/PlanList.vue` | 状态列筛选选项 |
| 前端 | `views/achievement/AchievementList.vue` | 同上 |
| 前端 | `components/approval/ApprovalDialog.vue` | 驳回相关提示文字 |

---

#### G-5 驳回理由双重强制

**现状**：
- 前端 `handleReject()` 有校验（`ElMessage.warning`），但**驳回按钮未禁用**
- 后端 `reject()` 直接 `body.get("comment")`，**无强制校验**

**目标**：前端驳回按钮置灰 + 后端接口强制校验

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `controller/ApprovalController.java` | reject 接口校验 comment 非空 |
| 后端 | `service/ApprovalService.java` | reject() 方法参数校验，空则抛 BusinessException |
| 前端 | `components/approval/ApprovalDialog.vue` | 驳回按钮 `:disabled="!comment.trim()"`；带 tooltip 提示 |

---

### 🟢 新增功能

#### G-6 自动归档机制

**现状**：无归档概念

**目标**：计划 `end_time` 已过 + 状态为 APPROVED → 自动标记为「已归档」

**实现方案选择**：

| 方案 | 描述 | 推荐度 |
|------|------|:---:|
| 方案A：新增 ARCHIVED 状态 | 定时任务更新状态为 ARCHIVED | ⭐⭐⭐ |
| 方案B：查询时动态判断 | 不改状态，查询时根据时间动态标记为"已归档" | ⭐⭐ |

**推荐方案A，改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `enums/PlanStatus.java` | 新增 `ARCHIVED("ARCHIVED", "已归档")` |
| 后端 | `service/PlanArchiveService.java` | **新建**定时任务：每日凌晨扫描 plan 表，end_time < now 且 status=APPROVED → ARCHIVED |
| 后端 | `config/ScheduleConfig.java` | **新建**定时任务配置 `@EnableScheduling` |
| 前端 | `views/plan/PlanList.vue` | 状态筛选增加「已归档」选项；增加「季度/年度」快捷筛选 |
| 前端 | `views/achievement/AchievementList.vue` | 增加季度/年度筛选 |

**状态流转图（更新后）**：
```
草稿(DRAFT) ──提交──> 已提交(SUBMITTED) ──审批链启动──> 审批中(APPROVING)
                        │    ▲                                    │
                        │    │ 撤回                               │
                        │    └────────────────────────────────────┘
                        │
                        ├──> 已通过(APPROVED) ──到期自动──> 已归档(ARCHIVED)
                        │
                        └──> 待修改(REJECTED) ──修改重提交──> 已提交(SUBMITTED)
```

---

#### G-7 Excel 数据导出

**现状**：无导出功能

**目标**：领导可将计划/成果数据导出为 Excel，方便绩效考核

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `pom.xml` | 新增 Apache POI 依赖 |
| 后端 | `controller/ExportController.java` | **新建** `GET /api/v1/export/plans` 和 `GET /api/v1/export/achievements` |
| 后端 | `service/ExportService.java` | **新建**生成 .xlsx 文件流 |
| 前端 | `views/plan/PlanList.vue` | 新增「导出Excel」按钮 |
| 前端 | `views/achievement/AchievementList.vue` | 同上 |
| 前端 | `views/statistics/TeamStats.vue` | 领导可在统计页面导出汇总数据 |
| 前端 | `api/plan.js` | 新增导出请求（responseType: blob） |

---

#### G-8 批量审批

**现状**：待审批列表只能逐条操作

**目标**：支持多选勾选，一键批量通过

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `controller/ApprovalController.java` | 新增 `POST /approvals/batch-approve` 接口，接收 `recordIds` 数组 |
| 后端 | `service/ApprovalService.java` | 批量审批逻辑：遍历+事务 |
| 前端 | `views/approval/ApprovalPending.vue` | 列表增加多选checkbox，顶栏增加「批量通过」按钮 |
| 前端 | `api/approval.js` | 新增 batchApprove(recordIds, comment) |

**约束**：批量操作仅限「通过」，驳回和转审必须逐条处理（保证审批质量）。

---

#### G-9 软删除

**现状**：`DELETE FROM plan WHERE id=?` 硬删除

**目标**：所有核心数据标记删除，30天内可恢复

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| DB | `db/init.sql` | plan、achievement 表新增 `deleted_at DATETIME DEFAULT NULL` 字段 |
| DB | `db/init.sql` | **新建** `recycle_bin` 表（记录删除操作，支持恢复） |
| 后端 | `entity/Plan.java` | 新增 deletedAt 字段 |
| 后端 | `entity/Achievement.java` | 新增 deletedAt 字段 |
| 后端 | `mapper/PlanMapper.java` | 所有查询自动追加 `WHERE deleted_at IS NULL` |
| 后端 | `service/PlanService.java` | deletePlan() → 改为软删除 |
| 后端 | `service/RecycleBinService.java` | **新建**回收站服务（列表、恢复、彻底删除、30天自动清理） |
| 后端 | `controller/RecycleBinController.java` | **新建** `GET/POST/DELETE /api/v1/recycle-bin` |
| 前端 | `views/plan/PlanList.vue` | 删除改为确认弹窗"数据将移至回收站" |
| 前端 | `views/admin/RecycleBin.vue` | **新建**回收站页面（仅领导可见） |

---

#### G-10 计划模板 + 一键复制

**现状**：每次新建计划从空白开始

**目标**：支持保存个人模板 + 复制历史计划

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| DB | `db/init.sql` | **新建** `plan_template` 表 |
| 后端 | `entity/PlanTemplate.java` | **新建** |
| 后端 | `controller/PlanTemplateController.java` | **新建** CRUD |
| 后端 | `service/PlanTemplateService.java` | **新建** |
| 后端 | `controller/PlanController.java` | 新增 `POST /plans/copy/{id}` 复制历史计划 |
| 前端 | `views/plan/PlanCreate.vue` | 新建页面增加「从模板创建」和「复制历史计划」入口 |
| 前端 | `views/plan/PlanTemplates.vue` | **新建**模板管理页 |
| 前端 | `views/plan/PlanList.vue` | 每条已通过的计划增加「复制」按钮 |
| 前端 | `api/plan.js` | 新增 copyPlan、模板相关接口 |

**PlanTemplate 表结构**：
```sql
CREATE TABLE plan_template (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    plan_type   VARCHAR(10) NOT NULL,
    title       VARCHAR(200),
    description TEXT,
    priority    VARCHAR(10) DEFAULT 'MEDIUM',
    category_id BIGINT,
    quant_target VARCHAR(200),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划模板表';
```

---

#### G-11 定时催办提醒

**现状**：催办通知设计存在但无自动触发机制

**目标**：工作日定时检查并自动发送催办通知

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `service/ReminderService.java` | **新建**定时任务服务 |
| 后端 | `config/ScheduleConfig.java` | `@EnableScheduling` 配置 |
| 前端 | `views/statistics/TeamStats.vue` | 未提交名单增加「一键催办」按钮 |
| 前端 | `api/statistics.js` | 新增 remindUnsubmitted(date) 接口 |

**催办规则**：
- 工作日每天 9:15：检查当日未提交日报的员工，自动发通知
- 每周一 9:15：检查本周未提交周报的员工
- 每月1日 9:15：检查本月未提交月报的员工
- 审批超时：提交后 4 小时未审批，提醒审批人

---

#### G-12 操作日志

**现状**：无操作日志记录

**目标**：记录所有关键操作，支持审计追溯

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| DB | `db/init.sql` | **新建** `operation_log` 表 |
| 后端 | `entity/OperationLog.java` | **新建** |
| 后端 | `aspect/OperationLogAspect.java` | **新建** AOP 切面自动记录 |
| 后端 | `annotation/OperationLog.java` | **新建**自定义注解 |
| 后端 | `controller/LogController.java` | **新建** `GET /api/v1/admin/logs`（领导可查） |
| 前端 | `views/admin/OperationLog.vue` | **新建**日志查看页（按时间/操作人/操作类型筛选） |

**记录字段**：操作人、时间、IP、操作类型（CREATE/UPDATE/DELETE/SUBMIT/WITHDRAW/APPROVE/REJECT）、目标类型（PLAN/ACHIEVEMENT）、目标ID、变更摘要

**实现方式**：AOP 切面 + 自定义注解，对 Service 层关键方法零侵入记录。

---

#### G-13 业务规则校验（防重复提交）

**现状**：同一员工可提交多份同日日报

**目标**：后端强制校验防重复

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `service/PlanService.java` | createPlan 时校验：同一天只能一份DAILY、同一周只能一份WEEKLY、同一月只能一份MONTHLY |
| 前端 | `views/plan/PlanCreate.vue` | 新建页面前置检查：如今天已有日报，提示「你今天已有一份日报，是否编辑？」 |

**校验规则**：
- DAILY：同一 user_id + 同一日期不允许重复提交（DRAFT除外）
- WEEKLY：同一 user_id + 同一自然周不允许重复
- MONTHLY：同一 user_id + 同一自然月不允许重复
- 驳回后重提交不受此限（同一份计划的修改）

---

#### G-14 移动端响应式适配

**现状**：Element Plus 桌面端布局，手机体验未知

**目标**：核心页面在手机上可正常使用

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 前端 | `components/layout/AppLayout.vue` | 移动端侧边栏折叠、响应式断点 |
| 前端 | `components/layout/Sidebar.vue` | 移动端汉堡菜单 |
| 前端 | `views/plan/PlanCreate.vue` | 表单移动端布局（单列） |
| 前端 | `views/plan/PlanList.vue` | 表格→卡片列表（移动端） |
| 前端 | `views/approval/ApprovalPending.vue` | 移动端适配 |
| 前端 | 全局 CSS | 响应式媒体查询 |

**适配策略**：768px 断点，移动端核心场景（填日报、查看通知、审批）优先保障。

---

#### G-15 计划评论/讨论区

**现状**：只有正式审批意见，无自由讨论空间

**目标**：计划和成果详情页增加评论区

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| DB | `db/init.sql` | **新建** `plan_comment` 表 |
| 后端 | `entity/PlanComment.java` | **新建** |
| 后端 | `controller/CommentController.java` | **新建**评论 CRUD |
| 后端 | `service/CommentService.java` | **新建** |
| 前端 | `components/plan/CommentSection.vue` | **新建**评论区组件 |
| 前端 | `views/plan/PlanDetail.vue` | 底部增加评论区 |
| 前端 | `views/achievement/AchievementDetail.vue` | 底部增加评论区 |

**与审批意见的区别**：评论是非正式沟通，不触发状态流转，全员可见（提交人+审批人都能评论）。

---

#### G-16 个人工作台首页

**现状**：登录后直接跳转计划列表

**目标**：登录后进入工作台仪表盘

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `controller/DashboardController.java` | **新建** `GET /api/v1/dashboard` 聚合数据 |
| 后端 | `service/DashboardService.java` | **新建**汇聚今日概览、待办数、本周统计 |
| 前端 | `views/dashboard/Dashboard.vue` | **新建**工作台首页 |
| 前端 | `router/index.js` | `/` 路由指向 Dashboard |

**Dashboard 内容**：
- **员工视角**：今日计划状态、待修改项、本周完成率、最近通知
- **领导视角**：待审批数、团队今日提交率、未提交名单、团队本周统计概览
- 日历小部件（未来7天计划预览）

---

#### G-17 逾期追踪

**现状**：计划过了 end_time 无任何标记

**目标**：逾期计划自动标记并在统计中体现

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| 后端 | `enums/PlanStatus.java` | 新增 `OVERDUE("OVERDUE", "已逾期")` |
| 后端 | `service/PlanOverdueService.java` | **新建**定时任务：每日扫描 end_time < now 且无成果且状态为 APPROVED 的计划→标记 OVERDUE |
| 后端 | `service/StatisticsService.java` | 统计中增加逾期率指标 |
| 前端 | `views/plan/PlanList.vue` | 逾期计划红色高亮显示 |
| 前端 | `views/statistics/PersonalStats.vue` | 增加逾期率图表 |
| 前端 | `views/statistics/TeamStats.vue` | 团队逾期排名 |
| 前端 | `views/dashboard/Dashboard.vue` | 领导工作台显示逾期预警 |

---

#### G-18 修改留痕

**现状**：驳回后员工修改重新提交，领导看不出改了什么

**目标**：记录修改前后差异，方便领导对比审批

**改动文件**：

| 层级 | 文件 | 改动内容 |
|------|------|----------|
| DB | `db/init.sql` | **新建** `plan_revision` 表 |
| 后端 | `entity/PlanRevision.java` | **新建** |
| 后端 | `service/PlanService.java` | submitPlan 时对比上一版本，生成 revision 记录 |
| 后端 | `controller/PlanController.java` | 详情接口返回 revision 历史 |
| 前端 | `views/plan/PlanDetail.vue` | 审批时间线中增加「修改记录」Tab |
| 前端 | `components/approval/ApprovalDialog.vue` | 审批弹窗中展示修改对比（diff高亮） |

**PlanRevision 表**：
```sql
CREATE TABLE plan_revision (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id     BIGINT NOT NULL,
    version     INT NOT NULL,
    changes     JSON COMMENT '变更字段及新旧值',
    submitter_note TEXT COMMENT '员工填写的修改说明',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划修改版本记录';
```

---

## 三、无需改动的部分（已满足需求）

| # | 需求 | 现有实现 | 状态 |
|---|------|----------|:--:|
| 1 | 员工/领导两套系统 | Sidebar + Router 根据 role 显示不同菜单 | ✅ |
| 2 | 审批后不可编辑 | PlanService 状态校验：仅 DRAFT/REJECTED 可编辑 | ✅ |
| 3 | 日/月计划体系 | DAILY/WEEKLY/MONTHLY 三种类型 | ✅ |
| 4 | 多级审批链 | approval_chain + approval_record，默认两级 | ✅ |
| 5 | 审批时间线 | ApprovalTimeline.vue + GET timeline 接口 | ✅ |
| 6 | 同级审批互见 | peerApprovals 展示 | ✅ |
| 7 | 成果关联审批通过的计划 | achievement 创建时校验 plan.status=APPROVED | ✅ |
| 8 | 计划 vs 实际对比 | PlanVsActual.vue + comparison 接口 | ✅ |
| 9 | 日历视图 | FullCalendar + 颜色标识 | ✅ |
| 10 | 统计分析图表 | ECharts 饼图/柱状图/折线图 | ✅ |
| 11 | 通知系统 | WebSocket + notification 表 | ✅ |
| 12 | 附件上传 | FileUpload.vue + 大小/格式校验 | ✅ |
| 13 | 数据权限隔离 | 员工看自己、领导看下属（递归） | ✅ |

---

## 四、改动汇总统计

### 4.1 按优先级分类

| 优先级 | 编号 | 改动项 | 难度 | 涉及文件 |
|:---:|:--:|------|:--:|:------:|
| 🔴 | G-1 | 计划-成果：一对一→一对多 | 高 | ~15 |
| 🔴 | G-9 | 软删除 | 中 | ~12 |
| 🔴 | G-18 | 修改留痕 | 中 | ~8 |
| 🔴 | G-12 | 操作日志 | 中 | ~7 |
| 🟡 | G-2 | 撤回功能 | 低 | ~6 |
| 🟡 | G-3 | 角色合并（3→2） | 低 | ~6 |
| 🟡 | G-4 | 状态重命名（驳回→待修改） | 低 | ~5 |
| 🟡 | G-5 | 驳回理由双重强制 | 低 | ~3 |
| 🟡 | G-8 | 批量审批 | 中 | ~5 |
| 🟡 | G-13 | 业务规则校验（防重复） | 中 | ~4 |
| 🟡 | G-16 | 个人工作台首页 | 中 | ~6 |
| 🟢 | G-6 | 自动归档 | 中 | ~6 |
| 🟢 | G-7 | Excel导出 | 中 | ~5 |
| 🟢 | G-10 | 计划模板+一键复制 | 中 | ~8 |
| 🟢 | G-11 | 定时催办提醒 | 中 | ~5 |
| 🟢 | G-14 | 移动端响应式适配 | 中 | ~8 |
| 🟢 | G-15 | 评论/讨论区 | 中 | ~6 |
| 🟢 | G-17 | 逾期追踪 | 中 | ~6 |

### 4.2 按新增表统计

| 序号 | 新表名 | 说明 | 关联改动 |
|:--:|------|------|:--:|
| 1 | `achievement_plan_ref` | 成果-计划多对多关联 | G-1 |
| 2 | `plan_template` | 个人计划模板 | G-10 |
| 3 | `recycle_bin` | 回收站（软删除记录） | G-9 |
| 4 | `operation_log` | 操作审计日志 | G-12 |
| 5 | `plan_comment` | 计划评论 | G-15 |
| 6 | `plan_revision` | 计划修改版本留痕 | G-18 |

**现有11张表 + 新增6张表 = 共17张表**
| **合计需改动** | **18项** | **~80+个文件** |

---

## 五、HR需求完整文字说明（供截图配文）

基于以上分析，对HR需求的完整理解如下：

> **日/月计划和成果验收系统**，服务于部门内部（约10-30人），覆盖工作计划管理和成果验收的完整闭环。
>
> **用户角色**：分为**员工**和**领导**两种。员工填写计划和提交成果；领导审批计划、验收成果，同时兼任系统管理（用户管理、审批链配置等）。同一系统内通过权限区分菜单。登录后进入**个人工作台首页**，一览今日待办、审批进度和统计概览。
>
> **计划体系**：支持**日报、周报、月报**三级计划。员工可通过**模板**和**一键复制历史计划**快速填写，减少重复输入。每天/每周/每月限制一份（防止重复提交）。日历视图支持日/周/月切换，不同颜色标识状态。日报→周报→月报支持逐级汇总引用。
>
> **审批流程**：支持**多级审批链**（默认两级：直属领导→部门负责人）。领导可**批量通过**多条待审批计划（大幅提高效率）。审批操作包括通过、驳回（附强制理由，前端按钮禁用+后端校验双重强制）、转审（限同部门）。同级多审批人需全部通过才能进入下一级，同级可互见审批意见。审批全流程可追溯。计划被驳回后员工修改重新提交，系统自动**记录修改差异**，领导可看到具体改了什么。
>
> **成果管理**：**成果可关联多条已通过的计划**（一对多），一项工作成果可汇总引用多条计划条目。成果提交支持附件上传（截图、文档等佐证材料）。领导验收时系统展示**计划 vs 实际对比**（指标偏差一目了然）。
>
> **核心业务规则**：
> 1. 计划/成果在**草稿**和**待修改**（被驳回后）状态可编辑
> 2. 提交后审批前可**撤回**修改，审批通过后**锁定不可改**
> 3. 驳回**必须附带具体理由**（双重强制），方便员工针对性整改
> 4. 计划到期后**自动归档**为只读状态
> 5. 计划截止后未提交成果→标记**逾期**，在统计中体现
> 6. 所有删除操作**软删除**（标记删除，数据不真删），误删30天内可从回收站恢复
>
> **沟通协作**：计划和成果详情页支持**评论/讨论区**，员工和领导可在正式审批之外自由沟通交流。
>
> **催办提醒**：工作日定时（9:15）自动检查未提交日报的员工并发送通知提醒；领导也可**一键催办**未提交人员。审批超时（4小时未处理）自动提醒审批人。
>
> **统计查询**：支持按日/周/月、状态、优先级、时间范围、分类等多维度筛选。个人统计（完成率、按时提交率、逾期率）和团队统计（领导视角含排名）以图表展示。支持**Excel数据导出**，方便季度绩效考核和复盘。
>
> **审计合规**：所有关键操作（创建/修改/删除/提交/撤回/审批）自动记录**操作日志**，包含操作人、时间、IP、变更摘要，支持审计追溯。
>
> **移动办公**：核心页面（填日报、查看通知、审批）支持**手机浏览器响应式适配**，方便员工随时随地填报。
>
> **消息通知**：WebSocket 实时推送审批状态变更，右上角铃铛+红点提示。通知场景覆盖：新审批到达、审批结果（通过/驳回）、催办提醒、逾期预警。

---

## 六、完整状态流转图

### 6.1 计划状态流转
```
                        ┌─ 撤回 ──────────────────────────┐
                        │                                  │
  草稿(DRAFT) ──提交──> 已提交(SUBMITTED) ──审批链启动──> 审批中(APPROVING)
    ▲                     │                                  │
    │                     │                    ┌─────────────┼─────────────┐
    │   ┌─────────────────┘                    │             │             │
    │   │                                      ▼             ▼             ▼
    │   │                              全部通过→已通过    有人驳回→待修改   超时→逾期
    │   │                              (APPROVED)    (REJECTED)    (OVERDUE)
    │   │                                   │             │
    │   │                             到期自动归档    修改后重新提交
    │   │                             (ARCHIVED)    ──> 已提交
    │   │
    └───┴── 员工在撤回/驳回后可编辑修改
```

### 6.2 成果状态流转
```
  待填写(PENDING) ──提交──> 已提交(SUBMITTED) ──验收链启动──> 验收中(APPROVING)
      ▲                         │                                  │
      │                         │ 撤回                     ┌──────┴──────┐
      │                         ▼                          │             │
      └────────────────── 撤销回待填写              全部通过→已通过    有人驳回→待修改
                                                      (APPROVED)    (REJECTED)
                                                                        │
                                                                  修改后重新提交
                                                                  ──> 已提交
```

---

> **文档版本**：v1.0  
> **生成日期**：2026-06-20  
> **基于**：`doc/proposal.md`、`doc/high-Level-design.md`、`doc/detailed-design.md`、`db/init.sql` 及全部源码
