package com.example.attendancesystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="teachers")
public class Teachers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(nullable = false,length=100)
    private String name;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true )
    private Users user;

    @OneToOne(mappedBy = "teacher")
    @JsonIgnore // Break circular reference by ignoring classEntity
    private Classes classEntity;

}
