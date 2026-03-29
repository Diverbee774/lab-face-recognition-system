package com.lab.face.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccessRecord {
    private Long id;
    private Long studentId;
    private Long labId;
    private LocalDateTime accessTime;
    private Integer accessType;
    private Integer status;
    private String failReason;
}
