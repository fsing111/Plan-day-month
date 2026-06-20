# 模块：数据库初始化

> **优先级**：P0（项目初始化后立即执行）  
> **依赖**：`init-project`（需要 MySQL 服务可用）  
> **说明**：执行 DDL 建表、插入初始化数据

---

- [x] **T01** 编写完整 `db/init.sql`（11 张表 DDL） ✅
  - `department` 部门表
  - `user` 用户表
  - `project_category` 项目分类表
  - `plan` 计划表
  - `achievement` 成果表
  - `approval_chain` 审批链配置表
  - `approval_record` 审批记录表
  - `notification` 通知表
  - `attachment` 附件表
  - `plan_weekly_ref` 日报-周报关联表
  - `plan_monthly_ref` 周报-月报关联表
- [x] **T02** 编写种子数据（含 init.sql 中） ✅
  - 默认部门（id=1, name='默认部门'）
  - 管理员账号（admin / BCrypt 加密密码）
  - 4 个默认项目分类
- [x] **T03** 在本地 MySQL 执行 init.sql，验证所有 11 张表建表成功 ✅
- [x] **T04** 配置 MyBatis-Plus（MyBatisPlusConfig + PaginationInnerInterceptor），连接本地 MySQL ✅
- [x] **T05** 确认数据库字符集为 `utf8mb4` ✅
