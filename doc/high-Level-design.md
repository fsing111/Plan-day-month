# 日/月计划和成果验收系统 - 概要设计文档

## 一、文档概述

### 1.1 文档目的

本文档基于需求文档 `proposal.md`，对系统进行概要设计，明确系统架构、模块划分、模块间关系、数据模型和接口规范，为后续详细设计和编码开发提供依据。

### 1.2 设计原则

| 原则 | 说明 |
|------|------|
| **分层解耦** | 前后端分离，后端分层架构（Controller - Service - DAO） |
| **可扩展** | 审批链、部门、计划分类均可配置扩展 |
| **数据安全** | 严格的数据权限隔离，行级数据过滤 |
| **约定优于配置** | 遵循 Spring Boot 和 Vue 生态的最佳实践 |

---

## 二、系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │ 计划管理  │ │ 成果管理  │ │ 审批中心  │ │ 统计/日历视图  │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │ 消息通知  │ │ 系统管理  │ │ 个人中心  │ │  登录/注册    │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────┘  │
│                          │  HTTP/REST + WebSocket           │
└──────────────────────────┼──────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────┐
│               后端 (Spring Boot 单体应用)                     │
│                          │                                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    控制层 (Controller)                   │  │
│  │  PlanController │ AchievementController │ ApprovalController│
│  │  UserController │ NotificationController│ AdminController  │
│  └─────────────────────────┬─────────────────────────────┘  │
│                            │                                  │
│  ┌─────────────────────────┴─────────────────────────────┐  │
│  │                    业务层 (Service)                      │  │
│  │  PlanService  │ AchievementService │ ApprovalService   │  │
│  │  UserService  │ NotificationService│ FileService       │  │
│  │  StatisticsService │ WebSocketService                  │  │
│  └─────────────────────────┬─────────────────────────────┘  │
│                            │                                  │
│  ┌─────────────────────────┴─────────────────────────────┐  │
│  │                   数据层 (DAO/Repository)                │  │
│  │  PlanRepository │ AchievementRepository │ ...          │  │
│  │  UserRepository │ ApprovalRepository    │ ...          │  │
│  └─────────────────────────┬─────────────────────────────┘  │
│                            │                                  │
└────────────────────────────┼──────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
   ┌────┴─────┐      ┌──────┴──────┐      ┌─────┴─────┐
   │  MySQL   │      │    Redis    │      │ 文件存储   │
   │  :3306   │      │   :6379    │      │ (本地/OSS) │
   └──────────┘      └────────────┘      └───────────┘
```

### 2.2 技术选型明细

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 前端框架 | Vue | 3.x | 整体前端框架 |
| UI 组件库 | Element Plus | 最新稳定版 | 表单、表格、弹窗等 UI 组件 |
| 日历组件 | FullCalendar 或 v-calendar | - | 日历视图 |
| 图表库 | ECharts | 5.x | 统计分析图表 |
| 状态管理 | Pinia | 最新稳定版 | Vue 状态管理 |
| HTTP 客户端 | Axios | - | REST API 请求 |
| WebSocket | SockJS + STOMP | - | 实时消息推送 |
| 路由 | Vue Router | 4.x | 前端路由 |
| 构建工具 | Vite | - | 前端构建 |
| 后端框架 | Spring Boot | 2.7.x 或 3.x | 后端整体框架 |
| 安全框架 | Spring Security | - | 认证和授权 |
| JWT | jjwt | - | Token 生成和校验 |
| ORM | MyBatis-Plus | 3.x | 数据库操作 |
| WebSocket | Spring WebSocket | - | WebSocket 支持 |
| 文件上传 | Spring Multipart | - | 附件上传 |
| API 文档 | Knife4j (Swagger) | - | 接口文档生成 |
| 数据库 | MySQL | 8.0 | 主数据存储 |
| 缓存 | Redis | 7.x | 会话/Token 黑名单/缓存 |
| 容器 | Docker + Docker Compose | - | 容器化部署 |
| Web 服务器 | Nginx | - | 前端静态资源 + 反向代理 |

---

## 三、模块划分

### 3.1 后端模块

```
plan-achievement-system (根模块)
├── common/                         # 公共模块
│   ├── config/                     # 配置类（Security、WebSocket、CORS等）
│   ├── exception/                  # 全局异常处理
│   ├── util/                       # 工具类（JWT工具、日期工具等）
│   ├── dto/                        # 通用 DTO（分页请求、响应包装等）
│   └── constant/                   # 常量/枚举定义
│
├── auth/                           # 认证授权模块
│   ├── controller/AuthController   # 登录/注册/登出
│   ├── service/AuthService         # 认证逻辑
│   ├── filter/JwtAuthFilter        # JWT 鉴权过滤器
│   └── dto/                        # 登录请求/响应 DTO
│
├── user/                           # 用户与组织模块
│   ├── controller/UserController   # 用户 CRUD（管理员）
│   ├── service/UserService         # 用户业务逻辑
│   ├── repository/UserRepository   # 用户数据访问
│   ├── entity/User                 # 用户实体
│   ├── entity/Department           # 部门实体
│   └── dto/                        # 用户相关 DTO
│
├── plan/                           # 计划管理模块
│   ├── controller/PlanController   # 计划 CRUD + 提交
│   ├── service/PlanService         # 计划业务逻辑
│   ├── service/PlanRollupService   # 日报→周报→月报 汇总引用
│   ├── repository/PlanRepository   # 计划数据访问
│   ├── entity/Plan                 # 计划实体
│   ├── entity/PlanType             # 计划类型枚举
│   └── dto/                        # 计划请求/响应 DTO
│
├── achievement/                    # 成果管理模块
│   ├── controller/AchievementController  # 成果 CRUD + 提交
│   ├── service/AchievementService        # 成果业务逻辑（含对比）
│   ├── repository/AchievementRepository  # 成果数据访问
│   ├── entity/Achievement                # 成果实体
│   └── dto/                               # 成果请求/响应 DTO
│
├── approval/                       # 审批流模块
│   ├── controller/ApprovalController     # 审批操作
│   ├── service/ApprovalService           # 审批逻辑（流转、记录）
│   ├── service/ApprovalChainService      # 审批链配置管理
│   ├── repository/ApprovalRecordRepo     # 审批记录数据访问
│   ├── repository/ApprovalChainRepo      # 审批链配置数据访问
│   ├── entity/ApprovalRecord             # 审批记录实体
│   ├── entity/ApprovalChain              # 审批链配置实体
│   ├── enums/ApprovalAction              # 审批操作枚举（通过/驳回/转审）
│   └── dto/                               # 审批相关 DTO
│
├── notification/                   # 消息通知模块
│   ├── controller/NotificationController  # 通知查询/已读
│   ├── service/NotificationService        # 通知创建、查询、标记已读
│   ├── websocket/WebSocketHandler         # WebSocket 连接管理
│   ├── websocket/NotificationPushService  # 实时推送逻辑
│   ├── repository/NotificationRepository  # 通知数据访问
│   ├── entity/Notification               # 通知实体
│   └── dto/                               # 通知 DTO
│
├── file/                           # 附件管理模块
│   ├── controller/FileController   # 文件上传/下载/预览
│   ├── service/FileService         # 文件存储逻辑
│   ├── entity/Attachment           # 附件实体
│   └── config/FileStorageConfig    # 文件存储配置
│
├── statistics/                     # 统计报表模块
│   ├── controller/StatisticsController   # 统计数据接口
│   ├── service/StatisticsService         # 统计计算逻辑
│   └── dto/                               # 统计数据 DTO
│
└── admin/                          # 系统管理模块
    ├── controller/AdminController        # 管理后台接口
    ├── service/AdminService              # 管理业务逻辑
    └── dto/                               # 管理相关 DTO
```

### 3.2 前端模块（页面结构）

```
src/
├── views/                          # 页面视图
│   ├── login/                      # 登录页
│   │   └── LoginView.vue
│   ├── plan/                       # 计划管理
│   │   ├── PlanList.vue            # 计划列表（含筛选）
│   │   ├── PlanCreate.vue          # 新建计划（日/周/月）
│   │   ├── PlanDetail.vue          # 计划详情
│   │   └── PlanCalendar.vue        # 日历视图
│   ├── achievement/                # 成果管理
│   │   ├── AchievementList.vue     # 成果列表
│   │   ├── AchievementSubmit.vue   # 提交成果
│   │   └── AchievementDetail.vue   # 成果详情（含计划vs实际对比）
│   ├── approval/                   # 审批中心
│   │   ├── ApprovalPending.vue     # 待审批列表
│   │   └── ApprovalHistory.vue     # 审批历史
│   ├── statistics/                 # 统计分析
│   │   ├── PersonalStats.vue       # 个人统计
│   │   └── TeamStats.vue           # 团队统计（领导可见）
│   ├── notification/               # 消息通知
│   │   └── NotificationList.vue    # 通知列表
│   ├── admin/                      # 系统管理（管理员）
│   │   ├── UserManage.vue          # 用户管理
│   │   ├── DeptManage.vue          # 部门管理
│   │   ├── ApprovalChainConfig.vue # 审批链配置
│   │   └── CategoryManage.vue      # 项目分类管理
│   └── profile/                    # 个人中心
│       └── ProfileView.vue
│
├── components/                     # 公共组件
│   ├── layout/
│   │   ├── AppLayout.vue           # 主布局（侧边栏+顶栏+内容区）
│   │   ├── Sidebar.vue             # 侧边栏导航
│   │   └── Topbar.vue              # 顶栏（用户信息、通知铃铛）
│   ├── plan/
│   │   ├── PlanForm.vue            # 计划表单组件（日/周/月复用）
│   │   └── PlanStatusTag.vue       # 计划状态标签
│   ├── achievement/
│   │   ├── AchievementForm.vue     # 成果表单组件
│   │   └── PlanVsActual.vue        # 计划vs实际对比组件
│   ├── approval/
│   │   ├── ApprovalDialog.vue      # 审批操作弹窗
│   │   └── ApprovalTimeline.vue    # 审批时间线/流程展示
│   ├── common/
│   │   ├── FileUpload.vue          # 附件上传组件
│   │   ├── FilePreview.vue         # 附件预览组件
│   │   └── NotificationBell.vue    # 通知铃铛（红点+下拉）
│   └── charts/
│       ├── PieChart.vue            # 饼图
│       ├── BarChart.vue            # 柱状图
│       └── LineChart.vue           # 折线图
│
├── stores/                         # Pinia 状态管理
│   ├── user.js                     # 用户信息状态
│   ├── notification.js             # 通知状态（未读数等）
│   └── app.js                      # 全局状态（侧边栏折叠等）
│
├── api/                            # API 接口封装
│   ├── request.js                  # Axios 实例（拦截器、Token注入）
│   ├── auth.js                     # 认证接口
│   ├── plan.js                     # 计划接口
│   ├── achievement.js              # 成果接口
│   ├── approval.js                 # 审批接口
│   ├── notification.js             # 通知接口
│   ├── file.js                     # 文件接口
│   ├── statistics.js               # 统计接口
│   └── admin.js                    # 管理接口
│
├── router/                         # 路由配置
│   └── index.js                    # 路由表 + 导航守卫
│
└── utils/                          # 工具函数
    ├── websocket.js                # WebSocket 连接管理
    ├── date.js                     # 日期格式化工具
    └── auth.js                     # Token 存取工具
```

---

## 四、模块关系

### 4.1 模块依赖图

```
                        ┌─────────────┐
                        │    auth     │ ← 所有模块依赖它（JWT鉴权）
                        └──────┬──────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
       ┌──────┴──────┐  ┌─────┴──────┐  ┌──────┴──────┐
       │    user      │  │   plan     │  │   admin     │
       │ (用户/部门)   │  │ (计划管理)  │  │ (系统管理)   │
       └──────┬──────┘  └─────┬──────┘  └──────┬──────┘
              │               │                │
              │        ┌──────┴──────┐         │
              │        │ achievement │         │
              │        │  (成果管理)  │         │
              │        └──────┬──────┘         │
              │               │                │
              │        ┌──────┴──────┐         │
              │        │  approval   │─────────┤
              │        │  (审批流)   │         │
              │        └──────┬──────┘         │
              │               │                │
              │        ┌──────┴──────┐         │
              ├────────┤notification │─────────┤
              │        │ (消息通知)   │         │
              │        └──────┬──────┘         │
              │               │                │
              │        ┌──────┴──────┐         │
              │        │    file     │         │
              │        │  (附件管理)  │         │
              │        └─────────────┘         │
              │                                │
              │        ┌──────────────┐        │
              └────────┤ statistics   │────────┘
                       │  (统计报表)   │
                       └──────────────┘
```

### 4.2 模块间调用关系

| 调用方 | 被调用方 | 调用场景 |
|--------|----------|----------|
| `plan` | `user` | 查询计划时检查数据权限、获取提交人信息 |
| `achievement` | `plan` | 提交成果时关联计划、计算计划 vs 实际对比 |
| `approval` | `plan` | 审批计划后更新计划状态 |
| `approval` | `achievement` | 审批成果后更新成果状态 |
| `approval` | `user` | 获取审批人信息、确定下级审批人 |
| `approval` | `notification` | 审批操作后触发通知（驳回/通过提醒） |
| `notification` | `user` | 查询用户以确定通知接收人 |
| `plan` | `notification` | 超时未提交计划触发催办通知 |
| `approval` | `notification` | 超时未审批触发催办通知 |
| `file` | `achievement` | 附件与成果关联 |
| `statistics` | `plan` | 统计完成率、提交率 |
| `statistics` | `achievement` | 统计验收通过率 |
| `statistics` | `user` | 获取团队成员列表、上下级关系 |
| `admin` | `user` | 用户 CRUD、上下级关系配置 |

### 4.3 关键业务流

#### 4.3.1 计划提交流

```
员工填写计划 → 保存草稿
     │
     ▼
  提交计划 → planService.submit()
     │
     ▼
  approvalService.startApproval()
     │
     ├── 根据 approvalChain 配置确定审批人列表
     ├── 创建 ApprovalRecord（第一级审批，状态=待审批）
     ├── 更新计划状态 → 审批中
     └── notificationService.send()
           └── WebSocket 推送 → 审批人收到「待审批通知」
```

#### 4.3.2 多级审批流

```
┌─────────────────────────────────────────────────────┐
│  approvalService.approve(planId, userId, action)     │
│                                                      │
│  1. 记录本次审批操作（通过/驳回/转审 + 意见）         │
│  2. 更新当前 ApprovalRecord 状态                     │
│  3. 判断：                                          │
│     ├── 驳回 → 计划状态=已驳回，通知提交人            │
│     ├── 转审 → 创建新审批记录给转审目标人             │
│     └── 通过 → 判断是否是最后一级                    │
│           ├── 是 → 计划状态=已通过，通知提交人        │
│           └── 否 → 创建下一级审批记录，通知下一审批人  │
└─────────────────────────────────────────────────────┘
```

#### 4.3.3 成果验收流

```
员工选择已通过的计划 → 填写成果 → 提交
     │
     ▼
  achievementService.submit()
     │
     ├── 关联 Plan
     ├── 保存 Achievement（状态=已提交）
     ├── 触发审批流（同上述审批流，审批对象=成果）
     └── 通知审批人
```

#### 4.3.4 日报→周报汇总引用流

```
员工创建周报
     │
     ▼
  planRollupService.getWeeklyDailyPlans(userId, weekRange)
     │
     ├── 查询该用户本周所有「已通过」的日报
     ├── 返回日报列表（标题、完成情况摘要）
     │
     ▼
  员工选择需要引用的日报 → 建立 plan_weekly_ref 关联
     │
     ▼
  周报详情中展示已引用的日报列表
```

---

## 五、数据库设计

### 5.1 数据库 ER 图（核心表）

```
┌──────────────┐       ┌──────────────────┐
│  department  │       │  approval_chain  │
│──────────────│       │──────────────────│
│ id           │◄──────│ department_id(FK)│
│ name         │       │ plan_type        │
│ parent_id    │       │ approval_level   │
│ created_at   │       │ approver_id (FK) │
└──────┬───────┘       │ sort_order       │
       │               └──────────────────┘
       │
       │ 1:N
       │
┌──────┴───────┐       ┌──────────────────┐
│    user      │       │      plan        │
│──────────────│       │──────────────────│
│ id           │◄──────│ user_id (FK)     │
│ username     │  1:N  │ plan_type        │
│ password     │       │ title            │
│ real_name    │       │ description      │
│ role         │       │ priority         │
│ dept_id (FK) │       │ status           │
│ leader_id(FK)│       │ start_time       │
│ enabled      │       │ end_time         │
│ created_at   │       │ category_id (FK) │
└──────┬───────┘       │ quant_target     │
       │               │ created_at       │
       │               └──┬───────┬───────┘
       │                  │       │
       │          1:1     │       │ 1:N
       │                  │       │
       │   ┌──────────────┘       └──────────────────┐
       │   │                                          │
       │   ▼                                          ▼
       │  ┌───────────────┐              ┌──────────────────────┐
       │  │  achievement  │              │   approval_record    │
       │  │───────────────│              │──────────────────────│
       │  │ id            │              │ id                   │
       │  │ plan_id (FK)  │──────1:N────│ target_id (多态关联)  │
       │  │ description   │              │ target_type(PLAN/ACHV)│
       │  │ actual_qty    │              │ approver_id (FK→user)│
       │  │ actual_hours  │              │ approval_level       │
       │  │ issues        │              │ action               │
       │  │ remark        │              │ comment              │
       │  │ status        │              │ approved_at          │
       │  │ submitted_at  │              │ sort_order           │
       │  └───────────────┘              └──────────────────────┘
       │
       │
       │  ┌──────────────────────────────┐
       │  │       attachment             │
       │  │──────────────────────────────│
       │  │ id                           │
       │  │ achievement_id (FK)          │
       │  │ file_name                    │
       │  │ file_path                    │
       │  │ file_size                    │
       │  │ file_type                    │
       │  │ uploaded_at                  │
       │  └──────────────────────────────┘
       │
       │  ┌──────────────────────────────┐
       │  │      notification            │
       │  │──────────────────────────────│
       │  │ id                           │
       │  │ receiver_id (FK→user)        │
       │  │ type                         │
       │  │ title                        │
       │  │ content                      │
       │  │ is_read                      │
       │  │ related_id (关联业务ID)       │
       │  │ created_at                   │
       │  └──────────────────────────────┘
       │
       │  ┌──────────────────────────────┐
       │  │    plan_weekly_ref           │
       │  │──────────────────────────────│
       │  │ id                           │
       │  │ weekly_plan_id (FK→plan)     │
       │  │ daily_plan_id (FK→plan)      │
       │  └──────────────────────────────┘
       │
       │  ┌──────────────────────────────┐
       │  │   plan_monthly_ref           │
       │  │──────────────────────────────│
       │  │ id                           │
       │  │ monthly_plan_id (FK→plan)    │
       │  │ weekly_plan_id (FK→plan)     │
       │  └──────────────────────────────┘
       │
       └──────────┐
                  ▼
       ┌──────────────────────┐
       │   project_category   │
       │──────────────────────│
       │ id                   │
       │ name                 │
       │ dept_id (FK)         │
       │ created_at           │
       └──────────────────────┘
```

### 5.2 核心表字段说明

#### user 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| username | VARCHAR(50) UNIQUE | 登录用户名 |
| password | VARCHAR(255) | 加密密码（BCrypt） |
| real_name | VARCHAR(50) | 真实姓名 |
| role | VARCHAR(20) | 角色：EMPLOYEE / LEADER / ADMIN |
| dept_id | BIGINT FK | 所属部门 ID |
| leader_id | BIGINT FK | 直属领导 ID（自引用→user.id） |
| email | VARCHAR(100) | 邮箱（预留） |
| phone | VARCHAR(20) | 手机号（预留） |
| enabled | TINYINT | 是否启用 1/0 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### plan 计划表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 提交人 ID |
| plan_type | VARCHAR(10) | 计划类型：DAILY / WEEKLY / MONTHLY |
| title | VARCHAR(200) | 计划标题 |
| description | TEXT | 详细描述（富文本 HTML） |
| priority | VARCHAR(10) | 优先级：HIGH / MEDIUM / LOW |
| status | VARCHAR(20) | 状态：DRAFT / SUBMITTED / APPROVING / APPROVED / REJECTED |
| start_time | DATETIME | 计划开始时间 |
| end_time | DATETIME | 计划截止时间 |
| category_id | BIGINT FK | 所属项目/分类 ID |
| quant_target | VARCHAR(200) | 量化指标描述 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### achievement 成果表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| plan_id | BIGINT FK UNIQUE | 关联的计划 ID（一对一） |
| description | TEXT | 完成说明（富文本 HTML） |
| actual_qty | VARCHAR(200) | 实际完成数量（与计划量化指标对应） |
| actual_hours | DECIMAL(5,1) | 实际耗时（小时） |
| issues | TEXT | 遇到的问题 |
| remark | TEXT | 备注 |
| status | VARCHAR(20) | 状态：PENDING / SUBMITTED / APPROVING / APPROVED / REJECTED |
| submitted_at | DATETIME | 提交时间 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### approval_chain 审批链配置表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| dept_id | BIGINT FK | 所属部门 |
| plan_type | VARCHAR(10) | 适用计划类型：DAILY/WEEKLY/MONTHLY/ACHIEVEMENT（成果用单独的审批链） |
| approval_level | INT | 审批级别（1, 2, 3...） |
| approver_id | BIGINT FK | 该级审批人 ID |
| sort_order | INT | 审批顺序 |
| created_at | DATETIME | 创建时间 |

> **说明**：同一部门 + 同一计划类型 + 同一审批级别可配置多个审批人。**全部审批人通过**后该级才算通过。

#### approval_record 审批记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| target_id | BIGINT | 审批目标 ID（计划 ID 或成果 ID） |
| target_type | VARCHAR(20) | 审批目标类型：PLAN / ACHIEVEMENT |
| approver_id | BIGINT FK | 审批人 ID |
| approval_level | INT | 当前审批级别 |
| action | VARCHAR(20) | 审批操作：APPROVE / REJECT / TRANSFER |
| comment | TEXT | 审批意见 |
| approved_at | DATETIME | 审批时间 |
| sort_order | INT | 审批顺序 |
| created_at | DATETIME | 创建时间 |

#### notification 通知表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| receiver_id | BIGINT FK | 接收人 ID |
| type | VARCHAR(30) | 通知类型：PLAN_REJECTED/PLAN_APPROVED/NEW_APPROVAL/REMIND_SUBMIT/REMIND_APPROVE/ACHV_REJECTED/ACHV_APPROVED |
| title | VARCHAR(200) | 通知标题 |
| content | TEXT | 通知内容 |
| is_read | TINYINT | 是否已读 0/1 |
| related_id | BIGINT | 关联的业务 ID（可跳转查看） |
| related_type | VARCHAR(20) | 关联业务类型：PLAN/ACHIEVEMENT |
| created_at | DATETIME | 通知时间 |

#### attachment 附件表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| achievement_id | BIGINT FK | 关联的成果 ID |
| file_name | VARCHAR(255) | 原始文件名 |
| file_path | VARCHAR(500) | 服务器存储路径 |
| file_size | BIGINT | 文件大小（字节） |
| file_type | VARCHAR(50) | MIME 类型 |
| uploaded_at | DATETIME | 上传时间 |

---

## 六、接口设计（概要）

### 6.1 RESTful API 规范

- **Base URL**：`/api/v1`
- **认证方式**：Header `Authorization: Bearer <JWT_TOKEN>`
- **请求格式**：JSON
- **响应格式**：统一响应体

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 6.2 接口清单（按模块）

#### 认证模块 `/api/v1/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录，返回 JWT Token |
| POST | `/auth/logout` | 用户登出，Token 加入 Redis 黑名单 |
| GET | `/auth/me` | 获取当前登录用户信息 |

#### 计划模块 `/api/v1/plans`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/plans` | 查询计划列表（分页+筛选） |
| GET | `/plans/{id}` | 查看计划详情 |
| POST | `/plans` | 创建计划（保存草稿或直接提交） |
| PUT | `/plans/{id}` | 修改计划（仅草稿/驳回状态可修改） |
| POST | `/plans/{id}/submit` | 提交计划 |
| DELETE | `/plans/{id}` | 删除计划（仅草稿状态可删除） |
| GET | `/plans/calendar` | 日历视图数据（按月份查询） |
| GET | `/plans/{id}/rollup-options` | 获取可引用的下级计划（周报引用日报、月报引用周报） |

#### 成果模块 `/api/v1/achievements`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/achievements` | 查询成果列表（分页+筛选） |
| GET | `/achievements/{id}` | 查看成果详情（含计划 vs 实际对比） |
| POST | `/achievements` | 创建成果 |
| PUT | `/achievements/{id}` | 修改成果（仅待填写/驳回状态） |
| POST | `/achievements/{id}/submit` | 提交成果 |
| GET | `/plans/{planId}/achievement` | 查看某计划的成果 |

#### 审批模块 `/api/v1/approvals`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/approvals/pending` | 查询我的待审批列表 |
| GET | `/approvals/history` | 查询我的审批历史 |
| POST | `/approvals/{recordId}/approve` | 通过审批 |
| POST | `/approvals/{recordId}/reject` | 驳回审批 |
| POST | `/approvals/{recordId}/transfer` | 转审给他人 |
| GET | `/approvals/{targetType}/{targetId}/timeline` | 查看某计划/成果的审批时间线 |

#### 通知模块 `/api/v1/notifications`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/notifications` | 查询我的通知列表 |
| GET | `/notifications/unread-count` | 获取未读通知数 |
| PUT | `/notifications/{id}/read` | 标记单条已读 |
| PUT | `/notifications/read-all` | 全部标记已读 |

#### 文件模块 `/api/v1/files`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/files/upload` | 上传附件（multipart/form-data） |
| GET | `/files/{id}/download` | 下载附件 |
| GET | `/files/{id}/preview` | 预览附件（图片直接返回，其他返回文件信息） |
| DELETE | `/files/{id}` | 删除附件 |

#### 统计模块 `/api/v1/statistics`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/statistics/personal` | 个人统计数据 |
| GET | `/statistics/team` | 团队统计数据（领导可见） |
| GET | `/statistics/trend` | 趋势数据（按周/月） |

#### 管理模块 `/api/v1/admin`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users` | 用户列表 |
| POST | `/admin/users` | 添加用户 |
| PUT | `/admin/users/{id}` | 编辑用户 |
| PUT | `/admin/users/{id}/disable` | 禁用/启用用户 |
| GET | `/admin/departments` | 部门列表 |
| POST | `/admin/departments` | 添加部门 |
| PUT | `/admin/departments/{id}` | 编辑部门 |
| GET | `/admin/approval-chains` | 审批链配置列表 |
| POST | `/admin/approval-chains` | 添加审批链配置 |
| PUT | `/admin/approval-chains/{id}` | 修改审批链配置 |
| DELETE | `/admin/approval-chains/{id}` | 删除审批链配置 |
| GET | `/admin/categories` | 项目分类列表 |
| POST | `/admin/categories` | 添加分类 |
| PUT | `/admin/categories/{id}` | 修改分类 |
| DELETE | `/admin/categories/{id}` | 删除分类 |

### 6.3 WebSocket 端点

| 端点 | 说明 |
|------|------|
| `/ws/notifications` | 通知推送通道，客户端连接后接收实时通知 |

**推送消息格式**：
```json
{
  "type": "NOTIFICATION",
  "data": {
    "id": 123,
    "type": "NEW_APPROVAL",
    "title": "新的待审批计划",
    "content": "张三提交了日报计划，请审批",
    "createdAt": "2026-06-20T09:30:00"
  }
}
```

---

## 七、前端路由设计

| 路径 | 页面 | 权限 | 说明 |
|------|------|:---:|------|
| `/login` | 登录页 | 公开 | 登录界面 |
| `/` | 重定向到 /plans | 登录 | 默认首页 |
| `/plans` | 计划列表 | 登录 | 我的计划（员工）/ 下属计划（领导） |
| `/plans/create` | 新建计划 | 登录 | 日/周/月计划表单 |
| `/plans/create?type=daily` | 新建日报 | 登录 | 快捷新建日报 |
| `/plans/create?type=weekly` | 新建周报 | 登录 | 快捷新建周报 |
| `/plans/create?type=monthly` | 新建月报 | 登录 | 快捷新建月报 |
| `/plans/:id` | 计划详情 | 登录 | 含审批时间线 |
| `/plans/calendar` | 日历视图 | 登录 | 日/周/月切换 |
| `/achievements` | 成果列表 | 登录 | 我的成果 / 下属成果 |
| `/achievements/submit/:planId` | 提交成果 | 登录 | 基于某计划提交成果 |
| `/achievements/:id` | 成果详情 | 登录 | 含计划 vs 实际对比 |
| `/approvals/pending` | 待审批 | 领导 | 待审批的计划和成果 |
| `/approvals/history` | 审批历史 | 领导 | 已处理的审批记录 |
| `/statistics/personal` | 个人统计 | 登录 | 个人完成率图表 |
| `/statistics/team` | 团队统计 | 领导 | 团队整体统计 |
| `/notifications` | 通知列表 | 登录 | 全部通知 |
| `/admin/users` | 用户管理 | 管理员 | 用户 CRUD |
| `/admin/departments` | 部门管理 | 管理员 | 部门管理 |
| `/admin/approval-chains` | 审批链配置 | 管理员 | 审批链 CRUD |
| `/admin/categories` | 分类管理 | 管理员 | 项目分类管理 |
| `/profile` | 个人中心 | 登录 | 个人信息、修改密码 |

### 7.1 导航菜单结构

```
侧边栏导航
├── 📋 工作计划
│   ├── 计划列表
│   ├── 新建日报
│   ├── 新建周报
│   ├── 新建月报
│   └── 日历视图
├── ✅ 成果管理
│   ├── 成果列表
│   └── 待提交成果
├── 🔍 查询统计
│   ├── 个人统计
│   └── 团队统计（领导可见）
├── 📝 审批中心（领导可见）
│   ├── 待审批
│   └── 审批历史
├── 🔧 系统管理（管理员可见）
│   ├── 用户管理
│   ├── 部门管理
│   ├── 审批链配置
│   └── 分类管理
└── ⚙️ 个人中心
```

---

## 八、安全设计

### 8.1 认证流程

```
┌────────┐         ┌────────┐          ┌────────┐
│  前端   │         │  后端   │          │ Redis  │
└───┬────┘         └───┬────┘          └───┬────┘
    │   POST /login    │                   │
    │  (username,pwd)  │                   │
    │ ────────────────>│                   │
    │                  │ 验证用户名密码      │
    │                  │ 生成 JWT Token     │
    │  返回 Token      │                   │
    │ <────────────────│                   │
    │                  │                   │
    │  后续请求 +Header│                   │
    │  Authorization   │                   │
    │ ────────────────>│                   │
    │                  │ 校验 Token 签名    │
    │                  │ 检查是否在黑名单    │
    │                  │ ─────────────────>│
    │                  │ (黑名单检查)       │
    │                  │ <─────────────────│
    │                  │ 解析用户信息        │
    │  返回业务数据     │                   │
    │ <────────────────│                   │
```

### 8.2 权限控制

| 维度 | 实现方式 |
|------|----------|
| **接口权限** | Spring Security + `@PreAuthorize` 注解，基于角色控制 |
| **数据权限** | Service 层根据当前用户角色自动过滤：员工只能查自己的数据，领导查下属数据 |
| **Token 管理** | JWT + Redis 黑名单，登出时 Token 加入黑名单，有效期至 Token 过期 |

### 8.3 数据权限过滤逻辑

```java
// 伪代码示例
if (currentUser.isEmployee()) {
    // 员工：只能查自己的
    query.eq("user_id", currentUser.getId());
} else if (currentUser.isLeader()) {
    // 领导：查自己 + 所有下属（含间接下属）
    List<Long> subordinateIds = userService.getAllSubordinateIds(currentUser.getId());
    query.in("user_id", subordinateIds);
}
// 管理员：不限制（或根据上下文限制）
```

---

## 九、关键设计决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| 后端架构 | 单体应用 | 团队规模 30 人，单体足够；模块化包结构保证代码清晰 |
| 审批链 | 独立配置表 | 支持灵活扩展审批级数，不依赖组织架构硬编码 |
| 通知机制 | WebSocket 实时推送 | 审批通知需要实时触达，减少轮询开销 |
| 日报→周报汇总 | 软关联（手动选择） | V1.0 简单实用，避免自动汇总不准确带来的体验问题 |
| 计划与成果关系 | 一对一 | 每条计划对应一个成果，逻辑清晰，一次计划一次验收 |
| 部门设计 | 预留多部门 | 父级 ID 自引用，支持树形部门结构 |
| 审批目标关联 | 多态关联（target_id + target_type） | 审批记录同时支持计划和成果，避免两张审批表 |
| 文件存储 | 本地文件系统 | V1.0 简单部署，预留接口方便切换至 OSS/MinIO |
| Token 黑名单 | Redis | 登出后 Token 即时失效，防止泄漏后继续使用 |

---

## 十、部署架构（Docker Compose）

```
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    volumes:
      - mysql_data:/var/lib/mysql
    environment:
      MYSQL_ROOT_PASSWORD: xxx
      MYSQL_DATABASE: plan_system
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/plan_system
      SPRING_REDIS_HOST: redis
    ports:
      - "8080:8080"
    volumes:
      - upload_files:/app/uploads

  frontend:
    build: ./frontend
    depends_on:
      - backend
    ports:
      - "80:80"

volumes:
  mysql_data:
  upload_files:
```

---

## 十一、已确认的设计细节

| # | 事项 | 确认结果 |
|---|------|----------|
| 1 | 审批链的默认规则 | 未配置审批链时，回退到**默认规则**：直属领导 → 部门负责人 |
| 2 | 转审的目标人范围 | **限制在同部门**内转审 |
| 3 | 通知保留策略 | **永久保留**，用户可手动删除，不做自动清理 |
| 4 | 周/月的边界定义 | 周一为每周第一天，月为**自然月** |
| 5 | 并发审批（同级多审批人） | **全部审批人通过**才算该级通过 |
| 6 | 计划是否强制要求提交成果 | **否**，计划通过后不强制提交成果

---

> **文档版本**：v1.0  
> **创建日期**：2026-06-20  
> **对应需求文档**：`doc/proposal.md`  
> **下一步**：详细设计文档 `doc/detailed-design.md`
