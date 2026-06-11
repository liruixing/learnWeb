INSERT INTO roles (code, name, description)
VALUES ('USER', '普通用户', '可浏览文章、发表评论')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO roles (code, name, description)
VALUES ('AUTHOR', '作者', '可创建、编辑、发布自己的文章')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO roles (code, name, description)
VALUES ('ADMIN', '管理员', '可管理全站用户、文章、分类、标签和评论')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;
