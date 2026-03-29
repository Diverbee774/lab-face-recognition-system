package com.lab.face.service;

import com.lab.face.entity.Lab;
import com.lab.face.mapper.LabMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LabService {

    @Autowired
    private LabMapper labMapper;

    public void addLab(Lab lab) {
        if (lab.getName() == null || lab.getName().isEmpty()) {
            throw new RuntimeException("实验室名称不能为空");
        }
        if (lab.getAccessMode() == null) {
            lab.setAccessMode(1);
        }
        labMapper.insert(lab);
    }

    public void updateLab(Lab lab) {
        if (lab.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }
        Lab existing = labMapper.findById(lab.getId());
        if (existing == null) {
            throw new RuntimeException("实验室不存在");
        }
        labMapper.update(lab);
    }

    public void deleteLab(Long id) {
        Lab lab = labMapper.findById(id);
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }
        labMapper.deleteById(id);
    }

    public List<Lab> getAllLabs() {
        return labMapper.findAll();
    }

    public Lab getLabById(Long id) {
        Lab lab = labMapper.findById(id);
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }
        return lab;
    }
}
