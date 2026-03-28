# Prompt 上下文文档

**项目名称**：基于人脸识别的实验室智能出入管理系统
**最后更新**：2026-03-28

---

## 一、项目基本信息

### 1.1 项目概述
- 毕设项目：B/S 架构的智能门禁系统
- 核心功能：人脸识别 + 权限管控，刷脸进出实验室
- 用户角色：普通用户（刷脸通行）、系统管理员（后台管理）

### 1.2 技术架构
```
Vue.js 前端 → Java Spring Boot → Python Thrift (人脸识别)
                    ↑                    ↑
                 Thrift RPC          纯算法服务
              (业务逻辑+数据库)      (图片→向量)
```

### 1.3 技术栈
| 层级 | 技术 |
|------|------|
| 前端 | Vue.js + Element UI |
| 后端 | Java Spring Boot |
| 算法服务 | Python + face_recognition + dlib |
| RPC框架 | Apache Thrift |
| 数据库 | MySQL |
| 开发环境 | Anaconda (ML 环境) |

---

## 二、项目结构

```
e:\project\face_recon_lab_person_ms\
├── src/
│   ├── idl/
│   │   └── face.thrift              # Thrift 接口定义
│   ├── py/
│   │   ├── face_service/            # Thrift 生成代码
│   │   │   ├── __init__.py
│   │   │   ├── constants.py
│   │   │   ├── ttypes.py
│   │   │   └── FaceRecognition.py
│   │   └── main.py                  # Python 服务入口
│   └── java/                        # Java 后端 (待开发)
├── 计划书.md
└── .gitignore
```

---

## 三、Thrift 接口定义

**文件**：`src/idl/face.thrift`

```thrift
namespace py face_service

struct FaceInfo {
    1: list<double> encoding,    // 128维人脸向量
    2: i32 top,                  // 人脸框上边界
    3: i32 right,                // 人脸框右边界
    4: i32 bottom,               // 人脸框下边界
    5: i32 left                  // 人脸框左边界
}

struct EncodeRequest {
    1: required binary image_data  // 图片字节流
}

struct EncodeResponse {
    1: required i32 code,
    2: optional list<FaceInfo> faces,  // 检测到的所有人脸
    3: required string msg
}

service FaceRecognition {
    EncodeResponse encode(1: EncodeRequest req)
}
```

---

## 四、Python 服务

**入口**：`src/py/main.py`

**接口**：
- `encode(req)` - 接收图片字节流，返回所有人脸的位置+128维向量

**职责**：纯算法，不做业务逻辑、不存储数据

---

## 五、Java 端职责

1. **对外接口**：接收前端请求
2. **调用 Python**：发送图片字节流，获取人脸向量
3. **业务逻辑**：
   - 用户注册 → 调用 encode → 存储向量到数据库
   - 人脸识别 → 调用 encode → 遍历底库比对 → 返回结果
4. **数据库**：管理用户、实验室、权限、日志

---

## 六、人脸识别流程

### 6.1 注册新人脸
```
前端拍照 → Java接收图片 → 调用Python encode → 拿到向量 → 存入tb_face_base表
```

### 6.2 身份识别
```
前端拍照 → Java接收图片 → 调用Python encode → 拿到向量
                                      ↓
                        从tb_face_base查所有向量 → 遍历比对 → 找最小距离
                                      ↓
                              返回用户ID + 置信度
```

---

## 七、数据库核心表

| 表名 | 说明 |
|------|------|
| tb_user | 用户表 |
| tb_lab | 实验室表 |
| tb_face_base | 人脸底库（存128维向量） |
| tb_lab_user_relation | 白名单关系 |
| tb_access_log | 通行日志 |

---

## 八、已知约束

1. **Python 只做特征提取**：不存储、不做比对
2. **Java 掌控业务**：所有业务逻辑在 Java 端
3. **多实验室支持**：通过 lab_id 区分
4. **Thrift RPC**：用户明确要求使用 Thrift，不是 HTTP

---

## 九、环境信息

- **Python**：3.x，ML 环境，安装了 face_recognition、dlib
- **Thrift**：0.22.0
- **Java**：待搭建 Spring Boot
- **MySQL**：待安装
- **项目根目录**：`e:\project\face_recon_lab_person_ms`
