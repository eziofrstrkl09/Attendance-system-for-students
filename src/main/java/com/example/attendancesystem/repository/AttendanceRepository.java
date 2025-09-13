package com.example.attendancesystem.repository;

import com.example.attendancesystem.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByStudentStudentId(Integer studentId); // works because student -> studentId
    List<Attendance> findByDate(LocalDate date);

    Optional<Attendance> findByStudentStudentIdAndDate(Integer studentId, LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.student.classEntity.classId = :classId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByClassIdAndDateRange(@Param("classId") Integer classId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.student.studentId = :studentId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentIdAndDateRange(@Param("studentId") Integer studentId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}
