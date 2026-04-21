package com.lab.face.controller;

import com.lab.face.common.Result;
import com.lab.face.entity.Student;
import com.lab.face.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/register")
    public Result register(@RequestBody Student student) {
        try {
            studentService.register(student);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result listStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        return Result.success(studentService.getStudents(page, pageSize, search));
    }

    @GetMapping("/{id}")
    public Result getStudent(@PathVariable Long id) {
        return Result.success(studentService.getStudentById(id));
    }

    @PutMapping("/update")
    public Result updateStudent(@RequestBody Student student) {
        studentService.updateStudent(student);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success();
    }
}
