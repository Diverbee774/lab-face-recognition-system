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
        studentService.register(student);
        return Result.success();
    }

    @GetMapping("/list")
    public Result listStudents() {
        return Result.success(studentService.getAllStudents());
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
