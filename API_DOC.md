# API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **响应格式**: 统一响应包装

### 统一响应格式

```json
{
    "code": 200,
    "msg": "成功",
    "data": null
}
```

| code | 说明 |
|------|------|
| 200 | 成功 |
| 500 | 业务错误（msg 包含具体原因） |

---

## 学生接口

### 1. 学生注册

**请求**
```
POST /student/register
```

**Body (JSON)**
```json
{
    "studentNo": "2021001",
    "name": "张三",
    "password": "123456",
    "imageBase64": "iVBORw0KGgoAAAANSUhEUgAA..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| studentNo | string | 是 | 学号 |
| name | string | 是 | 姓名 |
| password | string | 是 | 密码 |
| imageBase64 | string | 是 | 图片Base64编码（不带 data:image/... 前缀） |

**响应示例**
```json
{
    "code": 200,
    "msg": "成功",
    "data": null
}
```

**错误码**
| msg | 说明 |
|-----|------|
| 图片不能为空 | imageBase64 为空 |
| 未检测到人脸 | 图片中没有人脸 |
| 调用Python服务失败 | Thrift 服务异常 |

---

## 实验室接口

### 2. 添加实验室

**请求**
```
POST /lab/add
```

**Body (JSON)**
```json
{
    "name": "计算机实验室A",
    "location": "A栋101",
    "accessMode": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 实验室名称 |
| location | string | 否 | 位置 |
| accessMode | int | 否 | 访问模式（默认1） |

**accessMode 说明**
| 值 | 说明 |
|----|------|
| 1 | 白名单模式 |
| 2 | 开放模式 |
| 3 | 维护中 |

**响应示例**
```json
{
    "code": 200,
    "msg": "成功",
    "data": null
}
```

---

### 3. 更新实验室

**请求**
```
PUT /lab/update
```

**Body (JSON)**
```json
{
    "id": 1,
    "name": "计算机实验室A",
    "location": "A栋102",
    "accessMode": 2
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 实验室ID |
| name | string | 是 | 实验室名称 |
| location | string | 否 | 位置 |
| accessMode | int | 否 | 访问模式 |

**响应示例**
```json
{
    "code": 200,
    "msg": "成功",
    "data": null
}
```

---

### 4. 删除实验室

**请求**
```
DELETE /lab/{id}
```

**Path Parameters**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 实验室ID |

**响应示例**
```json
{
    "code": 200,
    "msg": "成功",
    "data": null
}
```

---

### 5. 查询所有实验室

**请求**
```
GET /lab/list
```

**响应示例**
```json
{
    "code": 200,
    "msg": "成功",
    "data": [
        {
            "id": 1,
            "name": "计算机实验室A",
            "location": "A栋101",
            "accessMode": 1,
            "createdAt": "2024-01-01T10:00:00"
        }
    ]
}
```

---

### 6. 查询单个实验室

**请求**
```
GET /lab/{id}
```

**Path Parameters**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 实验室ID |

**响应示例**
```json
{
    "code": 200,
    "msg": "成功",
    "data": {
        "id": 1,
        "name": "计算机实验室A",
        "location": "A栋101",
        "accessMode": 1,
        "createdAt": "2024-01-01T10:00:00"
    }
}
```

---

## 待开发接口

- [ ] 管理员登录
- [ ] 学生-实验室权限管理
- [ ] 进出记录查询
- [ ] 人脸识别进门
- [ ] 人脸识别出门
