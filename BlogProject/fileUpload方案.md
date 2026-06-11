# 博客系统图片/附件上传方案

## 核心原则

从程序员角度看，博客系统里的图片和附件不要直接“塞进文章内容”，而是应该当成独立资源管理。

文章只保存资源地址或资源 ID，例如：

```markdown
![示例图](/uploads/2026/06/11/abc.png)
```

或：

```markdown
![封面图](attachment://12345)
```

这样做的好处是：

- 文章内容和文件资源解耦
- 方便做权限控制、CDN 加速、文件清理
- 方便后续从本地存储迁移到 OSS/S3
- 方便统计图片/附件引用关系

## 方案一：本地文件存储 + 数据库记录

适合个人博客、小型内部系统、单机部署。

### 架构流程

```text
前端上传图片/附件
   ↓
后端接口接收 multipart/form-data
   ↓
保存到服务器磁盘
   ↓
数据库记录文件信息
   ↓
文章内容中引用文件 URL
```

### 文件保存示例

```text
/uploads/2026/06/11/uuid.png
/uploads/2026/06/11/uuid.pdf
```

### 数据库表设计示例

`article`：

| 字段 | 说明 |
| --- | --- |
| `id` | 文章 ID |
| `title` | 文章标题 |
| `content` | 文章内容 |
| `status` | 文章状态 |
| `created_at` | 创建时间 |

`attachment`：

| 字段 | 说明 |
| --- | --- |
| `id` | 附件 ID |
| `article_id` | 关联文章 ID |
| `original_name` | 原始文件名 |
| `file_name` | 服务端保存文件名 |
| `file_path` | 文件磁盘路径 |
| `file_url` | 文件访问 URL |
| `mime_type` | 文件 MIME 类型 |
| `file_size` | 文件大小 |
| `type` | 资源类型：`image` / `file` |
| `created_at` | 创建时间 |

### 文章内容引用示例

```markdown
这是一张图片：

![示例图](/uploads/2026/06/11/abc.png)
```

### 优点

- 实现简单
- 成本低
- 适合小项目
- 备份时数据库和 `uploads` 目录一起备份即可

### 缺点

- 多服务器部署麻烦
- 文件容易占满服务器磁盘
- CDN 加速不方便
- 文件迁移成本高

### 技术组合

```text
Spring Boot / Node.js / Django / Laravel
+
Nginx 静态文件服务
+
MySQL / PostgreSQL
```

### 适合场景

- 个人博客
- 后台管理系统
- 内网知识库
- 单机部署的小型内容系统

## 方案二：对象存储 OSS/S3 + CDN

这是当前最主流、最推荐的生产方案。

### 架构流程

```text
前端选择文件
   ↓
后端生成上传凭证 / 预签名 URL
   ↓
前端直传对象存储
   ↓
对象存储返回文件 key
   ↓
后端保存文件元数据
   ↓
文章引用 CDN URL
```

### 文件最终访问地址示例

```text
https://cdn.example.com/blog/images/2026/06/uuid.png
```

### 数据库表设计建议

不要只存完整 URL，最好同时保存 `bucket` 和 `object_key`，便于后续换 CDN 域名、做权限控制或迁移存储。

`attachment`：

| 字段 | 说明 |
| --- | --- |
| `id` | 附件 ID |
| `article_id` | 关联文章 ID |
| `bucket` | 存储桶名称 |
| `object_key` | 对象存储 key |
| `original_name` | 原始文件名 |
| `mime_type` | 文件 MIME 类型 |
| `file_size` | 文件大小 |
| `file_hash` | 文件 hash，用于去重或校验 |
| `type` | 资源类型：`image` / `file` |
| `url` | CDN 或公开访问 URL |
| `created_at` | 创建时间 |

### 文章内容引用方式

直接引用 CDN 地址：

```markdown
![封面图](https://cdn.example.com/blog/images/2026/06/abc.png)
```

或引用资源 ID，由后端渲染时转换：

```markdown
![封面图](attachment://12345)
```

### 推荐上传流程

1. 前端请求后端：我要上传 `xxx.png`。
2. 后端校验登录、文件类型、文件大小。
3. 后端生成 presigned URL。
4. 前端直接 `PUT` 到 OSS/S3。
5. 前端通知后端上传完成。
6. 后端保存 `attachment` 记录。
7. 文章保存时关联 `attachment`。

### 优点

- 适合生产环境
- 文件不占应用服务器磁盘
- 支持 CDN 加速
- 支持大文件上传
- 支持多服务器部署
- 扩容方便

### 缺点

- 需要接入云厂商
- 有存储和流量成本
- 权限、回调、清理逻辑要设计好

### 主流技术

对象存储：

- AWS S3
- 阿里云 OSS
- 腾讯云 COS
- 七牛云 Kodo
- MinIO

后端：

- Spring Boot + AWS S3 SDK / Aliyun OSS SDK
- Node.js + multer + S3 SDK
- Go + minio-go
- Python + boto3

前端：

- Vue / React + direct upload

### 适合场景

- 正式博客平台
- SaaS
- 多用户内容系统
- 企业官网 CMS
- 多服务器部署系统

## 方案三：内容编辑器资源管理 + 临时资源转正机制

这个方案更偏完整 CMS 设计，适合用户边写文章边上传图片，最后可能保存、发布或放弃的场景。

### 核心问题

用户创建文章时上传了图片，但最后没有保存文章，这些图片怎么办？

如果没有临时资源机制，就会出现大量无主图片或无主附件。

### 推荐设计

```text
上传文件时先进入临时资源区
   ↓
文章保存成功后，把资源绑定到 article_id
   ↓
定时任务清理长期未绑定的临时文件
```

### 数据库表设计

`attachment`：

| 字段 | 说明 |
| --- | --- |
| `id` | 附件 ID |
| `article_id` | 关联文章 ID，未绑定时为空 |
| `upload_token` | 当前编辑会话 ID |
| `object_key` | 对象存储 key 或本地文件 key |
| `original_name` | 原始文件名 |
| `mime_type` | 文件 MIME 类型 |
| `file_size` | 文件大小 |
| `status` | 资源状态：`temp` / `bound` / `deleted` |
| `created_by` | 上传用户 ID |
| `created_at` | 创建时间 |
| `bound_at` | 绑定时间 |

### 创建文章流程

1. 前端打开创建文章页面。
2. 后端生成 `editor_session_id`。
3. 用户上传图片，`attachment.article_id = null`，`status = temp`。
4. Markdown / 富文本内容中插入图片地址。
5. 用户点击保存。
6. 后端创建 `article`。
7. 后端扫描文章内容里引用的附件。
8. 把这些附件绑定到 `article_id`，`status = bound`。
9. 未引用的临时附件后续由定时任务自动清理。

### 文章保存时的引用提取

Markdown：

```markdown
![xxx](https://cdn.example.com/blog/tmp/a.png)
```

HTML：

```html
<img src="https://cdn.example.com/blog/tmp/a.png">
<a href="https://cdn.example.com/blog/files/b.pdf">下载附件</a>
```

### 清理策略

每天凌晨清理：

```sql
status = 'temp'
AND created_at < 当前时间 - 24 小时
```

如果使用对象存储，清理任务需要同时删除对象存储里的真实文件。

### 优点

- 适合真实编辑器场景
- 避免大量无主图片
- 支持草稿、预览、发布
- 支持附件引用统计
- 方便做权限控制和资源管理

### 缺点

- 实现复杂度更高
- 需要定时任务
- 需要处理文章修改时的附件增删关系

### 适合技术组合

前端编辑器：

- Editor.md
- Toast UI Editor
- Vditor
- WangEditor
- TinyMCE
- TipTap

后端：

- Spring Boot
- NestJS
- Django
- Laravel

存储：

- S3
- OSS
- MinIO

任务：

- Spring Scheduler
- Quartz
- Celery
- BullMQ

## 推荐落地方案

生产系统推荐使用：方案二 + 方案三结合。

也就是：

- 对象存储负责文件存储
- 临时资源机制负责编辑过程
- 数据库负责资源元数据和文章关联
- CDN 负责公开访问加速
- 定时任务负责清理未绑定资源

最终设计可以是：

| 模块 | 职责 |
| --- | --- |
| `article` 表 | 保存文章主体 |
| `attachment` 表 | 保存图片/附件资源元数据 |
| `article_attachment` 表 | 保存文章和附件的引用关系 |
| Object Storage | 保存真实文件 |
| CDN | 对外提供加速访问 |
| 定时任务 | 清理未绑定临时资源 |

### 小项目建议

如果只是个人博客或单机系统，可以先用方案一：

```text
Spring Boot 上传接口
+
本地 uploads 目录
+
MySQL attachment 表
+
Nginx 静态文件访问
```

后续业务变大后，再迁移到 OSS/S3。

### 正式项目建议

正式博客平台、CMS、多用户系统建议一开始就采用：

```text
OSS/S3/MinIO
+
CDN
+
attachment 元数据表
+
临时资源转正机制
+
定时清理任务
```

这样扩展性、性能和数据管理都会更稳定。
