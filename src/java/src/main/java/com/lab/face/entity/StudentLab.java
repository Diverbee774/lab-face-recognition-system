package com.lab.face.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentLab {
    private Long id;
    private Long studentId;
    private Long labId;
    private LocalDateTime createdAt;
}
