# 任务看板后端 API 文档

## 项目概述

基于 Spring Boot + JDK 17 + H2 数据库的任务看板后端系统，提供完整的任务管理功能。

## 技术栈

- JDK 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 内存数据库
- Lombok
- Hutool
- Apache Commons
- Hibernate Validator

## API 列表

### 1. 获取任务列表

- **接口地址**: `GET /api/tasks`
- **功能说明**: 获取所有任务
- **请求参数**: 无
- **返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "任务标题",
      "description": "任务描述",
      "status": "待办",
      "priority": "高",
      "dueDate": "2024-12-31T23:59:59",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ]
}
```

### 2. 获取单个任务

- **接口地址**: `GET /api/tasks/{id}`
- **功能说明**: 根据ID获取单个任务
- **路径参数**:
  - `id`: 任务ID
- **返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "任务标题",
    "description": "任务描述",
    "status": "待办",
    "priority": "高",
    "dueDate": "2024-12-31T23:59:59",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 3. 创建任务

- **接口地址**: `POST /api/tasks`
- **功能说明**: 创建新任务
- **请求参数**:
```json
{
  "title": "任务标题",
  "description": "任务描述",
  "status": "待办",
  "priority": "高",
  "dueDate": "2024-12-31T23:59:59"
}
```
- **参数验证**:
  - `title`: 必填，最大255字符
  - `description`: 最大1000字符
  - `status`: 必填，最大50字符
  - `priority`: 最大20字符
- **返回示例**:
```json
{
  "code": 200,
  "message": "任务创建成功",
  "data": {
    "id": 1,
    "title": "任务标题",
    "description": "任务描述",
    "status": "待办",
    "priority": "高",
    "dueDate": "2024-12-31T23:59:59",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 4. 更新任务

- **接口地址**: `PUT /api/tasks/{id}`
- **功能说明**: 更新任务信息
- **路径参数**:
  - `id`: 任务ID
- **请求参数**:
```json
{
  "title": "任务标题",
  "description": "任务描述",
  "status": "进行中",
  "priority": "中",
  "dueDate": "2024-12-31T23:59:59"
}
```
- **参数验证**:
  - `title`: 必填，最大255字符
  - `description`: 最大1000字符
  - `status`: 必填，最大50字符
  - `priority`: 最大20字符
- **返回示例**:
```json
{
  "code": 200,
  "message": "任务更新成功",
  "data": {
    "id": 1,
    "title": "任务标题",
    "description": "任务描述",
    "status": "进行中",
    "priority": "中",
    "dueDate": "2024-12-31T23:59:59",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T11:00:00"
  }
}
```

### 5. 删除任务

- **接口地址**: `DELETE /api/tasks/{id}`
- **功能说明**: 删除指定ID的任务
- **路径参数**:
  - `id`: 任务ID
- **返回示例**:
```json
{
  "code": 200,
  "message": "任务删除成功",
  "data": null
}
```

## 错误码说明

- `200`: 成功
- `400`: 参数校验失败
- `404`: 资源未找到
- `500`: 服务器内部错误

## 本地运行说明

### 1. 环境准备

- 安装 JDK 17+
- 安装 Maven 3.6+

### 2. 项目启动

```bash
# 克隆项目（如果适用）
git clone <repository-url>

# 进入项目目录
cd task-board-backend

# 编译并启动项目
mvn spring-boot:run
```

### 3. 访问应用

- 应用访问地址: `http://localhost:8080`
- H2数据库控制台: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 用户名: `sa`
  - 密码: 留空

### 4. 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=TaskServiceImplTest
```

## 项目结构

```
src/
├── main/
│   ├── java/com/taskboard/
│   │   ├── TaskBoardApplication.java          # 启动类
│   │   ├── common/
│   │   │   └── Result.java                    # 统一响应封装
│   │   ├── controller/
│   │   │   └── TaskController.java            # 任务控制器
│   │   ├── entity/
│   │   │   └── Task.java                      # 任务实体
│   │   ├── dto/
│   │   │   ├── TaskCreateRequest.java         # 创建任务请求DTO
│   │   │   └── TaskUpdateRequest.java         # 更新任务请求DTO
│   │   ├── repository/
│   │   │   └── TaskRepository.java            # 任务数据访问层
│   │   ├── service/
│   │   │   ├── TaskService.java               # 任务服务接口
│   │   │   └── impl/
│   │   │       └── TaskServiceImpl.java       # 任务服务实现
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java    # 全局异常处理器
│   └── resources/
│       └── application.yml                    # 应用配置文件
└── test/
    └── java/com/taskboard/
        └── service/impl/
            └── TaskServiceImplTest.java       # 服务层单元测试
```

## 特性说明

1. **统一错误处理**: 所有异常都通过全局异常处理器统一处理
2. **参数校验**: 使用Hibernate Validator进行参数校验
3. **事务管理**: 在服务层使用@Transactional注解确保数据一致性
4. **代码简化**: 使用Lombok减少样板代码
5. **工具类集成**: 集成了Hutool和Apache Commons工具类
6. **内存数据库**: 使用H2内存数据库便于快速开发和测试