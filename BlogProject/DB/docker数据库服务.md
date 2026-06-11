# Docker 数据库服务说明

## 1. Docker 安装 MySQL 镜像

本项目使用 MySQL 8.4，推荐直接用 Docker Compose 启动：

```bash
docker compose up -d mysql
```

如果还没有镜像，可以先手动拉取：

```bash
docker pull mysql:8.4
```

常用检查命令：

```bash
docker compose ps mysql
docker logs learn-mysql
```

当前 `docker-compose.yml` 中的数据库配置：

```text
镜像: mysql:8.4
容器名: learn-mysql
端口: 3306:3306
数据库: blog_mvp
root 密码: root
数据卷: learn-mysql-data
```

## 2. 项目启动与数据库创建流程

项目启动入口是：

```text
src/main/kotlin/com/dennis/learn/LearnApplication.kt
```

启动后，Spring Boot 会读取：

```text
src/main/resources/application.properties
```

重点数据库配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blog_mvp?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.sql.init.mode=always
```

启动流程简述：

1. 先启动 Docker MySQL 容器，容器监听本机 `3306` 端口。
2. IDEA 运行 `LearnApplication`，Spring Boot 创建数据库连接池。
3. JDBC 连接到 `localhost:3306/blog_mvp`。
4. `createDatabaseIfNotExist=true` 会在数据库不存在时自动创建 `blog_mvp`。
5. `spring.sql.init.mode=always` 会让 Spring Boot 执行资源目录下的初始化 SQL。
6. `schema.sql` 创建基础表：`users`、`roles`、`user_roles`。
7. `data.sql` 初始化基础角色：`USER`、`AUTHOR`、`ADMIN`。
8. 应用启动完成后，Swagger 可访问：

```text
http://localhost:8080/swagger-ui.html
```

如果接口返回 500，优先检查 MySQL 容器是否运行、`3306` 是否监听、账号密码是否和 `application.properties` 一致。

## 3. schema.sql 和 data.sql 何时执行

`schema.sql` 和 `data.sql` 不需要手动执行。它们由 Spring Boot 在应用启动时自动调用。

触发条件是 `application.properties` 中开启了：

```properties
spring.sql.init.mode=always
```

执行时机：

1. IDEA 点击运行 `LearnApplication`。
2. Spring Boot 创建 `DataSource`，连接 MySQL。
3. 数据源初始化阶段，Spring Boot 查找 `src/main/resources/schema.sql` 和 `src/main/resources/data.sql`。
4. 先执行 `schema.sql`，用于建表。
5. 再执行 `data.sql`，用于插入初始数据。
6. SQL 初始化完成后，应用继续启动 Controller、Swagger 等 Web 组件。

因此，正常启动顺序可以理解为：

```text
启动 MySQL 容器 -> 启动 Spring Boot -> 连接数据库 -> 执行 schema.sql -> 执行 data.sql -> 启动 Web 服务
```

本项目的 `schema.sql` 使用 `CREATE TABLE IF NOT EXISTS`，`data.sql` 使用 `ON DUPLICATE KEY UPDATE`，所以应用重复启动时不会重复建表，也不会重复插入相同角色。
