# 模块：成果管理（achievement）

> **优先级**：P1  
> **依赖**：`common`、`auth`、`user`、`plan`（需要关联计划）  
> **说明**：成果的增删改查、提交验收、计划 vs 实际量化对比

---

## 一、后端 - 实体与数据访问

- [ ] **T01** [后端] 创建 `Achievement` 实体类（映射 `achievement` 表）
- [ ] **T02** [后端] 创建 `AchievementMapper`（继承 BaseMapper）

## 二、后端 - 成果 CRUD

- [ ] **T03** [后端] 实现 `POST /api/v1/achievements` 创建成果
  - 校验关联 plan 状态为 `APPROVED`（只有通过的才能提交成果）
  - 校验该 plan 尚未有关联成果（一对一）
  - 处理 attachmentIds 关联
  - 如果 `submitDirectly=true`，自动提交
- [ ] **T04** [后端] 实现 `PUT /api/v1/achievements/{id}` 修改成果
  - 仅 `PENDING` 或 `REJECTED` 状态可修改
  - 校验当前用户是成果关联计划的创建人
- [ ] **T05** [后端] 实现 `GET /api/v1/achievements` 成果列表
  - 支持筛选：status、planId、日期范围、userId
  - 集成数据权限过滤
  - 每条记录含 planTitle、planType、附件数量

## 三、后端 - 成果提交

- [ ] **T06** [后端] 实现 `POST /api/v1/achievements/{id}/submit` 提交成果
  - 校验状态为 `PENDING` 或 `REJECTED`
  - 查成果审批链（plan_type='ACHIEVEMENT'），未配置则回退默认规则
  - 为第一级审批人创建 approval_record
  - 更新 status → `APPROVING`，记录 submitted_at
  - 发送通知给审批人

## 四、后端 - 成果详情与对比

- [ ] **T07** [后端] 实现 `GET /api/v1/achievements/{id}` 成果详情
  - 返回成果完整信息
  - 附带计划基本信息
  - 附带计划 vs 实际对比（planQuantTarget vs actualQty、planTimeRange vs actualHours）
  - 附带审批时间线
  - 附带附件列表
- [ ] **T08** [后端] 实现 `GET /api/v1/plans/{planId}/achievement` 查某计划的成果
  - 简单返回（planId 对应的 achievement 或 null）
- [ ] **T09** [后端] 实现对比状态判断逻辑
  - `MATCH`：实际完成数量 ≥ 计划指标
  - `PARTIAL`：部分完成
  - `EXCEED`：超额完成
  - `NOT_MATCH`：未完成
  - 优先人工判断，系统给出参考对比

## 五、前端 - 成果提交

- [ ] **T10** [前端] 实现成果表单组件 `components/achievement/AchievementForm.vue`
  - 关联计划选择（下拉，筛选当前用户已通过且未提交成果的计划）
  - 完成说明（富文本编辑器）
  - 实际完成数量输入（与计划量化指标并列展示对比）
  - 实际耗时输入（小时，小数点后1位）
  - 遇到的问题（多行文本）
  - 备注（多行文本）
  - 附件上传区域（FileUpload 组件）
  - 保存 / 提交 按钮
- [ ] **T11** [前端] 实现提交成果页 `views/achievement/AchievementSubmit.vue`
  - URL 参数 `planId` → 自动选中关联计划
  - 引入 AchievementForm 组件

## 六、前端 - 成果详情

- [ ] **T12** [前端] 实现成果详情页 `views/achievement/AchievementDetail.vue`
  - 成果信息展示
  - 计划 vs 实际对比（PlanVsActual 组件）
  - 审批时间线
  - 附件预览/下载

## 七、前端 - 成果列表

- [ ] **T13** [前端] 实现成果列表页 `views/achievement/AchievementList.vue`
  - 筛选：状态、日期范围、计划类型
  - 表格列：计划标题、计划类型、状态、实际耗时、提交时间、操作
  - 点击查看详情

## 八、前端 - 公共组件

- [ ] **T14** [前端] 实现 `components/achievement/PlanVsActual.vue` 对比组件
  - 左右分栏/表格对比：计划 vs 实际
  - 量化指标对比（计划指标 vs 实际完成）
  - 耗时对比
  - 对比结论标签（达成/部分达成/超额/未达成）
- [ ] **T15** [前端] 封装成果相关 API `api/achievement.js`
