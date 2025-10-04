package com.example.attendancesystem.controller;

import com.example.attendancesystem.dto.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final UsersRepository usersRepository;
    private final TeachersRepository teachersRepository;
    private final ClassesRepository classesRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(
            StudentRepository studentRepository,
            AttendanceRepository attendanceRepository,
            UsersRepository usersRepository,
            TeachersRepository teachersRepository,
            ClassesRepository classesRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.usersRepository = usersRepository;
        this.teachersRepository = teachersRepository;
        this.classesRepository = classesRepository;
        this.passwordEncoder = passwordEncoder;
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
            student.setEmail(request.getEmail());
            student.setPhone(request.getPhone());

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

    @GetMapping("/students/{id}")
    public ResponseEntity<Students> getStudentById(@PathVariable Integer id, Authentication authentication) {
        // 1. Find the requested student
        Optional<Students> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Students student = studentOpt.get();

        // 2. Get the authenticated user and their role
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 3. Check authorization
        if (user.getRole() == Users.Role.PRINCIPAL) {
            // Principal can view any student
            return ResponseEntity.ok(student);
        }

        // For teachers, check if they are authorized for this student's class
        Teachers teacher = teachersRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Teacher not found for user: " + username));

        Classes teacherClass = teacher.getClassEntity();
        if (teacherClass != null && teacherClass.getClassId().equals(student.getClassEntity().getClassId())) {
            // Teacher is authorized for this student's class
            return ResponseEntity.ok(student);
        }

        // 4. If not authorized, return Forbidden
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @RequestParam LocalDate date, Authentication authentication) {
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<com.example.attendancesystem.model.Attendance> attendanceList;

        if (user.getRole() == Users.Role.PRINCIPAL) {
            attendanceList = attendanceRepository.findByDate(date);
        } else {
            Teachers teacher = teachersRepository.findByUserUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found for user: " + username));
            Classes classEntity = teacher.getClassEntity();
            if (classEntity == null) {
                return ResponseEntity.ok(List.of()); // No class assigned
            }
            attendanceList = attendanceRepository.findByClassIdAndDateRange(classEntity.getClassId(), date, date);
        }
        // Map the list of Attendance entities to a list of AttendanceResponse DTOs
        List<AttendanceResponse> responseList = attendanceList.stream()
                .map(AttendanceResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
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

    @GetMapping("/classes")
    public ResponseEntity<List<Classes>> getAllClasses(Authentication authentication) {
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (user.getRole() == Users.Role.PRINCIPAL) {
            return ResponseEntity.ok(classesRepository.findAll());
        }
        Teachers teacher = teachersRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Teacher not found for user: " + username));
        Classes classEntity = teacher.getClassEntity();
        if (classEntity == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(List.of(classEntity));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(Authentication authentication) {
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (user.getRole() != Users.Role.PRINCIPAL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Users> allUsers = usersRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();

        for (Users u : allUsers) {
            if (u.getRole() == Users.Role.TEACHER) {
                Optional<Teachers> teacherOpt = teachersRepository.findByUserUserId(u.getUserId());
                teacherOpt.ifPresent(teacher -> {
                    userResponses.add(new UserResponse(u.getUserId(), u.getUsername(), teacher.getName(), u.getRole(), teacher.getClassEntity()));
                });
            } else if (u.getRole() == Users.Role.PRINCIPAL) {
                // For Principal, name can be the username as there's no separate name field
                userResponses.add(new UserResponse(u.getUserId(), u.getUsername(), u.getUsername(), u.getRole(), null));
            }
        }

        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/users/profile")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserResponse response;
        if (user.getRole() == Users.Role.TEACHER) {
            Teachers teacher = teachersRepository.findByUserUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Teacher profile not found for user: " + username));
            response = new UserResponse(user.getUserId(), user.getUsername(), teacher.getName(), user.getRole(), teacher.getClassEntity());
        } else { // PRINCIPAL
            response = new UserResponse(user.getUserId(), user.getUsername(), user.getUsername(), user.getRole(), null);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/teacher")
    public ResponseEntity<?> createTeacher(@Valid @RequestBody CreateTeacherRequest request, Authentication authentication) {
        // 1. Authorization Check: Only a PRINCIPAL can create a teacher.
        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        if (currentUser.getRole() != Users.Role.PRINCIPAL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only principals can create new teachers.");
        }

        // 2. Validate Class: Ensure the class exists and is unassigned.
        Optional<Classes> classOpt = classesRepository.findById(request.getClassId());
        if (classOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The selected class does not exist.");
        }
        Classes classToAssign = classOpt.get();
        if (classToAssign.getTeacher() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This class is already assigned to another teacher.");
        }

        // 3. Check if username already exists
        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }

        // 4. Create and save the new User
        Users newUser = new Users();
        newUser.setUsername(request.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Users.Role.TEACHER);
        Users savedUser = usersRepository.save(newUser);

        // 5. Create and save the new Teacher profile
        Teachers newTeacher = new Teachers();
        newTeacher.setName(request.getName());
        newTeacher.setUser(savedUser);
        Teachers savedTeacher = teachersRepository.save(newTeacher);

        // 6. Assign the class to the new teacher
        classToAssign.setTeacher(savedTeacher);
        classesRepository.save(classToAssign);

        // 7. Prepare and return the response DTO
        UserResponse responseDto = new UserResponse(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedTeacher.getName(),
                savedUser.getRole(),
                classToAssign
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Transactional
    @PutMapping("/users/teacher/{userId}")
    public ResponseEntity<?> updateTeacher(@PathVariable Integer userId, @Valid @RequestBody UpdateTeacherRequest request, Authentication authentication) {
        // 1. Authorization Check
        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        if (currentUser.getRole() != Users.Role.PRINCIPAL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only principals can edit teachers.");
        }

        // 2. Find the teacher and user to update
        Teachers teacherToUpdate = teachersRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher not found for user ID: " + userId));
        Users userToUpdate = teacherToUpdate.getUser();

        // 3. Update teacher's name
        teacherToUpdate.setName(request.getName());

        // 4. Handle class re-assignment
        Classes currentClass = teacherToUpdate.getClassEntity();
        Integer newClassId = request.getClassId();

        if (currentClass == null || !currentClass.getClassId().equals(newClassId)) {
            // Find the new class
            Classes newClass = classesRepository.findById(newClassId)
                    .orElseThrow(() -> new RuntimeException("New class not found with ID: " + newClassId));

            // Check if the new class is already assigned to someone else
            if (newClass.getTeacher() != null && !newClass.getTeacher().getTeacherId().equals(teacherToUpdate.getTeacherId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The selected class is already assigned to another teacher.");
            }

            // Unassign the old class
            if (currentClass != null) {
                currentClass.setTeacher(null);
                classesRepository.save(currentClass);
            }

            // Assign the new class
            newClass.setTeacher(teacherToUpdate);
            classesRepository.save(newClass);
        }

        Teachers updatedTeacher = teachersRepository.save(teacherToUpdate);

        // 5. Prepare and return the response DTO
        UserResponse responseDto = new UserResponse(
                userToUpdate.getUserId(),
                userToUpdate.getUsername(),
                updatedTeacher.getName(),
                userToUpdate.getRole(),
                updatedTeacher.getClassEntity()
        );

        return ResponseEntity.ok(responseDto);
    }

    @Transactional
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId, Authentication authentication) {
        // 1. Authorization Check
        Users currentUser = usersRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        if (currentUser.getRole() != Users.Role.PRINCIPAL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 2. Find the user to delete
        Users userToDelete = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to delete not found with ID: " + userId));

        // 3. If it's a teacher, unassign their class and delete the teacher profile
        if (userToDelete.getRole() == Users.Role.TEACHER) {
            teachersRepository.findByUserUserId(userId).ifPresent(teacher -> {
                if (teacher.getClassEntity() != null) {
                    teacher.getClassEntity().setTeacher(null);
                }
                teachersRepository.delete(teacher);
            });
        }

        // 4. Delete the user
        usersRepository.delete(userToDelete);

        return ResponseEntity.noContent().build();
    }
}
