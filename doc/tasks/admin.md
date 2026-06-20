# 模块：系统管理（admin）

> **优先级**：P2  
> **依赖**：`common`、`auth`、`user`、`approval`（审批链配置相关）  
> **说明**：管理员后台——用户管理、部门管理、审批链配置、项目分类管理

---

## 一、后端 - 用户管理

- [ ] **T01** [后端] 实现 `GET /api/v1/admin/users` 用户列表
  - 支持搜索：username / realName 模糊搜索
  - 支持按 deptId、role、enabled 筛选
  - 分页返回
- [ ] **T02** [后端] 实现 `POST /api/v1/admin/users` 添加用户
  - 必填：username、password、realName、role、deptId
  - 密码 BCrypt 加密存储
  - 校验 username 唯一
- [ ] **T03** [后端] 实现 `PUT /api/v1/admin/users/{id}` 编辑用户
  - 可修改：realName、role、deptId、leaderId、email、phone
  - 不可修改：username
- [ ] **T04** [后端] 实现 `PUT /api/v1/admin/users/{id}/disable` 禁用/启用
  - 切换 enabled 状态（1 ↔ 0）
  - 禁用的用户无法登录
- [ ] **T05** [后端] 实现 `PUT /api/v1/admin/users/{id}/reset-password` 重置密码
  - 重置为默认密码（如 `123456`），BCrypt 加密

## 二、后端 - 部门管理

- [ ] **T06** [后端] 实现 `GET /api/v1/admin/departments` 部门列表
  - 返回树形结构（含子部门）
- [ ] **T07** [后端] 实现 `POST /api/v1/admin/departments` 添加部门
  - 支持设置 parent_id 创建子部门
- [ ] **T08** [后端] 实现 `PUT /api/v1/admin/departments/{id}` 编辑部门
- [ ] **T09** [后端] 实现 `DELETE /api/v1/admin/departments/{id}` 删除部门
  - 有子部门或有用户关联时不可删除

## 三、后端 - 项目分类管理

- [ ] **T10** [后端] 实现分类 CRUD
  - `GET /api/v1/admin/categories` 列表（可按部门筛选）
  - `POST /api/v1/admin/categories` 添加
  - `PUT /api/v1/admin/categories/{id}` 修改
  - `DELETE /api/v1/admin/categories/{id}` 删除

## 四、前端 - 管理页面

- [ ] **T11** [前端] 实现用户管理页 `views/admin/UserManage.vue`
  - 表格：用户名、姓名、角色、部门、领导、状态、创建时间
  - 操作：添加、编辑、禁用/启用、重置密码
  - 添加/编辑弹窗：包含所有必填字段的表单
- [ ] **T12** [前端] 实现部门管理页 `views/admin/DeptManage.vue`
  - Element Plus Tree 树形展示
  - 操作：添加顶级部门、添加子部门、编辑、删除
- [ ] **T13** [前端] 实现审批链配置页 `views/admin/ApprovalChainConfig.vue`
  - 筛选：部门 + 计划类型
  - 表格：审批级别、审批人、排序
  - 操作：添加审批链、编辑、删除
  - 支持同一级别添加多个审批人
- [ ] **T14** [前端] 实现分类管理页 `views/admin/CategoryManage.vue`
  - 表格：分类名称、所属部门、排序
  - 操作：添加、编辑、删除
- [ ] **T15** [前端] 封装管理相关 API `api/admin.js`
