package com.lab.face.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.Distance;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Service
public class VectorService {

    private static final Logger log = LoggerFactory.getLogger(VectorService.class);
    private static final String COLLECTION_NAME = "face_encodings";
    private static final int VECTOR_SIZE = 128;

    private QdrantClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            client = new QdrantClient(
                QdrantGrpcClient.newBuilder("localhost", 6334, false).build()
            );
            createCollectionIfNotExists();
            log.info("VectorService 初始化成功");
        } catch (Exception e) {
            log.error("VectorService 初始化失败", e);
        }
    }

    private void createCollectionIfNotExists() {
        try {
            client.getCollectionInfoAsync(COLLECTION_NAME).get();
            log.info("Collection {} 已存在", COLLECTION_NAME);
        } catch (Exception e) {
            log.info("创建 Collection {}", COLLECTION_NAME);
            try {
                client.createCollectionAsync(
                    COLLECTION_NAME,
                    VectorParams.newBuilder()
                        .setDistance(Distance.Cosine)
                        .setSize(VECTOR_SIZE)
                        .build()
                ).get();
                log.info("Collection {} 创建成功", COLLECTION_NAME);
            } catch (Exception ex) {
                log.error("创建 Collection 失败", ex);
            }
        }
    }

    public int addEncoding(Long studentId, String encodingJson) {
        if (client == null) {
            log.error("Qdrant client 未初始化");
            return -1;
        }
        try {
            List<Float> vector = parseEncoding(encodingJson);
            if (vector == null || vector.size() != VECTOR_SIZE) {
                log.error("学生 {} 的 encoding 无效, size={}", studentId, vector != null ? vector.size() : "null");
                return -2;
            }

            PointStruct point = PointStruct.newBuilder()
                .setId(id(studentId))
                .setVectors(vectors(vector))
                .putAllPayload(Map.of("student_id", value(studentId)))
                .build();

            client.upsertAsync(COLLECTION_NAME, List.of(point)).get();
            log.info("学生 {} 的 encoding 已添加到 Qdrant", studentId);
            return 0;
        } catch (Exception e) {
            log.error("添加 encoding 失败, studentId={}", studentId, e);
            return -3;
        }
    }

    public int deleteEncoding(Long studentId) {
        if (client == null) {
            log.error("Qdrant client 未初始化");
            return -1;
        }
        try {
            client.deleteAsync(COLLECTION_NAME, List.of(id(studentId))).get();
            log.info("学生 {} 的 encoding 已从 Qdrant 删除", studentId);
            return 0;
        } catch (Exception e) {
            log.error("删除 encoding 失败, studentId={}", studentId, e);
            return -1;
        }
    }

    public List<Map<String, Object>> searchSimilar(List<Float> queryVector, int topK) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (client == null) {
            log.error("Qdrant client 未初始化");
            return results;
        }
        try {
            SearchPoints searchRequest = SearchPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .addAllVector(queryVector)
                .setLimit(topK)
                .build();

            List<ScoredPoint> searchResult = client.searchAsync(searchRequest).get();

            for (ScoredPoint point : searchResult) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", point.getId().getNum());
                result.put("score", point.getScore());
                results.add(result);
            }
            log.info("搜索返回 {} 个结果", results.size());
        } catch (Exception e) {
            log.error("搜索相似 encoding 失败", e);
        }
        return results;
    }

    private List<Float> parseEncoding(String encodingJson) {
        try {
            JsonNode root = objectMapper.readTree(encodingJson);
            JsonNode encodingNode = root.has("encoding") ? root.get("encoding") : root;
            List<Float> result = new ArrayList<>();
            for (JsonNode node : encodingNode) {
                result.add((float) node.asDouble());
            }
            return result;
        } catch (Exception e) {
            log.error("解析 encoding JSON 失败", e);
            return null;
        }
    }
}
