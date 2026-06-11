CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  username VARCHAR(50) NOT NULL COMMENT '登录用户名，全局唯一',
  email VARCHAR(100) NULL COMMENT '邮箱，全局唯一',
  phone VARCHAR(30) NULL COMMENT '手机号，全局唯一',
  password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  nickname VARCHAR(50) NOT NULL COMMENT '前台展示昵称',
  avatar_url VARCHAR(500) NULL COMMENT '用户头像地址',
  bio VARCHAR(500) NULL COMMENT '用户简介',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '用户状态：active、disabled',
  last_login_at DATETIME NULL COMMENT '最近登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_phone (phone),
  KEY idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
  code VARCHAR(50) NOT NULL COMMENT '角色编码，如 USER、AUTHOR、ADMIN',
  name VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(255) NULL COMMENT '角色说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS user_roles (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
  user_id BIGINT NOT NULL COMMENT '用户 ID',
  role_id BIGINT NOT NULL COMMENT '角色 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_roles_user_role (user_id, role_id),
  KEY idx_user_roles_role_id (role_id),
  CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS articles (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章 ID',
  author_id BIGINT NOT NULL COMMENT '作者用户 ID',
  title VARCHAR(200) NOT NULL COMMENT '文章标题',
  slug VARCHAR(220) NULL COMMENT 'URL 友好标识',
  summary VARCHAR(500) NULL COMMENT '文章摘要',
  content_md LONGTEXT NOT NULL COMMENT 'Markdown 原文',
  content_html LONGTEXT NULL COMMENT '渲染后的 HTML',
  cover_url VARCHAR(500) NULL COMMENT '封面图地址',
  status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '文章状态：draft、published、offline',
  view_count BIGINT NOT NULL DEFAULT 0 COMMENT '阅读量',
  comment_count BIGINT NOT NULL DEFAULT 0 COMMENT '评论数',
  published_at DATETIME NULL COMMENT '发布时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_articles_slug (slug),
  KEY idx_articles_author_id (author_id),
  KEY idx_articles_status_published_at (status, published_at),
  KEY idx_articles_created_at (created_at),
  FULLTEXT KEY ft_articles_search (title, summary, content_md),
  CONSTRAINT fk_articles_author_id FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  slug VARCHAR(80) NOT NULL COMMENT '分类 URL 标识',
  description VARCHAR(255) NULL COMMENT '分类描述',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active、disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name),
  UNIQUE KEY uk_categories_slug (slug),
  KEY idx_categories_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类表';

CREATE TABLE IF NOT EXISTS article_categories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
  article_id BIGINT NOT NULL COMMENT '文章 ID',
  category_id BIGINT NOT NULL COMMENT '分类 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_article_categories_article_category (article_id, category_id),
  KEY idx_article_categories_category_id (category_id),
  CONSTRAINT fk_article_categories_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_article_categories_category_id FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章分类关联表';

CREATE TABLE IF NOT EXISTS tags (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签 ID',
  name VARCHAR(50) NOT NULL COMMENT '标签名称',
  slug VARCHAR(80) NOT NULL COMMENT '标签 URL 标识',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active、disabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tags_name (name),
  UNIQUE KEY uk_tags_slug (slug),
  KEY idx_tags_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

CREATE TABLE IF NOT EXISTS article_tags (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
  article_id BIGINT NOT NULL COMMENT '文章 ID',
  tag_id BIGINT NOT NULL COMMENT '标签 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_article_tags_article_tag (article_id, tag_id),
  KEY idx_article_tags_tag_id (tag_id),
  CONSTRAINT fk_article_tags_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_article_tags_tag_id FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签关联表';

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论 ID',
  article_id BIGINT NOT NULL COMMENT '文章 ID',
  user_id BIGINT NOT NULL COMMENT '评论用户 ID',
  parent_id BIGINT NULL COMMENT '父评论 ID，用于评论回复',
  content VARCHAR(1000) NOT NULL COMMENT '评论内容',
  status VARCHAR(20) NOT NULL DEFAULT 'visible' COMMENT '评论状态：visible、hidden',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_at DATETIME NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  KEY idx_comments_article_status_created (article_id, status, created_at),
  KEY idx_comments_user_id (user_id),
  KEY idx_comments_parent_id (parent_id),
  CONSTRAINT fk_comments_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_comments_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_comments_parent_id FOREIGN KEY (parent_id) REFERENCES comments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章评论表';

CREATE TABLE IF NOT EXISTS attachments (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件 ID',
  article_id BIGINT NULL COMMENT '绑定文章 ID，临时资源为空',
  upload_token VARCHAR(80) NULL COMMENT '编辑会话 ID',
  original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  file_name VARCHAR(255) NOT NULL COMMENT '服务端保存文件名',
  file_path VARCHAR(1000) NOT NULL COMMENT '本地文件路径',
  file_url VARCHAR(1000) NOT NULL COMMENT '访问 URL',
  object_key VARCHAR(500) NOT NULL COMMENT '本地文件 key，后续可迁移到 OSS/S3 object_key',
  mime_type VARCHAR(120) NOT NULL COMMENT '文件 MIME 类型',
  file_size BIGINT NOT NULL COMMENT '文件大小',
  file_hash VARCHAR(64) NOT NULL COMMENT '文件 SHA-256',
  type VARCHAR(20) NOT NULL COMMENT '资源类型：image、file',
  status VARCHAR(20) NOT NULL DEFAULT 'temp' COMMENT '资源状态：temp、bound、deleted',
  created_by BIGINT NOT NULL COMMENT '上传用户 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  bound_at DATETIME NULL COMMENT '绑定时间',
  deleted_at DATETIME NULL COMMENT '删除时间',
  PRIMARY KEY (id),
  KEY idx_attachments_article_id (article_id),
  KEY idx_attachments_upload_token (upload_token),
  KEY idx_attachments_status_created (status, created_at),
  KEY idx_attachments_created_by (created_by),
  CONSTRAINT fk_attachments_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_attachments_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图片和附件资源表';

CREATE TABLE IF NOT EXISTS article_attachments (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联 ID',
  article_id BIGINT NOT NULL COMMENT '文章 ID',
  attachment_id BIGINT NOT NULL COMMENT '附件 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_article_attachments_article_attachment (article_id, attachment_id),
  KEY idx_article_attachments_attachment_id (attachment_id),
  CONSTRAINT fk_article_attachments_article_id FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_article_attachments_attachment_id FOREIGN KEY (attachment_id) REFERENCES attachments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章附件引用关系表';
