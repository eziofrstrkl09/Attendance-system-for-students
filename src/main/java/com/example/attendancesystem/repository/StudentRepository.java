package com.example.attendancesystem.repository;

import com.example.attendancesystem.model.Students;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Students, Integer> {
    Optional<Students> findByUid(String uid);
    List<Students> findByClassEntityClassId(Integer classId);

}