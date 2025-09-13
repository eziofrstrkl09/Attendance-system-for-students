package com.example.attendancesystem.repository;

import com.example.attendancesystem.model.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassesRepository extends JpaRepository<Classes,Integer> {
    Optional<Classes> findByClassNameAndSection(String className,String section);
    Optional<Classes> findByTeacherTeacherId(Integer teacherId);
}
