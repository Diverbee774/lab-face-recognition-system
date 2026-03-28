# 人脸识别实验室门禁系统

基于人脸识别技术的实验室门禁管理系统，用于毕业设计。

## 技术架构

- **后端**：Java Spring Boot 3.2.0 + MyBatis + MySQL
- **人脸识别**：Python face_recognition 库
- **通信框架**：Apache Thrift（Java 调用 Python 服务）
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
│   │       ├── thrift/        # Thrift 生成代码
│   │       └── entity/
│   ├── py/                   # Python 人脸识别服务
│   │   ├── main.py           # 服务入口
│   │   ├── face_service/     # Thrift 生成代码
│   │   └── service/
│   │       ├── handler.py    # 业务处理
│   │       └── loader.py     # 底库加载
│   └── idl/
│       └── face.thrift       # Thrift 接口定义
├── build.ps1                 # 构建脚本
├── run.ps1                   # 启动脚本
├── requirements.txt          # Python 依赖
└── README.md
```

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

```bash
.\build.ps1
```

或手动分步执行：

```bash
# Python 依赖
conda activate ML
pip install thrift==0.16.0
pip install face_recognition

# Java 构建
mvn -f src/java/pom.xml clean compile
```

## 运行

### 一键启动

```bash
.\run.ps1
```

### 手动分步启动

**终端1 - 启动 Python 人脸识别服务：**

```bash
conda activate ML
D:\AnaConda\envs\ML\python.exe src/py/main.py
```

**终端2 - 启动 Java 后端：**

```bash
cd src/java
mvn spring-boot:run
```

### 服务地址

| 服务 | 地址 |
|------|------|
| Java 后端 | http://localhost:8080 |
| Python Thrift 服务 | localhost:9090 |

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

## Thrift 接口说明

定义文件：`src/idl/face.thrift`

```thrift
struct FaceInfo {
    1: list<double> encoding,   // 128维人脸特征向量
    2: i32 top,                 // 人脸包围框上
    3: i32 right,               // 人脸包围框右
    4: i32 bottom,              // 人脸包围框下
    5: i32 left                 // 人脸包围框左
}

struct EncodeRequest {
    1: required binary image_data
}

struct EncodeResponse {
    1: required i32 code,
    2: optional list<FaceInfo> faces,
    3: required string msg
}

service FaceRecognition {
    EncodeResponse encode(1: EncodeRequest req)
}
```

## 开发说明

### Thrift 代码生成

如需修改 IDL，重新生成命令：

```bash
# 生成 Python 代码
thrift-0.22.0.exe --gen py src/idl/face.thrift
# 把 gen-py 下的 face_service 复制到 src/py/

# 生成 Java 代码
thrift-0.22.0.exe --gen java src/idl/face.thrift
# 把 gen-java 下的 com 目录复制到 src/java/src/main/java/com/lab/face/
```

### 添加新的 Thrift 依赖

```bash
# 在 src/java/pom.xml 添加
mvn -f src/java/pom.xml clean compile
```

## 常见问题

### Q: Python 服务启动报错 "No module named 'thrift.transport.TServerSocket'"

A: 确保 thrift 版本为 0.16.0：`pip install thrift==0.16.0`

### Q: Java 启动报错 "Failed to configure a DataSource"

A: 确保 MySQL 数据库已创建，配置用户名密码正确

### Q: 连接被拒绝 "Connection reset by peer"

A: 确保 Python 服务先启动，再启动 Java 服务；检查协议是否一致（都用 TCompactProtocol）
