package com.lab.face.service;

import com.lab.face.entity.FaceEncoding;
import com.lab.face.entity.Lab;
import com.lab.face.entity.Student;
import com.lab.face.entity.StudentLab;
import com.lab.face.mapper.LabMapper;
import com.lab.face.mapper.StudentLabMapper;
import com.lab.face.mapper.StudentMapper;
import com.lab.face.thrift.*;
import com.lab.face.util.ImageUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class AccessService {

    private static final Logger log = LoggerFactory.getLogger(AccessService.class);

    @Autowired
    private FaceRecognitionClient faceRecognitionClient;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private StudentLabMapper studentLabMapper;

    @Autowired
    private LabMapper labMapper;

    @Autowired
    private VectorService vectorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final double THRESHOLD = 0.6;

    public Map<String, Object> recognize(String imageBase64, Long labId) {
        Map<String, Object> result = new HashMap<>();

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
            result.put("hasFace", false);
            return result;
        }

        FaceInfo face = response.getFaces().get(0);
        List<Double> currentEncoding = face.getEncoding();
        log.info("currentEncoding size={}, first={}", currentEncoding.size(), currentEncoding.get(0));

        List<Float> queryVector = new ArrayList<>();
        for (Double d : currentEncoding) {
            queryVector.add(d.floatValue());
        }

        List<Map<String, Object>> searchResults = vectorService.searchSimilar(queryVector, 1);
        log.info("Vector search results: {}", searchResults);

        double maxSimilarity = 0;
        Student matchedStudent = null;

        if (!searchResults.isEmpty()) {
            Map<String, Object> best = searchResults.get(0);
            Long studentId = ((Number) best.get("id")).longValue();
            maxSimilarity = ((Number) best.get("score")).doubleValue();
            matchedStudent = studentMapper.findById(studentId);
        }

        if (matchedStudent == null || maxSimilarity < THRESHOLD) {
            result.put("hasFace", true);
            result.put("matched", false);
            result.put("similarity", maxSimilarity);
            result.put("faceLocation", Map.of(
                    "top", face.getTop(),
                    "right", face.getRight(),
                    "bottom", face.getBottom(),
                    "left", face.getLeft()
            ));
            return result;
        }

        boolean hasAccess = checkAccess(matchedStudent.getId(), labId);

        result.put("hasFace", true);
        result.put("matched", true);
        result.put("hasAccess", hasAccess);
        result.put("similarity", maxSimilarity);
        result.put("studentId", matchedStudent.getId());
        result.put("studentNo", matchedStudent.getStudentNo());
        result.put("name", matchedStudent.getName());
        result.put("imageBase64", matchedStudent.getImageBase64());
        result.put("faceLocation", Map.of(
                "top", face.getTop(),
                "right", face.getRight(),
                "bottom", face.getBottom(),
                "left", face.getLeft()
        ));

        return result;
    }

    private boolean checkAccess(Long studentId, Long labId) {
        if (labId == null) {
            return true;
        }

        Lab lab = labMapper.findById(labId);
        if (lab == null) {
            return false;
        }

        if (lab.getAccessMode() == 2) {
            return true;
        }

        if (lab.getAccessMode() == 3) {
            return false;
        }

        StudentLab sl = studentLabMapper.findByStudentIdAndLabId(studentId, labId);
        return sl != null;
    }

    private List<Double> parseEncoding(String encodingJson) {
        try {
            FaceEncoding faceEncoding = objectMapper.readValue(encodingJson, FaceEncoding.class);
            return faceEncoding.getEncoding();
        } catch (Exception e) {
            log.error("parseEncoding failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private double cosineSimilarity(List<Double> A, List<Double> B) {
        if (A == null || B == null || A.isEmpty() || B.isEmpty()) {
            log.info("A or B is null/empty");
            return 0;
        }
        if (A.size() != B.size()) {
            log.info("A.size={} != B.size={}", A.size(), B.size());
            return 0;
        }

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < A.size(); i++) {
            dotProduct += A.get(i) * B.get(i);
            normA += A.get(i) * A.get(i);
            normB += B.get(i) * B.get(i);
        }

        log.info("dotProduct={}, normA={}, normB={}", dotProduct, normA, normB);
        log.info("A[0]={}, B[0]={}, A[1]={}, B[1]={}", A.get(0), B.get(0), A.get(1), B.get(1));

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
