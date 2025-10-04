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
        // Find the student by their unique RFID UID
        Optional<Students> studentOpt = studentRepository.findByUid(uid);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found with UID: " + uid);
        }
        Students student = studentOpt.get();

        // Determine the entry time
        LocalTime entryTime = (time != null && !time.isEmpty()) ? LocalTime.parse(time) : LocalTime.now();

        // --- CHANGE IS HERE ---
        // Find if an attendance record already exists for this student on this date
        Attendance attendance = attendanceRepository.findByStudentStudentIdAndDate(student.getStudentId(), LocalDate.now())
                .orElse(new Attendance()); // If not, create a new one

        // If it's a new record, set the student and date
        if (attendance.getAttendanceId() == null) {
            attendance.setStudent(student);
            attendance.setDate(LocalDate.now());
        }

        // Set the status to present and record the entry time
        attendance.setStatus(true); // true means present
        attendance.setTimeEntry(entryTime);

        // Save the new or updated record
        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Attendance entry recorded for student: " + student.getName());
    }

    @PostMapping("/exit")
    public ResponseEntity<String> recordExit(@RequestParam @NotBlank String uid, @RequestParam(required = false) String time) {
        Optional<Students> studentOpt = studentRepository.findByUid(uid);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found with UID: " + uid);
        }
        Students student = studentOpt.get();

        LocalTime exitTime = (time != null && !time.isEmpty()) ? LocalTime.parse(time) : LocalTime.now();

        // Find the existing attendance record for today. It should exist from the entry scan.
        Optional<Attendance> attendanceOpt = attendanceRepository.findByStudentStudentIdAndDate(student.getStudentId(), LocalDate.now());

        if (attendanceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No entry record found for today. Cannot record exit.");
        }

        Attendance attendance = attendanceOpt.get();
        attendance.setTimeExit(exitTime);
        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Exit time recorded for student: " + student.getName());
    }
}