# 模块：计划管理（plan）

> **优先级**：P1  
> **依赖**：`common`、`auth`、`user`（需要数据权限过滤）  
> **说明**：日/周/月计划的增删改查、提交、日历视图、日报→周报→月报软关联

---

## 一、后端 - 实体与数据访问

- [ ] **T01** [后端] 创建 `Plan` 实体类（MyBatis-Plus 映射 `plan` 表）
- [ ] **T02** [后端] 创建 `PlanMapper`（继承 BaseMapper）
  - 自定义查询：按条件分页查询（含用户姓名、分类名称联表）
  - 日历查询：按用户 + 月份查计划列表

## 二、后端 - 计划 CRUD

- [ ] **T03** [后端] 实现 `POST /api/v1/plans` 创建计划
  - 校验必填字段（title、planType、startTime、endTime）
  - 如果 `submitDirectly=true`，创建后自动提交
  - 如果 `refPlanIds` 不为空（周报/月报），建立软关联
- [ ] **T04** [后端] 实现 `PUT /api/v1/plans/{id}` 修改计划
  - 仅 `DRAFT` 或 `REJECTED` 状态可修改
  - 校验当前用户是计划创建人
- [ ] **T05** [后端] 实现 `DELETE /api/v1/plans/{id}` 删除计划
  - 仅 `DRAFT` 状态可删除
  - 同时删除关联的 `plan_weekly_ref` / `plan_monthly_ref` 记录
- [ ] **T06** [后端] 实现 `GET /api/v1/plans` 计划列表查询
  - 支持多条件筛选：planType、status、priority、startDate~endDate、categoryId、userId、keyword
  - 集成数据权限过滤（员工只看到自己的，领导看到下属的）
  - 每条记录附带 `hasAchievement`（是否已有成果）、`achievementId`

## 三、后端 - 计划提交

- [ ] **T07** [后端] 实现 `POST /api/v1/plans/{id}/submit` 提交计划
  - 校验状态为 `DRAFT` 或 `REJECTED`
  - 查 `approval_chain` → 如未配置，回退默认规则（直属领导→部门负责人）
  - 为第一级所有审批人创建 `approval_record`（action=NULL）
  - 更新 plan.status → `APPROVING`
  - 调用 `NotificationService` 发送通知给所有第一级审批人

## 四、后端 - 计划详情

- [ ] **T08** [后端] 实现 `GET /api/v1/plans/{id}` 计划详情
  - 返回计划基本信息
  - 附带审批时间线（查 `approval_record`）
  - 附带引用的下级计划列表（查 `plan_weekly_ref` / `plan_monthly_ref`）
  - 附带关联的成果信息（如有）

## 五、后端 - 日历视图

- [ ] **T09** [后端] 实现 `GET /api/v1/plans/calendar` 日历视图
  - 入参 year + month + 可选 planType
  - 返回按日期分组的计划列表
  - 数据权限过滤

## 六、后端 - 计划汇总引用

- [ ] **T10** [后端] 创建 `PlanRollupService`
  - `getAvailableDailyPlans(userId, weekStart, weekEnd)` → 查本周已通过的日报
  - `getAvailableWeeklyPlans(userId, monthStart, monthEnd)` → 查本月已通过的周报
  - 返回时标记 `alreadyRefed`（是否已被其他周报/月报引用）
- [ ] **T11** [后端] 实现 `GET /api/v1/plans/{id}/rollup-options` 获取可引用计划
  - 根据当前计划的 planType（WEEKLY→拉日报，MONTHLY→拉周报）
  - 根据当前计划的 startTime~endTime 确定时间范围

## 七、前端 - 计划列表

- [ ] **T12** [前端] 实现计划列表页 `views/plan/PlanList.vue`
  - 筛选区域：planType 下拉、status 下拉、priority 下拉、日期范围选择器、分类下拉、关键词搜索
  - 表格列：标题、类型、优先级、状态（PlanStatusTag）、开始时间、结束时间、分类、操作
  - 操作按钮：
    - 草稿：编辑、删除、提交
    - 驳回：编辑、提交
    - 审批中/已通过：查看详情
    - 已通过+无成果：提交成果按钮（跳转成果提交页）
    - 已通过+有成果：查看成果按钮
  - 分页组件

## 八、前端 - 计划表单

- [ ] **T13** [前端] 实现计划表单组件 `components/plan/PlanForm.vue`
  - 日/周/月类型切换（Tabs）
  - 标题输入框
  - 富文本编辑器（描述）
  - 时间范围选择器（日报=单个日期，周报=周期，月报=月）
  - 优先级选择（Radio）
  - 项目分类下拉
  - 量化指标输入
  - 引用计划区域（周报/月报时显示，多选列表）
  - 保存草稿 / 直接提交 两个按钮
- [ ] **T14** [前端] 实现新建计划页 `views/plan/PlanCreate.vue`
  - 引入 PlanForm 组件
  - 支持 URL query 参数 `?type=daily|weekly|monthly` 预选类型
- [ ] **T15** [前端] 实现编辑计划页 `views/plan/PlanEdit.vue`
  - 引入 PlanForm 组件，预填已有数据

## 九、前端 - 计划详情

- [ ] **T16** [前端] 实现计划详情页 `views/plan/PlanDetail.vue`
  - 计划信息卡片
  - 审批时间线组件（ApprovalTimeline）
  - 引用的下级计划列表
  - 成果区域（如有，含跳转查看详情）

## 十、前端 - 日历视图

- [ ] **T17** [前端] 实现日历视图 `views/plan/PlanCalendar.vue`
  - 集成 FullCalendar Vue 组件
  - 日/周/月视图切换
  - 事件颜色映射：绿色=已通过、橙色=审批中、红色=已驳回、灰色=草稿
  - 点击事件 → 弹出计划详情卡片/跳转详情页

## 十一、前端 - 公共组件

- [ ] **T18** [前端] 实现 `components/plan/PlanStatusTag.vue`
  - 根据 status 显示不同颜色的 Element Plus Tag
  - 草稿=灰色、已提交=蓝色、审批中=橙色、已通过=绿色、已驳回=红色
- [ ] **T19** [前端] 封装计划相关 API `api/plan.js`
  - 包含所有计划接口的 Axios 调用方法
