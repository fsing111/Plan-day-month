# 模块：用户与组织（user）

> **优先级**：P1  
> **依赖**：`common`、`auth`（需要认证和 UserContext）  
> **说明**：用户实体、部门实体、数据权限基础方法。前端管理页面由 admin 模块负责

---

## 一、后端 - 实体与数据访问

- [ ] **T01** [后端] 创建 `User` 实体类（MyBatis-Plus 映射 `user` 表）
  - 包含所有字段 + `@TableField` 映射
  - 密码字段不返回前端（`@JsonIgnore` 或 DTO 控制）
- [ ] **T02** [后端] 创建 `Department` 实体类（映射 `department` 表）
- [ ] **T03** [后端] 创建 `UserMapper` / `DepartmentMapper`（MyBatis-Plus BaseMapper）

## 二、后端 - 服务层

- [ ] **T04** [后端] 创建 `UserService`
  - `getById(id)` → 查用户
  - `getByUsername(username)` → 按用户名查（登录用）
  - `getAllSubordinateIds(leaderId)` → **递归**查所有下级用户 ID（含间接下级）
    - 先查 `leader_id = leaderId` 的直接下级
    - 对每个直接下级递归调用，收集所有 ID
    - 返回包括直接下级+间接下级的完整 ID 列表
  - `listByDept(deptId)` → 按部门查用户
- [ ] **T05** [后端] 创建 `DepartmentService`
  - `getTree()` → 返回部门树结构
  - `getById(id)` → 查单个部门

## 三、后端 - 数据权限拦截

- [ ] **T06** [后端] 创建 `DataScopeAspect` 或 Service 层数据过滤方法
  - 员工（EMPLOYEE）：查询时自动加 `user_id = currentUserId`
  - 领导（LEADER）：查询时自动加 `user_id IN (自己 + 所有下级ID)`
  - 管理员（ADMIN）：不加过滤
- [ ] **T07** [后端] 在各业务 Service 中集成数据权限过滤
  - PlanService 查询计划时应用
  - AchievementService 查询成果时应用

## 四、后端 - 个人中心接口

- [ ] **T08** [后端] `PUT /api/v1/profile` 修改个人信息（姓名、邮箱、手机）
- [ ] **T09** [后端] `PUT /api/v1/profile/password` 修改密码
  - 需输入旧密码验证，新密码 BCrypt 加密

## 五、前端 - 个人中心

- [ ] **T10** [前端] 实现个人中心页 `views/profile/ProfileView.vue`
  - 显示个人信息
  - 修改基本信息（姓名、邮箱、手机）
  - 修改密码（旧密码 + 新密码 + 确认新密码）
