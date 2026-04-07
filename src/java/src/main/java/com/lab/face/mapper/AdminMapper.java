package com.lab.face.mapper;

import com.lab.face.entity.Admin;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(String username);
}