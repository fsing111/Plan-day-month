# 模块：认证授权（auth）

> **优先级**：P0（所有业务模块依赖认证）  
> **依赖**：`common`（需要 Result、UserContext、ErrorCode）  
> **说明**：用户登录、JWT Token 管理、Spring Security 配置

---

## 一、后端 - JWT 工具

- [ ] **T01** [后端] 创建 `JwtUtils` JWT 工具类
  - `generateToken(userId, username, role)` → 生成 Token（8小时过期）
  - `parseToken(token)` → 解析 Token 返回 Claims
  - `validateToken(token)` → 校验 Token 有效性
  - Token 中存储：userId、username、role

## 二、后端 - Spring Security 配置

- [ ] **T02** [后端] 创建 `SecurityConfig` 安全配置类
  - 放行 `/api/v1/auth/login`（登录接口）
  - 放行 `/ws/**`（WebSocket 端点）
  - 放行 Swagger/Knife4j 文档路径
  - 其他所有接口需要认证
  - 配置无状态会话（SessionCreationPolicy.STATELESS）
  - 注册 JWT 过滤器
- [ ] **T03** [后端] 创建 `JwtAuthFilter` JWT 认证过滤器
  - 从 Header `Authorization: Bearer <token>` 提取 Token
  - 校验 Token，解析用户信息
  - 设置 `UserContext`（userId、role、deptId）
  - 设置 Spring Security 上下文
- [ ] **T04** [后端] 创建 `UserDetailServiceImpl` 实现 `UserDetailsService`
  - `loadUserByUsername(username)` 从数据库查用户
  - 返回 Spring Security 的 `UserDetails`

## 三、后端 - 认证接口

- [ ] **T05** [后端] 实现 `POST /api/v1/auth/login` 登录接口
  - 接收 `username` + `password`
  - 用 BCrypt 校验密码
  - 校验用户是否启用（enabled=1）
  - 返回 JWT Token + 用户信息（id/username/realName/role/deptId/deptName/leaderId/leaderName）
- [ ] **T06** [后端] 实现 `POST /api/v1/auth/logout` 登出接口
  - 将当前 Token 加入 Redis 黑名单（key=`blacklist:token:<token>`，过期时间=Token剩余有效期）
- [ ] **T07** [后端] 实现 `GET /api/v1/auth/me` 获取当前用户接口
  - 从 `UserContext` 取 userId，查数据库返回完整用户信息
- [ ] **T08** [后端] 在 `JwtAuthFilter` 中增加 Redis 黑名单校验
  - 每次请求检查 Token 是否在黑名单中

## 四、前端 - 登录与认证

- [ ] **T09** [前端] 实现登录页 `views/login/LoginView.vue`
  - 用户名/密码输入框
  - 登录按钮 + 加载状态
  - 错误提示（用户名或密码错误/账号已禁用）
  - 登录成功 → 存储 Token 到 localStorage → 跳转首页
- [ ] **T10** [前端] 封装 Axios 实例 `api/request.js`
  - 请求拦截器：自动注入 `Authorization: Bearer <token>`
  - 响应拦截器：统一处理 401 → 清除 Token → 跳转登录页
  - 响应拦截器：统一处理其他错误 → toast 提示
- [ ] **T11** [前端] 配置路由导航守卫 `router/index.js`
  - 未登录 → 只能访问 `/login`，其他自动跳转登录
  - 已登录 → 访问 `/login` 自动跳转首页
- [ ] **T12** [前端] 创建 Pinia 用户 Store `stores/user.js`
  - `token`、`userInfo` 状态
  - `login()` action
  - `logout()` action（调后端登出 + 清本地）
  - `fetchUserInfo()` action

## 五、前端 - 布局框架

- [ ] **T13** [前端] 实现主布局组件 `components/layout/AppLayout.vue`
  - 左侧：Sidebar 侧边栏
  - 顶部：Topbar 顶栏
  - 中间：`<router-view>` 内容区
- [ ] **T14** [前端] 实现侧边栏 `components/layout/Sidebar.vue`
  - 根据用户 role 动态显示菜单（员工不显示"审批中心"和"系统管理"）
  - 菜单折叠/展开
  - 当前路由高亮
- [ ] **T15** [前端] 实现顶栏 `components/layout/Topbar.vue`
  - 面包屑导航
  - 通知铃铛入口（组件后续实现）
  - 用户头像 + 下拉菜单（个人中心、退出登录）
