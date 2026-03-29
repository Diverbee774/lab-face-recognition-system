package com.lab.face.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private Long id;
    private String studentNo;
    private String name;
    private String password;
    private String encoding;
    private String imageUrl;
    private String imageBase64;
    private Integer status;
    private Integer hasFace;
    private LocalDateTime createdAt;
}
