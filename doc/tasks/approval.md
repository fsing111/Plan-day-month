# 模块：审批流（approval）

> **优先级**：P1  
> **依赖**：`common`、`auth`、`user`、`notification`（审批结果需推送通知）  
> **说明**：多级审批链配置、审批流转（通过/驳回/转审）、审批时间线

---

## 一、后端 - 实体与数据访问

- [ ] **T01** [后端] 创建 `ApprovalRecord` 实体类（映射 `approval_record` 表）
- [ ] **T02** [后端] 创建 `ApprovalChain` 实体类（映射 `approval_chain` 表）
- [ ] **T03** [后端] 创建 `ApprovalRecordMapper` / `ApprovalChainMapper`

## 二、后端 - 审批链服务

- [ ] **T04** [后端] 创建 `ApprovalChainService`
  - `getChain(deptId, planType)` → 查询审批链配置，按 approval_level + sort_order 排序
  - `getDefaultChain(userId)` → 回退默认规则
    - Level 1: 直属领导（查 user.leader_id）
    - Level 2: 部门负责人（查 department 表中本部门的负责人，或 dept 的 leader）
  - 返回结构：`Map<Integer, List<Long>>`（level → 审批人 ID 列表）
- [ ] **T05** [后端] 创建 `ApprovalService`
  - `startApproval(targetId, targetType, deptId, planType, submitterId)`
    - 调 ApprovalChainService 获取审批链
    - 为第一级所有审批人批量创建 approval_record
    - 更新 target（plan/achievement）状态为 APPROVING
    - 调 NotificationService 通知所有第一级审批人

## 三、后端 - 审批操作

- [ ] **T06** [后端] 实现 `POST /api/v1/approvals/{recordId}/approve` 审批通过
  - 校验当前用户是该 record 的审批人
  - 校验该 record 尚未被处理（action IS NULL）
  - 更新 record：action=APPROVE, comment, approved_at=now
  - **检查该级是否全部通过**：
    - 查该 target 同 approval_level 的所有 record
    - 如果全部 action=APPROVE → 该级通过
    - 有任何一个未审批 → 不推进，返回"等待同级其他审批人"
  - **该级通过后**：
    - 判断是否为最后一级：
      - 是 → 更新 target 状态=APPROVED，通知提交人
      - 否 → 为下一级所有审批人创建 record，通知下一级审批人，不更新 target 状态
- [ ] **T07** [后端] 实现 `POST /api/v1/approvals/{recordId}/reject` 审批驳回
  - 校验同上
  - 更新 record：action=REJECT, comment, approved_at=now
  - 更新 target 状态=REJECTED
  - 通知提交人"被驳回"，附带驳回原因
  - 同级的其他待审批 record 无需处理（后端查询时自动忽略被驳回的 target）
- [ ] **T08** [后端] 实现 `POST /api/v1/approvals/{recordId}/transfer` 转审
  - 校验 `targetUserId` 与本用户同部门（dept_id 相同）
  - 更新当前 record：action=TRANSFER, comment
  - 创建新的 approval_record：approver_id=targetUserId，同 level、同 sort_order
  - 通知新的审批人

## 四、后端 - 审批查询

- [ ] **T09** [后端] 实现 `GET /api/v1/approvals/pending` 待审批列表
  - 查当前用户的 approval_record（action IS NULL）
  - 关联查 target（plan/achievement）的标题、提交人、提交时间
  - 每条附带 `peerApprovals`：同 target 同 level 的其他审批人审批情况（同级互见）
  - 支持分页
- [ ] **T10** [后端] 实现 `GET /api/v1/approvals/history` 审批历史
  - 查当前用户的 approval_record（action IS NOT NULL）
  - 按审批时间倒序，支持分页
- [ ] **T11** [后端] 实现 `GET /api/v1/approvals/{targetType}/{targetId}/timeline` 审批时间线
  - 按 level 分组返回全部审批记录
  - 每级标注 `levelLabel`（一级审批/二级审批...）
  - 包含审批人姓名、操作、意见、时间

## 五、后端 - 审批链管理接口（管理员）

- [ ] **T12** [后端] 实现审批链 CRUD 接口（参考 admin 模块）
  - `GET /api/v1/admin/approval-chains` 列表
  - `POST /api/v1/admin/approval-chains` 添加
  - `PUT /api/v1/admin/approval-chains/{id}` 修改
  - `DELETE /api/v1/admin/approval-chains/{id}` 删除

## 六、前端 - 待审批列表

- [ ] **T13** [前端] 实现待审批列表 `views/approval/ApprovalPending.vue`
  - Tab 切换：计划审批 / 成果验收
  - 表格列：提交人、标题、类型、优先级、提交时间、同级审批状态、操作
  - 操作按钮：审批（弹出 ApprovalDialog）
- [ ] **T14** [前端] 实现审批弹窗 `components/approval/ApprovalDialog.vue`
  - 三个操作按钮：通过（绿色）/ 驳回（红色）/ 转审（蓝色）
  - 审批意见输入框（必填，驳回时意见必填）
  - 转审时：选择同部门用户（下拉搜索）
  - **同级审批意见展示**：显示同 target 同 level 其他人的审批意见（可见）
  - 提交后刷新列表

## 七、前端 - 审批历史与时间线

- [ ] **T15** [前端] 实现审批历史 `views/approval/ApprovalHistory.vue`
  - 表格列：提交人、标题、我的操作、意见、审批时间
- [ ] **T16** [前端] 实现审批时间线组件 `components/approval/ApprovalTimeline.vue`
  - Element Plus Timeline 时间线组件
  - 每级审批一个节点
  - 节点内显示：审批人、操作（通过/驳回/转审）、意见、时间
  - 颜色：通过=绿色、驳回=红色、转审=蓝色、待审批=灰色
  - 同级多人时并列显示

## 八、前端 - 公共

- [ ] **T17** [前端] 封装审批相关 API `api/approval.js`
