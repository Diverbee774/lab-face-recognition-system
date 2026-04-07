package com.lab.face.service;

import com.lab.face.entity.Admin;
import com.lab.face.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    public Admin login(String username, String password) {
        Admin admin = adminMapper.findByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!admin.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return admin;
    }
}