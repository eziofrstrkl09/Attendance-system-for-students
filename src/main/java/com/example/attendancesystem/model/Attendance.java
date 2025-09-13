package com.example.attendancesystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name="attendance",uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","date"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="attendance_id")
    private Integer attendanceId;

    @ManyToOne
    @JoinColumn(name="student_id",nullable=false)
    @JsonIgnore // Prevent deep serialization of student
    private Students student;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean status;                 //true=present, false=absent

    @Column(name = "time_exit")
    private LocalTime timeExit;

    @Column(name = "time_entry")
    private LocalTime timeEntry;

}
