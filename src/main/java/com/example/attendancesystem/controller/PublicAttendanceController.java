package com.example.attendancesystem.controller;

import com.example.attendancesystem.model.Attendance;
import com.example.attendancesystem.model.Student;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.StudentRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/public/attendance")
public class PublicAttendanceController {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public PublicAttendanceController(StudentRepository studentRepository, AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @PostMapping("/record")
    public ResponseEntity<String> recordAttendance(@RequestParam @NotBlank String uid) {
        Student student = studentRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Student not found with UID: " + uid));

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(LocalDate.now());
        attendance.setTime_exit(LocalTime.now());
        attendance.setStatus("PRESENT");

        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Attendance recorded for student: " + student.getName());
    }
}