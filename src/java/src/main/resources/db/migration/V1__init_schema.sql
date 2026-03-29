-- V1__init_schema.sql
-- 初始数据库表结构

-- ----------------------------
-- 1. admin 管理员表
-- ----------------------------
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    name VARCHAR(100) COMMENT '姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ----------------------------
-- 2. student 学生表
-- ----------------------------
CREATE TABLE student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    student_no VARCHAR(50) NOT NULL COMMENT '学号',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    password VARCHAR(255) COMMENT '密码',
    encoding JSON COMMENT '128维人脸特征向量',
    image_url VARCHAR(500) COMMENT '照片URL',
    status TINYINT DEFAULT 1 COMMENT '1=启用 0=禁用',
    has_face TINYINT DEFAULT 0 COMMENT '1=已录入人脸 0=未录入',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- ----------------------------
-- 3. lab 实验室表
-- ----------------------------
CREATE TABLE lab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '实验室名称',
    location VARCHAR(200) COMMENT '位置',
    access_mode TINYINT DEFAULT 1 COMMENT '1=白名单模式 2=开放模式 3=维护中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实验室表';

-- ----------------------------
-- 4. student_lab 学生-实验室权限表
-- ----------------------------
CREATE TABLE student_lab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    UNIQUE KEY uk_student_lab (student_id, lab_id),
    CONSTRAINT fk_student_lab_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_lab_lab FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生-实验室权限表';

-- ----------------------------
-- 5. access_record 进出记录表
-- ----------------------------
CREATE TABLE access_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    lab_id BIGINT NOT NULL COMMENT '实验室ID',
    access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '进出时间',
    access_type TINYINT NOT NULL COMMENT '1=进门 2=出门',
    status TINYINT NOT NULL COMMENT '1=允许 2=拒绝',
    fail_reason VARCHAR(100) COMMENT '失败原因',
    CONSTRAINT fk_access_record_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_record_lab FOREIGN KEY (lab_id) REFERENCES lab(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进出记录表';

-- ----------------------------
-- 索引
-- ----------------------------
CREATE INDEX idx_access_record_time ON access_record(access_time);
CREATE INDEX idx_access_record_student ON access_record(student_id);
CREATE INDEX idx_access_record_lab ON access_record(lab_id);

-- 初始管理员账号需要通过接口创建或手动插入
