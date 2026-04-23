package com.lab.face.service;

import com.lab.face.entity.EncodingOutbox;
import com.lab.face.entity.Student;
import com.lab.face.thrift.FaceInfo;
import com.lab.face.thrift.EncodeRequest;
import com.lab.face.thrift.EncodeResponse;
import com.lab.face.thrift.FaceRecognitionClient;
import com.lab.face.mapper.EncodingOutboxMapper;
import com.lab.face.mapper.StudentMapper;
import com.lab.face.util.ImageUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.thrift.TException;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private EncodingOutboxMapper encodingOutboxMapper;

    @Autowired
    private FaceRecognitionClient faceRecognitionClient;

    private final ObjectMapper objectMapper;

    public StudentService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Transactional
    public void register(Student student) {
        String imageBase64 = student.getImageBase64();
        if (imageBase64 == null || imageBase64.isEmpty()) {
            throw new RuntimeException("图片不能为空");
        }

        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
        byte[] jpegBytes = ImageUtils.convertToJpeg(imageBytes);

        EncodeRequest request = new EncodeRequest();
        request.setImage_data(jpegBytes);

        EncodeResponse response;
        try {
            response = faceRecognitionClient.encode(request);
        } catch (TException e) {
            throw new RuntimeException("调用Python服务失败: " + e.getMessage());
        }

        if (response.getCode() != 200) {
            throw new RuntimeException("Python服务处理失败: " + response.getMsg());
        }

        if (response.getFaces() == null || response.getFaces().isEmpty()) {
            throw new RuntimeException("未检测到人脸");
        }

        FaceInfo face = response.getFaces().get(0);

        String encodingJson;
        try {
            encodingJson = objectMapper.writeValueAsString(face);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化人脸信息失败: " + e.getMessage());
        }

        student.setEncoding(encodingJson);
        student.setStatus(1);
        student.setHasFace(1);
        student.setImageBase64(null);

        studentMapper.insert(student);

        EncodingOutbox outbox = new EncodingOutbox();
        outbox.setStudentId(student.getId());
        outbox.setOperation(EncodingOutbox.OPERATION_ADD);
        outbox.setStatus(EncodingOutbox.STATUS_PENDING);
        encodingOutboxMapper.insert(outbox);
    }

    public List<Student> getAllStudents() {
        return studentMapper.findAll();
    }

    public Map<String, Object> getStudents(int page, int pageSize, String search) {
        PageHelper.startPage(page, pageSize);
        List<Student> list = studentMapper.searchStudents(search);
        PageInfo<Student> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    public Student getStudentById(Long id) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        return student;
    }

    public void updateStudent(Student student) {
        if (student.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }
        Student existing = studentMapper.findById(student.getId());
        if (existing == null) {
            throw new RuntimeException("学生不存在");
        }
        studentMapper.update(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }

        EncodingOutbox outbox = new EncodingOutbox();
        outbox.setStudentId(id);
        outbox.setOperation(EncodingOutbox.OPERATION_DELETE);
        outbox.setStatus(EncodingOutbox.STATUS_PENDING);
        encodingOutboxMapper.insert(outbox);

        studentMapper.deleteById(id);
    }
}
