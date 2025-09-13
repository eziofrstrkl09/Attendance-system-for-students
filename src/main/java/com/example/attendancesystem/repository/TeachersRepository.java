package com.example.attendancesystem.repository;

import com.example.attendancesystem.model.Teachers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeachersRepository extends JpaRepository<Teachers,Integer> {
    Optional<Teachers> findByUserUserId(Integer userId);
}
