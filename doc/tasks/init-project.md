# 模块：项目初始化

> **优先级**：P0（最先执行）  
> **依赖**：无  
> **说明**：搭建前后端项目脚手架、Docker 容器环境、项目目录结构

---

## 一、Spring Boot 后端项目

- [x] **T01** 创建 Spring Boot 项目（Maven），引入核心依赖 ✅
  - `spring-boot-starter-web`
  - `spring-boot-starter-security`
  - `mybatis-plus-spring-boot3-starter`
  - `mysql-connector-j`
  - `jjwt`（JWT）
  - `spring-boot-starter-data-redis`
  - `spring-boot-starter-websocket`
  - `knife4j-openapi3-jakarta-starter`（API 文档）
  - `lombok`
  - `spring-boot-starter-validation`
- [x] **T02** 配置 `application.yml` ✅
  - 数据源（MySQL 连接）
  - Redis 连接
  - 文件上传路径和大小限制
  - JWT 密钥和过期时间（8小时）
  - 服务器端口 8080
- [x] **T03** 创建后端包结构（按概要设计的分层结构） ✅

---

## 二、Vue 3 前端项目

- [x] **T04** 创建 Vue 3 项目（Vite），引入核心依赖 ✅
  - `vue-router`
  - `pinia`
  - `element-plus`
  - `axios`
  - `@fullcalendar/vue3`（日历）
  - `echarts` + `vue-echarts`（图表）
  - `sockjs-client` + `@stomp/stompjs`（WebSocket）
  - `quill` + `@vueup/vue-quill`（富文本编辑器）
- [x] **T05** 创建前端目录结构（按概要设计的结构） ✅
- [x] **T06** 配置前端基础文件 ✅
  - 路由表骨架（router/index.js） - 18 条路由
  - Pinia store 入口（user/notification/app）
  - Axios 实例骨架（api/request.js + 8 个模块 API）
  - App.vue 基础布局

---

## 三、Docker 环境

- [x] **T07** 编写 `docker-compose.yml` ✅
  - MySQL 8.0 服务
  - Redis 7 服务
  - 后端服务（挂载上传目录）
  - 前端服务（Nginx）
- [ ] **T08** 验证 Docker Compose 能正常启动所有服务（需数据库模块完成后验证）
- [ ] **T09** 前后端联调验证（确保 API 能调通）（需认证模块完成后验证）

---

## 四、开发规范

- [x] **T10** 后端统一代码风格（Lombok、checkstyle.xml、Checkstyle 0 violations） ✅
- [x] **T11** 前端统一代码风格（ESLint + Prettier 配置） ✅
