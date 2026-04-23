# 人脸识别实验室门禁系统

基于人脸识别技术的实验室门禁管理系统，用于毕业设计。

## 技术架构

- **后端**：Java Spring Boot 3.2.0 + MyBatis + MySQL
- **人脸识别**：Python face_recognition 库
- **向量数据库**：Qdrant（用于高速人脸特征匹配）
- **通信框架**：Apache Thrift（Java 调用 Python 服务）
- **数据同步**：Outbox 模式（MySQL 与 Qdrant 同步）
- **构建工具**：Maven

## 项目结构

```
face_recon_lab_person_ms/
├── src/
│   ├── java/                 # Java 后端
│   │   ├── pom.xml
│   │   └── src/main/java/com/lab/face/
│   │       ├── FaceReconLabApplication.java
│   │       ├── controller/
│   │       ├── service/
│   │       │   ├── VectorService.java      # Qdrant 向量操作
│   │       │   ├── OutboxSyncTask.java     # Outbox 同步任务
│   │       │   └── ...
│   │       ├── thrift/        # Thrift 生成代码
│   │       ├── entity/
│   │       └── mapper/
│   ├── py/                   # Python 人脸识别服务
│   │   ├── main.py           # 服务入口
│   │   ├── face_service/     # Thrift 生成代码
│   │   └── service/
│   │       ├── handler.py    # 业务处理
│   │       └── loader.py     # 底库加载
│   └── idl/
│       └── face.thrift       # Thrift 接口定义
├── build.ps1                 # 构建脚本（含 Qdrant 下载）
├── run.ps1                   # 启动脚本（含 Qdrant 启动）
├── requirements.txt          # Python 依赖
└── README.md
```

## 核心设计

### Outbox 同步模式

为了保证 MySQL 与 Qdrant 的数据一致性，采用 Outbox 模式：

1. **注册学生**：同时写入 `student` 表和 `encoding_outbox` 表（同一事务）
2. **后台同步**：`OutboxSyncTask` 每 5 秒扫描 outbox 表，将数据同步到 Qdrant
3. **删除学生**：写入 DELETE 操作的 outbox 记录，后台同步时从 Qdrant 删除

```
注册学生 → MySQL(student) + MySQL(encoding_outbox) → Qdrant
删除学生 → MySQL(encoding_outbox: DELETE) → Qdrant 删除
```

### 人脸匹配流程

1. 摄像头拍照，发送到 Java 后端
2. Java 调用 Python 服务获取人脸特征向量（128维）
3. 用特征向量在 Qdrant 中搜索最相似的人脸
4. 根据相似度阈值（默认 0.6）判断是否匹配
5. 结合实验室权限（白名单/公开）判断是否放行

## 环境要求

### Python 环境（Anaconda）

需要创建 `ML` 环境：

```bash
conda create -n ML python=3.10
conda activate ML
```

### Python 依赖

```bash
pip install -r requirements.txt
```

或单独安装：

```bash
pip install thrift==0.16.0
pip install face_recognition
```

> 注意：thrift 版本必须为 0.16.0，新版本不支持服务端

### Java 环境

- JDK 17+
- Maven 3.6+

### 数据库

- MySQL 8.0+
- 数据库名：`face_lab`
- 字符集：`utf8mb4`

创建数据库：

```sql
CREATE DATABASE face_lab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Docker（Qdrant 向量数据库）

需要安装 Docker Desktop，Qdrant 镜像会在 build.ps1 中自动下载。

## 配置

### 数据库配置

编辑 `src/java/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/face_lab?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Thrift 配置

```yaml
thrift:
  python:
    host: localhost
    port: 9090
```

## 构建

### 一键构建（推荐）

```powershell
.\build.ps1
```

构建内容：

1. 下载 Qdrant 镜像
2. 安装 Python 依赖（thrift、face_recognition）
3. 编译 Java 项目

### 手动分步执行

```powershell
# 下载 Qdrant
docker pull qdrant/qdrant

# Python 依赖
conda run -n ML pip install thrift==0.16.0
conda run -n ML pip install face_recognition

# Java 构建
mvn -f src/java/pom.xml clean compile
```

## 运行

### 一键启动

```powershell
.\run.ps1
```

启动内容：

1. 启动 Qdrant 容器（端口 6333、6334）
2. 启动 Python Thrift 服务（9090）
3. 启动 Java 后端（8080）
4. 启动 Vue 前端（5173）

### 手动分步启动

**终端1 - 启动 Qdrant：**

```bash
docker run -d --name qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

**终端2 - 启动 Python 人脸识别服务：**

```bash
conda run -n ML python src/py/main.py
```

**终端3 - 启动 Java 后端：**

```bash
cd src/java
mvn spring-boot:run
```

### 服务地址

| 服务               | 地址                  |
| ------------------ | --------------------- |
| Qdrant Dashboard   | http://localhost:6333 |
| Java 后端          | http://localhost:8080 |
| Python Thrift 服务 | localhost:9090        |
| Vue 前端           | http://localhost:5173 |

## 测试接口

### 人脸特征提取

```
POST http://localhost:8080/test/encode
Content-Type: multipart/form-data

参数：
  image: 图片文件（支持 jpg、png 等常见格式）

返回示例：
  Code: 200, Msg: success, Faces: 1
```

### 门禁识别

```
POST http://localhost:8080/access/recognize
Content-Type: application/json

{
  "imageBase64": "base64编码的图片",
  "labId": 1
}

返回示例：
{
  "code": 200,
  "msg": "成功",
  "data": {
    "hasFace": true,
    "matched": true,
    "similarity": 0.85,
    "hasAccess": true,
    "studentId": 1,
    "studentNo": "2021001",
    "name": "张三"
  }
}
```

## Thrift 接口说明

定义文件：`src/idl/face.thrift`

```thrift
struct FaceInfo {
    1: list<double> encoding,   // 128维人脸特征向量
    2: i32 top,                // 人脸包围框上
    3: i32 right,              // 人脸包围框右
    4: i32 bottom,             // 人脸包围框下
    5: i32 left                // 人脸包围框左
}

struct EncodeRequest {
    1: required binary image_data
}

struct EncodeResponse {
    1: required i32 code,
    2: optional list<FaceInfo> faces,
    3: required string msg
}
```
