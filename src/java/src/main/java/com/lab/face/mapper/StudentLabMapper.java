package com.lab.face.mapper;

import com.lab.face.entity.StudentLab;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentLabMapper {

    @Select("SELECT * FROM student_lab WHERE student_id = #{studentId}")
    List<StudentLab> findByStudentId(Long studentId);

    @Select("SELECT * FROM student_lab WHERE lab_id = #{labId}")
    List<StudentLab> findByLabId(Long labId);

    @Select("SELECT * FROM student_lab WHERE student_id = #{studentId} AND lab_id = #{labId}")
    StudentLab findByStudentIdAndLabId(Long studentId, Long labId);

    @Insert("INSERT INTO student_lab (student_id, lab_id) VALUES (#{studentId}, #{labId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(StudentLab studentLab);

    @Delete("DELETE FROM student_lab WHERE student_id = #{studentId} AND lab_id = #{labId}")
    void delete(Long studentId, Long labId);

    @Delete("DELETE FROM student_lab WHERE lab_id = #{labId}")
    void deleteByLabId(Long labId);
}