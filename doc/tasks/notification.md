# 模块：消息通知（notification）

> **优先级**：P1  
> **依赖**：`common`、`auth`  
> **说明**：通知的创建、查询、已读标记；WebSocket 实时推送

---

## 一、后端 - 实体与数据访问

- [ ] **T01** [后端] 创建 `Notification` 实体类（映射 `notification` 表）
- [ ] **T02** [后端] 创建 `NotificationMapper`（继承 BaseMapper）
  - 自定义查询：按 receiver_id + is_read 查未读数

## 二、后端 - 通知服务

- [ ] **T03** [后端] 创建 `NotificationService`
  - `create(receiverId, type, title, content, relatedId, relatedType)` → 创建通知记录
  - `markAsRead(notificationId)` → 标记单条已读
  - `markAllAsRead(userId)` → 标记全部已读
  - `getUnreadCount(userId)` → 获取未读数
  - `deleteById(notificationId)` → 手动删除通知

## 三、后端 - WebSocket 配置

- [ ] **T04** [后端] 配置 Spring WebSocket + STOMP
  - 创建 `WebSocketConfig` 实现 `WebSocketMessageBrokerConfigurer`
  - 端点：`/ws`（SockJS 兼容）
  - 消息代理：`/queue`（点对点）、`/topic`（广播）
  - 设置认证拦截器（从请求参数或 Header 取 Token 校验）
- [ ] **T05** [后端] 创建 `NotificationPushService`
  - 封装 `SimpMessagingTemplate`
  - `pushToUser(userId, notification)` → 发送到 `/queue/notifications`
  - 被 NotificationService.create() 调用 → 每创建一条通知就实时推送

## 四、后端 - 集成到业务模块

- [ ] **T06** [后端] 在 plan 提交时调用通知
  - `PlanService.submit()` → 创建 `NEW_APPROVAL_PLAN` 通知给审批人
- [ ] **T07** [后端] 在 achievement 提交时调用通知
  - `AchievementService.submit()` → 创建 `NEW_APPROVAL_ACHV` 通知给审批人
- [ ] **T08** [后端] 在审批操作时调用通知
  - 通过（终审）→ `PLAN_APPROVED` / `ACHV_APPROVED` 通知提交人
  - 驳回 → `PLAN_REJECTED` / `ACHV_REJECTED` 通知提交人
  - 转审 → `NEW_APPROVAL_*` 通知新审批人
  - 进入下一级 → `NEW_APPROVAL_*` 通知下一级审批人

## 五、后端 - 通知接口

- [ ] **T09** [后端] 实现 `GET /api/v1/notifications` 通知列表
  - 查当前用户的全部通知，按时间倒序
  - 支持分页
- [ ] **T10** [后端] 实现 `GET /api/v1/notifications/unread-count` 未读数量
- [ ] **T11** [后端] 实现 `PUT /api/v1/notifications/{id}/read` 标记已读
- [ ] **T12** [后端] 实现 `PUT /api/v1/notifications/read-all` 全部已读
- [ ] **T13** [后端] 实现 `DELETE /api/v1/notifications/{id}` 删除通知

## 六、前端 - WebSocket 连接

- [ ] **T14** [前端] 封装 WebSocket 连接管理 `utils/websocket.js`
  - 登录后自动建立 SockJS + STOMP 连接
  - 订阅 `/user/queue/notifications`
  - 收到消息 → 写入 Pinia notification store → Toast 提示
  - 断开重连机制
  - 登出时断开连接

## 七、前端 - 通知铃铛

- [ ] **T15** [前端] 实现通知铃铛组件 `components/common/NotificationBell.vue`
  - 位于 Topbar 右上角（铃铛图标）
  - 未读红点数字（从 Pinia store 取 unreadCount）
  - 点击弹出下拉列表（最近 5 条通知）
  - 每条通知可点击跳转（根据 relatedType+relatedId 跳转到计划详情/成果详情）
  - 底部"查看全部"链接 → 跳转通知列表页
- [ ] **T16** [前端] 创建 Pinia 通知 Store `stores/notification.js`
  - `unreadCount`
  - `fetchUnreadCount()` action
  - `addNotification(notification)` → WebSocket 收到消息时调用
  - `incrementUnread()` / `resetUnread()`

## 八、前端 - 通知列表

- [ ] **T17** [前端] 实现通知列表页 `views/notification/NotificationList.vue`
  - 全部通知列表（分页）
  - 未读通知高亮显示
  - 每条可点击跳转 + 标记已读
  - 全部已读按钮
  - 删除按钮
- [ ] **T18** [前端] 封装通知相关 API `api/notification.js`
