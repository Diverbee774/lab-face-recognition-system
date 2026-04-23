package com.lab.face.mapper;

import com.lab.face.entity.EncodingOutbox;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EncodingOutboxMapper {

    @Select("SELECT * FROM encoding_outbox WHERE status = #{status} ORDER BY created_at ASC LIMIT #{limit}")
    List<EncodingOutbox> findByStatus(@Param("status") String status, @Param("limit") int limit);

    @Insert("INSERT INTO encoding_outbox (student_id, operation, status) VALUES (#{studentId}, #{operation}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(EncodingOutbox outbox);

    @Update("UPDATE encoding_outbox SET status = #{status}, processed_at = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
