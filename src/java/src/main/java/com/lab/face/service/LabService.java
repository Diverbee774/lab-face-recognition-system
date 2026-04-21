package com.lab.face.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lab.face.entity.Lab;
import com.lab.face.entity.Student;
import com.lab.face.entity.StudentLab;
import com.lab.face.mapper.LabMapper;
import com.lab.face.mapper.StudentLabMapper;
import com.lab.face.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabService {

    @Autowired
    private LabMapper labMapper;

    @Autowired
    private StudentLabMapper studentLabMapper;

    @Autowired
    private StudentMapper studentMapper;

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

    public Map<String, Object> getLabs(int page, int pageSize, String search) {
        PageHelper.startPage(page, pageSize);
        List<Lab> list = labMapper.searchLabs(search);
        PageInfo<Lab> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    public Lab getLabById(Long id) {
        Lab lab = labMapper.findById(id);
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }
        return lab;
    }

    public List<Student> getLabStudents(Long labId) {
        List<StudentLab> slList = studentLabMapper.findByLabId(labId);
        List<Student> students = new ArrayList<>();
        for (StudentLab sl : slList) {
            Student s = studentMapper.findById(sl.getStudentId());
            if (s != null) {
                students.add(s);
            }
        }
        return students;
    }

    public void addStudentToLab(Long labId, Long studentId) {
        Lab lab = labMapper.findById(labId);
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        StudentLab existing = studentLabMapper.findByStudentIdAndLabId(studentId, labId);
        if (existing != null) {
            throw new RuntimeException("该学生已在白名单中");
        }
        StudentLab sl = new StudentLab();
        sl.setStudentId(studentId);
        sl.setLabId(labId);
        studentLabMapper.insert(sl);
    }

    public void removeStudentFromLab(Long labId, Long studentId) {
        StudentLab existing = studentLabMapper.findByStudentIdAndLabId(studentId, labId);
        if (existing == null) {
            throw new RuntimeException("该学生不在白名单中");
        }
        studentLabMapper.delete(studentId, labId);
    }
}
