package com.example.attendancesystem.controller;

import com.example.attendancesystem.dto.CreateStudentRequest;
import com.example.attendancesystem.model.Classes;
import com.example.attendancesystem.model.Students;
import com.example.attendancesystem.model.Teachers;
import com.example.attendancesystem.model.Users;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.ClassesRepository;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.repository.TeachersRepository;
import com.example.attendancesystem.repository.UsersRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final UsersRepository usersRepository;
    private final TeachersRepository teachersRepository;
    private final ClassesRepository classesRepository;

    @Autowired
    public AdminController(
            StudentRepository studentRepository,
            AttendanceRepository attendanceRepository,
            UsersRepository usersRepository,
            TeachersRepository teachersRepository,
            ClassesRepository classesRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.usersRepository = usersRepository;
        this.teachersRepository = teachersRepository;
        this.classesRepository = classesRepository;
    }

    @PostMapping("/students")
    public ResponseEntity<String> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        // 1) Check UID uniqueness
        if (studentRepository.findByUid(request.getUid()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Student with UID already exists: " + request.getUid());
        }

        // 2) Find class by className + section
        Optional<Classes> classOpt = classesRepository.findByClassNameAndSection(
                request.getClassName(), request.getSection());

        if (classOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Class not found: " + request.getClassName() + " " + request.getSection());
        }
        Classes classEntity = classOpt.get();

        // 3) Check (rollNo, class) uniqueness — using existing repository method
        List<Students> studentsInClass = studentRepository.findByClassEntityClassId(classEntity.getClassId());
        boolean rollExists = studentsInClass.stream()
                .anyMatch(s -> s.getRollNo().equals(request.getRollNo()));
        if (rollExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Roll number " + request.getRollNo() + " already exists in class " +
                            request.getClassName() + " section " + request.getSection());
        }

        // 4) Create student
        try {
            Students student = new Students();
            student.setUid(request.getUid());
            student.setName(request.getName());
            student.setRollNo(request.getRollNo());
            student.setClassEntity(classEntity);

            studentRepository.save(student);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Student created: " + student.getName() + " (UID: " + student.getUid() + ")");
        } catch (Exception e) {
            // defensive: return 500 with message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create student: " + e.getMessage());
        }
    }

    @GetMapping("/students")
    public ResponseEntity<List<Students>> getAllStudents(Authentication authentication) {
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (user.getRole() == Users.Role.PRINCIPAL) {
            return ResponseEntity.ok(studentRepository.findAll());
        }
        Teachers teacher = teachersRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Teacher not found for user: " + username));
        Classes classEntity = teacher.getClassEntity();
        if (classEntity == null) {
            return ResponseEntity.ok(List.of()); // No class assigned
        }
        return ResponseEntity.ok(studentRepository.findByClassEntityClassId(classEntity.getClassId()));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<com.example.attendancesystem.model.Attendance>> getAttendanceByDate(
            @RequestParam LocalDate date, Authentication authentication) {
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (user.getRole() == Users.Role.PRINCIPAL) {
            return ResponseEntity.ok(attendanceRepository.findByDate(date));
        }
        Teachers teacher = teachersRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Teacher not found for user: " + username));
        Classes classEntity = teacher.getClassEntity();
        if (classEntity == null) {
            return ResponseEntity.ok(List.of()); // No class assigned
        }
        return ResponseEntity.ok(attendanceRepository.findByClassIdAndDateRange(classEntity.getClassId(), date, date));
    }

    @GetMapping("/attendance/student/{studentId}")
    public ResponseEntity<List<com.example.attendancesystem.model.Attendance>> getAttendanceByStudent(
            @PathVariable Integer studentId, Authentication authentication) {
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Students student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        if (user.getRole() == Users.Role.PRINCIPAL) {
            return ResponseEntity.ok(attendanceRepository.findByStudentStudentId(studentId));
        }
        Teachers teacher = teachersRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Teacher not found for user: " + username));
        Classes classEntity = teacher.getClassEntity();
        if (classEntity == null || !student.getClassEntity().getClassId().equals(classEntity.getClassId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(List.of()); // Teacher not authorized for this student
        }
        return ResponseEntity.ok(attendanceRepository.findByStudentStudentId(studentId));
    }
}
