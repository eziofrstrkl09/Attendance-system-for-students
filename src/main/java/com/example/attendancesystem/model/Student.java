package com.example.attendancesystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false,unique = true)
    private int rollNo;

    @Column(nullable=false)
    private String className;

    @Column(nullable=false,unique = true)
    private String uid;

}
