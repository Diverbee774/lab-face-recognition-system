package com.lab.face;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FaceReconLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaceReconLabApplication.class, args);
    }
}
