-- V3__encoding_outbox.sql
-- 人脸向量同步表，用于 MySQL 到 Qdrant 的异步同步

CREATE TABLE encoding_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    operation VARCHAR(10) NOT NULL COMMENT '操作类型：ADD=添加 DELETE=删除',
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING=待处理 DONE=已处理',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    processed_at DATETIME COMMENT '处理时间',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人脸向量同步表';
