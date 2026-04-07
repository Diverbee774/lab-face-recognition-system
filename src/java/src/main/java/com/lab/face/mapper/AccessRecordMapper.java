package com.lab.face.mapper;

import com.lab.face.entity.AccessRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccessRecordMapper {

    @Insert("INSERT INTO access_record (student_id, lab_id, access_time, access_type, status, fail_reason) " +
            "VALUES (#{studentId}, #{labId}, #{accessTime}, #{accessType}, #{status}, #{failReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AccessRecord record);

    @Select("SELECT * FROM access_record ORDER BY access_time DESC LIMIT 100")
    List<AccessRecord> findRecent();
}