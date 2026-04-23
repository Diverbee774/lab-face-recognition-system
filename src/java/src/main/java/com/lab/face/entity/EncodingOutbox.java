package com.lab.face.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EncodingOutbox {
    private Long id;
    private Long studentId;
    private String operation;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public static final String OPERATION_ADD = "ADD";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_DONE = "DONE";
}
