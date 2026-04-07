package com.lab.face.controller;

import com.lab.face.common.Result;
import com.lab.face.entity.Lab;
import com.lab.face.entity.Student;
import com.lab.face.service.LabService;
import com.lab.face.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    @Autowired
    private LabService labService;

    @Autowired
    private StudentService studentService;

    @PostMapping("/add")
    public Result addLab(@RequestBody Lab lab) {
        labService.addLab(lab);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateLab(@RequestBody Lab lab) {
        labService.updateLab(lab);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteLab(@PathVariable Long id) {
        labService.deleteLab(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result listLabs() {
        return Result.success(labService.getAllLabs());
    }

    @GetMapping("/{id}")
    public Result getLab(@PathVariable Long id) {
        return Result.success(labService.getLabById(id));
    }

    @GetMapping("/{labId}/students")
    public Result getLabStudents(@PathVariable Long labId) {
        return Result.success(labService.getLabStudents(labId));
    }

    @PostMapping("/{labId}/students/{studentId}")
    public Result addStudentToLab(@PathVariable Long labId, @PathVariable Long studentId) {
        labService.addStudentToLab(labId, studentId);
        return Result.success();
    }

    @DeleteMapping("/{labId}/students/{studentId}")
    public Result removeStudentFromLab(@PathVariable Long labId, @PathVariable Long studentId) {
        labService.removeStudentFromLab(labId, studentId);
        return Result.success();
    }

    @GetMapping("/students")
    public Result getAllStudents() {
        return Result.success(studentService.getAllStudents());
    }
}
