package com.lab.face.controller;

import com.lab.face.common.Result;
import com.lab.face.entity.Admin;
import com.lab.face.service.AdminService;
import com.lab.face.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody Admin loginAdmin) {
        Admin admin = adminService.login(loginAdmin.getUsername(), loginAdmin.getPassword());
        String token = JwtUtils.generateToken(admin.getId(), admin.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("admin", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "name", admin.getName()
        ));
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }

    @GetMapping("/info")
    public Result info(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        var claims = JwtUtils.parseToken(token);

        Map<String, Object> data = new HashMap<>();
        data.put("id", claims.get("adminId"));
        data.put("username", claims.getSubject());
        data.put("name", claims.get("name", String.class));
        return Result.success(data);
    }
}