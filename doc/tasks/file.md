# 模块：附件管理（file）

> **优先级**：P2  
> **依赖**：`common`、`auth`、`achievement`（附件与成果关联）  
> **说明**：附件上传、下载、预览、删除

---

## 一、后端 - 实体与数据访问

- [ ] **T01** [后端] 创建 `Attachment` 实体类（映射 `attachment` 表）
- [ ] **T02** [后端] 创建 `AttachmentMapper`（继承 BaseMapper）

## 二、后端 - 文件存储

- [ ] **T03** [后端] 配置文件存储路径
  - `application.yml` 中配置 `file.upload-dir=./uploads`
  - 存储规则：`{uploadDir}/{yyyy}/{MM}/{uuid}.{ext}`
  - Docker 部署时挂载该目录到宿主机
- [ ] **T04** [后端] 创建 `FileService`
  - `upload(file)` → 校验文件大小 ≤ 10MB、格式白名单、保存文件、写入 attachment 记录、返回 attachmentId
  - `download(attachmentId)` → 查记录、返回文件输入流
  - `preview(attachmentId)` → 图片直接返回文件流，其他返回 Content-Type 信息
  - `delete(attachmentId)` → 校验权限（上传人或管理员）、删除文件和记录

## 三、后端 - 文件接口

- [ ] **T05** [后端] 实现 `POST /api/v1/files/upload` 文件上传
  - 接收 `multipart/form-data`，字段名 `file`
  - 校验：大小 ≤ 10MB、格式白名单（jpg/png/gif/pdf/doc/docx/xlsx/txt/zip）
  - 返回 attachmentId
- [ ] **T06** [后端] 实现 `GET /api/v1/files/{id}/download` 文件下载
  - 设置 `Content-Disposition: attachment; filename="原始文件名"`
- [ ] **T07** [后端] 实现 `GET /api/v1/files/{id}/preview` 文件预览
  - 图片：设置 `Content-Type: image/*`，直接返回文件流
  - 其他：返回 JSON `{ fileName, fileSize, fileType, message: "不支持在线预览，请下载" }`
- [ ] **T08** [后端] 实现 `DELETE /api/v1/files/{id}` 删除附件
  - 仅上传者或管理员可删除
  - 如果附件已关联成果且成果已提交/审批中，不可删除

## 四、前端 - 上传组件

- [ ] **T09** [前端] 实现附件上传组件 `components/common/FileUpload.vue`
  - 拖拽上传 / 点击上传
  - 多文件支持（最多 5 个同时上传）
  - 上传进度条
  - 文件大小超限提示
  - 上传成功显示文件列表（文件名、大小、删除按钮）
  - 已上传文件 ID 列表通过 `v-model` 双向绑定
- [ ] **T10** [前端] 实现附件预览组件 `components/common/FilePreview.vue`
  - 图片：使用 Element Plus Image 组件预览
  - 其他文件：显示文件图标 + 文件名 + 下载按钮
  - 文件大小格式化显示（KB/MB）

## 五、前端 - API

- [ ] **T11** [前端] 封装文件相关 API `api/file.js`
  - `uploadFile(file, onProgress)` → FormData 上传，支持进度回调
  - `downloadFile(id)` → 触发浏览器下载
  - `getPreviewUrl(id)` → 返回预览 URL
  - `deleteFile(id)`
