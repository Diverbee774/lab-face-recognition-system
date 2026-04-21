package com.lab.face.mapper;

import com.lab.face.entity.Lab;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabMapper {

    @Select("SELECT * FROM lab WHERE id = #{id}")
    Lab findById(Long id);

    @Select("SELECT * FROM lab")
    List<Lab> findAll();

    @Select("<script>" +
            "SELECT * FROM lab WHERE 1=1 " +
            "<if test='search != null and search != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{search}, '%') OR location LIKE CONCAT('%', #{search}, '%')) " +
            "</if>" +
            "</script>")
    List<Lab> searchLabs(@Param("search") String search);

    @Insert("INSERT INTO lab (name, location, access_mode) VALUES (#{name}, #{location}, #{accessMode})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Lab lab);

    @Update("UPDATE lab SET name=#{name}, location=#{location}, access_mode=#{accessMode} WHERE id=#{id}")
    void update(Lab lab);

    @Delete("DELETE FROM lab WHERE id = #{id}")
    void deleteById(Long id);
}
