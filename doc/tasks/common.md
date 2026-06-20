# 模块：公共基础（common）

> **优先级**：P0（所有业务模块的前置依赖）  
> **依赖**：`init-project`、`init-database`  
> **说明**：后端公共类、工具、配置，被所有业务模块引用

---

## 一、统一响应封装

- [x] **T01** [后端] 创建 `Result<T>` 通用响应体 ✅
  - `code`、`message`、`data` 三个字段
  - 静态工厂方法：`Result.success(data)`、`Result.error(code, msg)`
- [x] **T02** [后端] 创建 `PageResult<T>` 分页响应体 ✅
  - `records`、`total`、`page`、`pageSize`

## 二、异常处理

- [x] **T03** [后端] 创建 `BusinessException` 业务异常类 ✅
  - 包含 `errorCode` 和 `message`
- [x] **T04** [后端] 创建 `ErrorCode` 错误码枚举 ✅
  - 包含详细设计中定义的 24 个错误码（含业务码1001-5001）
- [x] **T05** [后端] 创建 `GlobalExceptionHandler` 全局异常处理器 ✅
  - 处理 `BusinessException` → 返回对应 code/message
  - 处理 `MethodArgumentNotValidException`（参数校验失败）→ 400
  - 处理 `BindException` → 400
  - 处理 `MaxUploadSizeExceededException` → 文件大小超限
  - 处理 `Exception`（兜底）→ 500

## 三、枚举定义

- [x] **T06** [后端] 创建所有 8 个枚举类 ✅
  - `PlanType`：DAILY / WEEKLY / MONTHLY
  - `Priority`：HIGH / MEDIUM / LOW
  - `PlanStatus`：DRAFT / SUBMITTED / APPROVING / APPROVED / REJECTED
  - `AchievementStatus`：PENDING / SUBMITTED / APPROVING / APPROVED / REJECTED
  - `UserRole`：EMPLOYEE / LEADER / ADMIN
  - `ApprovalAction`：APPROVE / REJECT / TRANSFER
  - `NotificationType`：PLAN_REJECTED / PLAN_APPROVED / NEW_APPROVAL_PLAN / NEW_APPROVAL_ACHV / REMIND_SUBMIT_PLAN / REMIND_APPROVE / ACHV_REJECTED / ACHV_APPROVED
  - `ComparisonStatus`：MATCH / PARTIAL / EXCEED / NOT_MATCH

## 四、通用配置

- [x] **T07** [后端] CORS 跨域配置 ✅
- [x] **T08** [后端] MyBatis-Plus 分页插件配置 ✅
- [x] **T09** [后端] Jackson 日期格式化配置（`yyyy-MM-dd HH:mm:ss`） ✅

## 五、通用工具

- [x] **T10** [后端] `DateUtils` 日期工具类 ✅
  - 获取某周的第一天（周一）/ 最后一天（周日）
  - 获取某月的第一天 / 最后一天
  - 日期格式化/解析
- [x] **T11** [后端] `UserContext` 当前用户上下文持有类 ✅
  - ThreadLocal 存储当前请求的用户 ID、角色、部门 ID
  - 在 JWT 过滤器中设置，请求结束后清理
