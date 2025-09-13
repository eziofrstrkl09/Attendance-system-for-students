package com.example.attendancesystem.controller;

import com.example.attendancesystem.model.Students;
import com.example.attendancesystem.model.Attendance;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.repository.AttendanceRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

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
    public ResponseEntity<String> recordAttendance(@RequestParam @NotBlank String uid, @RequestParam(required = false) String time) {
        Students student = studentRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Student not found with UID: " + uid));

        LocalTime entryTime = time != null ? LocalTime.parse(time) : LocalTime.now();

        Optional<Attendance> existingAttendance = attendanceRepository.findByStudentStudentIdAndDate(student.getStudentId(), LocalDate.now());

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            attendance.setTimeEntry(entryTime);
            attendance.setStatus(true);
            attendanceRepository.save(attendance);
            return ResponseEntity.ok("Attendance updated for student: " + student.getName());
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(LocalDate.now());
        attendance.setStatus(true);
        attendance.setTimeEntry(entryTime);
        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Attendance recorded for student: " + student.getName());
    }

    @PostMapping("/exit")
    public ResponseEntity<String> recordExit(@RequestParam @NotBlank String uid, @RequestParam(required = false) String time) {
        Students student = studentRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Student not found with UID: " + uid));

        LocalTime exitTime = time != null ? LocalTime.parse(time) : LocalTime.now();

        Attendance existingAttendance = attendanceRepository.findByStudentStudentIdAndDate(student.getStudentId(), LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No attendance record found for today"));

        existingAttendance.setTimeExit(exitTime);
        attendanceRepository.save(existingAttendance);
        return ResponseEntity.ok("Exit time recorded for student: " + student.getName());
    }
}