package com.lab.face.controller;

import com.lab.face.common.Result;
import com.lab.face.service.AccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    @Autowired
    private AccessService accessService;

    @PostMapping("/recognize")
    public Result recognize(@RequestBody Map<String, Object> params) {
        String imageBase64 = (String) params.get("imageBase64");
        Long labId = params.get("labId") != null ? ((Number) params.get("labId")).longValue() : null;

        Map<String, Object> result = accessService.recognize(imageBase64, labId);
        return Result.success(result);
    }
}