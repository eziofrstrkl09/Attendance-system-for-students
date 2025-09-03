package com.example.attendancesystem.controller;

import com.example.attendancesystem.model.Attendance;
import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public AdminController(StudentRepository studentRepository, AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok(attendanceRepository.findByDate(date));
    }

    @GetMapping("/attendance/student/{studentId}")
    public ResponseEntity<List<Attendance>> getAttendanceByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceRepository.findByStudentStudentId(studentId));
    }
}