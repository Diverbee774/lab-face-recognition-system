package com.lab.face.service;

import com.lab.face.entity.EncodingOutbox;
import com.lab.face.entity.Student;
import com.lab.face.mapper.EncodingOutboxMapper;
import com.lab.face.mapper.StudentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxSyncTask {

    private static final Logger log = LoggerFactory.getLogger(OutboxSyncTask.class);
    private static final int BATCH_SIZE = 50;

    @Autowired
    private EncodingOutboxMapper encodingOutboxMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private VectorService vectorService;

    @Scheduled(fixedDelay = 5000)
    public void syncEncodings() {
        try {
            List<EncodingOutbox> pendingRecords = encodingOutboxMapper.findByStatus(
                EncodingOutbox.STATUS_PENDING, BATCH_SIZE);

            if (pendingRecords.isEmpty()) {
                return;
            }

            log.info("处理 {} 条待同步记录", pendingRecords.size());

            for (EncodingOutbox record : pendingRecords) {
                try {
                    if (EncodingOutbox.OPERATION_ADD.equals(record.getOperation())) {
                        processAdd(record);
                    } else if (EncodingOutbox.OPERATION_DELETE.equals(record.getOperation())) {
                        processDelete(record);
                    }
                    encodingOutboxMapper.updateStatus(record.getId(), EncodingOutbox.STATUS_DONE);
                } catch (Exception e) {
                    log.error("处理 outbox 记录失败, id={}", record.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("syncEncodings 任务执行失败", e);
        }
    }

    private void processAdd(EncodingOutbox record) {
        Student student = studentMapper.findById(record.getStudentId());
        if (student == null) {
            log.warn("学生不存在, outbox id={}, studentId={}", record.getId(), record.getStudentId());
            return;
        }
        if (student.getEncoding() == null || student.getEncoding().isEmpty()) {
            log.warn("学生 {} 没有 encoding, 跳过", record.getStudentId());
            return;
        }
        int result = vectorService.addEncoding(student.getId(), student.getEncoding());
        if (result == 0) {
            log.info("已将学生 {} 的 encoding 同步到 Qdrant", student.getId());
        } else {
            throw new RuntimeException("添加 encoding 失败, 返回值=" + result);
        }
    }

    private void processDelete(EncodingOutbox record) {
        int result = vectorService.deleteEncoding(record.getStudentId());
        if (result == 0) {
            log.info("已从 Qdrant 删除学生 {} 的 encoding", record.getStudentId());
        } else {
            throw new RuntimeException("删除 encoding 失败, 返回值=" + result);
        }
    }
}
