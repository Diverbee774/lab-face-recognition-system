package com.lab.face.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Lab {
    private Long id;
    private String name;
    private String location;
    private Integer accessMode;
    private LocalDateTime createdAt;
}
