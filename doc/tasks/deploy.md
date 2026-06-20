# 模块：容器化部署（deploy）

> **优先级**：P3（最后执行）  
> **依赖**：所有业务模块开发完成  
> **说明**：Docker 镜像构建、Nginx 配置、Docker Compose 编排、一键部署

---

## 一、后端 Docker

- [ ] **T01** 编写后端 `Dockerfile`
  - 基于 `openjdk:17-slim` 或 `eclipse-temurin:17`
  - 多阶段构建（Maven 构建 + JRE 运行）
  - 暴露 8080 端口
  - 挂载上传目录 `/app/uploads`
- [ ] **T02** 配置后端环境变量（通过 Docker Compose 注入）
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_REDIS_HOST`
  - `JWT_SECRET`
  - `FILE_UPLOAD_DIR`

## 二、前端 Docker

- [ ] **T03** 编写前端 `Dockerfile`
  - 多阶段构建：Node 构建 + Nginx 运行
  - 构建阶段：`npm install && npm run build`
  - 运行阶段：基于 `nginx:alpine`，复制 `dist/` 到 `/usr/share/nginx/html`
- [ ] **T04** 编写 `nginx.conf` 前端 Nginx 配置
  - 静态资源服务
  - `/api/` 反向代理到后端 `http://backend:8080`
  - `/ws/` WebSocket 代理（支持 Upgrade）
  - SPA 路由 fallback（`try_files $uri /index.html`）
  - gzip 压缩

## 三、Docker Compose 编排

- [ ] **T05** 编写完整 `docker-compose.yml`
  - `mysql` 服务：初始化脚本挂载、数据持久化、健康检查
  - `redis` 服务：数据持久化
  - `backend` 服务：依赖 mysql+redis 健康后启动、环境变量、上传目录挂载
  - `frontend` 服务：依赖 backend、端口映射 80:80
  - 自定义网络
- [ ] **T06** 编写 `.env` 环境变量文件
  - `MYSQL_ROOT_PASSWORD`
  - `MYSQL_DATABASE`
  - `JWT_SECRET`
- [ ] **T07** 编写 `init.sql` 初始化脚本
  - 建表 DDL + 初始化数据
  - 挂载到 MySQL 容器的 `/docker-entrypoint-initdb.d/`

## 四、启动脚本

- [ ] **T08** 编写 `start.sh` 一键启动脚本
  - 检查 Docker 环境
  - `docker-compose up -d`
  - 等待服务就绪
  - 打印访问地址
- [ ] **T09** 编写 `stop.sh` 停止脚本
  - `docker-compose down`
- [ ] **T10** 编写部署说明 `README.md`（项目根目录）
  - 环境要求
  - 快速启动步骤
  - 默认管理员账号
  - 访问地址
