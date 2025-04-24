# RssHub - RSS源管理与文章聚合系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green)
![MyBatis](https://img.shields.io/badge/MyBatis-latest-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)

RssHub是一个功能强大的RSS源管理系统，用于收集、整理和管理来自各种网站的RSS源内容，实现信息的集中化访问和查阅。

## 项目特点

- **RSS源管理**：添加、编辑、删除和分类管理RSS源
- **自动抓取**：定时或按需抓取RSS源的最新内容
- **内容管理**：查看、标记、收藏和删除文章
- **数据统计**：提供来源、分类和文章数量的统计信息
- **用户系统**：支持多用户访问，每个用户可以管理自己的订阅源
- **响应式设计**：支持PC和移动端访问

## 技术栈

- **后端**：Spring Boot 3.4.4, MyBatis
- **数据库**：MySQL 8.0
- **RSS解析**：Rome
- **HTML清理**：Jsoup
- **身份验证**：JWT Token
- **前端**：Vue.js (单独仓库)

## 系统架构

RssHub采用了经典的三层架构：

- **控制器层**：处理HTTP请求和响应
- **服务层**：实现业务逻辑，包括RSS解析、文章处理等
- **数据访问层**：使用MyBatis进行数据库操作

系统主要由以下几个服务组成：
- `RssParserService`：负责RSS源的解析、内容提取和数据转换
- `RssService`：负责调度、任务管理和定时抓取
- `ArticleService`：管理文章的增删改查
- `CategoryService`：管理RSS源分类
- `UserService`：用户管理和身份验证

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

1. 克隆仓库
```bash
git clone https://github.com/yourusername/RssHub.git
cd RssHub
```

2. 配置数据库
```properties
# 修改 src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/rsshub?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. 构建和运行
```bash
mvn clean package
java -jar target/RssHub-0.0.1-SNAPSHOT.jar
```

4. 访问系统
```
http://localhost:8080
```

## API接口

RssHub提供了完整的RESTful API，请参考 [API文档](doc/接口文档.md)。

## 开发指南

### 添加新的RSS源解析器

如需添加特定网站的自定义解析逻辑，请参考 `RssParserService` 类，并根据需要扩展相关功能。

### 数据库迁移

项目使用Flyway进行数据库版本控制，新的数据库变更请添加到 `src/main/resources/db/migration` 目录。

## 贡献指南

欢迎提交Pull Request或Issue，请确保您的代码符合以下要求：
1. 遵循代码规范
2. 添加必要的单元测试
3. 更新相关文档

## 许可证

本项目采用 [MIT 许可证](LICENSE)。

# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.4/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.4/maven-plugin/build-image.html)
* [MyBatis Framework](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.4/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [MyBatis Quick Start](https://github.com/mybatis/spring-boot-starter/wiki/Quick-Start)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

